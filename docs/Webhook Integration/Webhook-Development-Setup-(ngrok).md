# Webhook Development Setup (ngrok)

## 1. Overview

During development, the StreamHub Payment Service runs on the local machine.

Example

```
http://localhost:8083
```

This works perfectly when the frontend communicates with the backend because both are running on the developer's machine.

However, webhooks introduce a different communication pattern.

Instead of the frontend calling our backend, an external service (Razorpay) must call our backend.

This creates a networking problem.

---

# 2. Current Communication

Our current payment flow looks like this.

```
React Frontend

        │

        ▼

Spring Boot

http://localhost:8083

        │

        ▼

Razorpay
```

Everything works because our backend initiates the request to Razorpay.

Example

```
Spring Boot

↓

Create Order API

↓

Razorpay
```

Since the request originates from our application, Razorpay can always respond.

---

# 3. The Problem with Webhooks

A webhook works in the opposite direction.

Instead of our backend calling Razorpay,

Razorpay must call our backend.

```
Razorpay

↓

Spring Boot
```

The problem is that our backend is running on

```
http://localhost:8083
```

Localhost exists only on our computer.

Razorpay cannot access it.

---

# 4. Why Razorpay Cannot Reach localhost

Suppose Razorpay tries to send

```
POST http://localhost:8083/api/v1/webhooks/razorpay
```

From Razorpay's perspective,

```
localhost
```

means

```
Razorpay's own server
```

not our laptop.

Therefore

```
localhost
```

is never reachable from the Internet.

---

# 5. The Development Problem

Without ngrok

```
                Internet

                    │

                    ▼

             Razorpay Server

                    │

                    ▼

      http://localhost:8083

                    │

              ❌ Cannot Reach
```

The webhook never arrives.

No testing is possible.

---

# 6. What is ngrok?

ngrok is a secure tunneling tool.

It creates a public HTTPS URL that forwards requests to our local application.

Example

```
Local Application

http://localhost:8083

        │

        ▼

ngrok

        │

        ▼

https://abc123.ngrok-free.app
```

Now anyone on the Internet can communicate with our local application through this secure tunnel.

---

# 7. How ngrok Solves the Problem

Instead of configuring Razorpay with

```
http://localhost:8083
```

we configure

```
https://abc123.ngrok-free.app
```

The request flow becomes

```
Razorpay

↓

https://abc123.ngrok-free.app

↓

ngrok Tunnel

↓

http://localhost:8083

↓

Spring Boot
```

Our application behaves as if it were deployed on the Internet.

---

# 8. Webhook URL

Instead of

```
http://localhost:8083/api/v1/webhooks/razorpay
```

we register

```
https://abc123.ngrok-free.app/api/v1/webhooks/razorpay
```

inside the Razorpay Dashboard.

Now Razorpay can successfully deliver webhook events.

---

# 9. Does ngrok Generate the Webhook Secret?

No.

This is a common misconception.

ngrok is only responsible for exposing our local application to the Internet.

It does not create

- Key ID
- Key Secret
- Webhook Secret

---

# 10. What is the Webhook Secret?

The Webhook Secret is a shared secret between

```
Razorpay

and

Our Backend
```

We choose the value ourselves.

Example

```
streamhub-payment-webhook-test
```

The same value is configured

Inside Razorpay

```
Webhook Secret

↓

streamhub-payment-webhook-test
```

Inside Spring Boot

```yaml
razorpay:
  webhook-secret: streamhub-payment-webhook-test
```

Both sides now know the same secret.

---

# 11. Difference Between Credentials

## Key ID

Provided by Razorpay.

Purpose

Open Razorpay Checkout.

Safe to expose to the frontend.

---

## Key Secret

Provided by Razorpay.

Purpose

- Create Orders
- Verify Checkout Signature
- Fetch Payment Details

Backend only.

---

## Webhook Secret

Created by us.

Purpose

Verify webhook requests.

Backend only.

---

# 12. Development Workflow

```
Spring Boot

↓

localhost:8083

↓

ngrok

↓

https://abc123.ngrok-free.app

↓

Configured in Razorpay Dashboard

↓

Customer Pays

↓

Razorpay

↓

POST Webhook

↓

ngrok

↓

Spring Boot

↓

Verify Webhook Signature

↓

Process Event

↓

Update Database

↓

HTTP 200 OK
```

---

# 13. Do We Need ngrok in Production?

No.

In production, the Payment Service is already deployed on a public domain.

Example

```
https://payment.streamhub.com
```

Webhook URL

```
https://payment.streamhub.com/api/v1/webhooks/razorpay
```

Since the server is publicly accessible,

no tunnel is required.

---

# 14. Summary

During development, our Spring Boot application runs only on the local machine.

External systems like Razorpay cannot access localhost directly.

ngrok creates a secure public tunnel that forwards Internet requests to our local application, allowing Razorpay to deliver webhook events during development.

The Webhook Secret is completely unrelated to ngrok. It is a shared secret chosen by the developer and configured in both Razorpay and the backend to verify that webhook requests are authentic.