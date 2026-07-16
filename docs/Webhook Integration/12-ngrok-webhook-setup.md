# ngrok & Razorpay Webhook Development Setup

## 1. Overview

During development, the StreamHub Payment Service runs on the local machine.

Example

```
http://localhost:8083
```

This works perfectly when:

- React calls Spring Boot
- Spring Boot calls Razorpay APIs

because **our backend initiates the request**.

However, webhook communication works differently.

Instead of our application calling Razorpay,

Razorpay must call our application.

This creates a networking problem.

---

# 2. Current Payment Flow (Without Webhooks)

```
Frontend

        │

        ▼

Spring Boot

localhost:8083

        │

        ▼

Razorpay
```

Everything works because the backend sends requests to Razorpay.

Example

```
Create Order

↓

Spring Boot

↓

Razorpay
```

Since our backend starts the communication, Razorpay can always send the response back.

---

# 3. Webhook Communication

A webhook works in the opposite direction.

Instead of

```
Backend

↓

Razorpay
```

it becomes

```
Razorpay

↓

Backend
```

Now Razorpay must reach our application directly.

---

# 4. The Problem

Our application is running locally.

```
http://localhost:8083
```

The keyword

```
localhost
```

means

> "this computer"

For our laptop,

```
localhost
```

means

```
Our Laptop
```

For Razorpay's servers,

```
localhost
```

means

```
Razorpay's own server
```

NOT our laptop.

Therefore Razorpay can never reach our local machine.

Example

```
Razorpay

↓

http://localhost:8083

❌ Connection Failed
```

---

# 5. Why This Happens

Our laptop is inside a private network.

```
Internet

↓

Router

↓

Laptop

↓

localhost:8083
```

The Internet cannot directly access our localhost application.

Therefore Razorpay has nowhere to send webhook requests.

---

# 6. What is ngrok?

ngrok is a secure tunneling tool.

It creates a public HTTPS URL that forwards Internet traffic to our local application.

Example

Before ngrok

```
localhost:8083
```

After ngrok

```
https://abc123.ngrok-free.app
```

Internally

```
https://abc123.ngrok-free.app

↓

http://localhost:8083
```

Think of ngrok as a bridge between the Internet and your local computer.

---

# 7. How ngrok Solves the Problem

Without ngrok

```
             Internet

                 │

                 ▼

            Razorpay

                 │

                 ▼

        localhost:8083

                 │

          ❌ Not Reachable
```

With ngrok

```
             Internet

                 │

                 ▼

            Razorpay

                 │

                 ▼

https://abc123.ngrok-free.app

                 │

                 ▼

             ngrok Tunnel

                 │

                 ▼

http://localhost:8083

                 │

                 ▼

          Spring Boot
```

Now Razorpay can successfully send webhook events.

---

# 8. When Should We Use ngrok?

Use ngrok whenever:

- Your application is running locally.
- An external system must call your application.

Examples

- Razorpay Webhooks
- Stripe Webhooks
- GitHub Webhooks
- Slack Events
- Twilio Callbacks
- PayPal Webhooks

If your application only calls external APIs,

ngrok is NOT required.

---

# 9. Do We Need ngrok in Production?

No.

Production servers already have a public domain.

Example

```
https://payment.streamhub.com
```

Razorpay can directly access

```
https://payment.streamhub.com/api/v1/webhooks/razorpay
```

No tunnel is required.

---

# 10. Installing ngrok

## Step 1

Download ngrok

https://ngrok.com/download

Install it according to your operating system.

---

## Step 2

Create an ngrok Account

Visit

https://dashboard.ngrok.com

Create a free account.

---

## Step 3

Copy Your Authentication Token

Dashboard

↓

Your Authtoken

Example

```
2Jxxxxxxxxxxxxxxxxxxxxxxxx
```

---

## Step 4

Authenticate ngrok

Open Terminal

Run

```bash
ngrok config add-authtoken YOUR_AUTH_TOKEN
```

Example

```bash
ngrok config add-authtoken 2Jxxxxxxxxxxxxxxxxxx
```

This is done only once.

---

# 11. Running ngrok

Start your Spring Boot application.

```
http://localhost:8083
```

Open another terminal.

Run

```bash
ngrok http 8083
```

ngrok will display

```
Forwarding

https://abc123.ngrok-free.app

↓

http://localhost:8083
```

Copy the HTTPS URL.

We will use it in Razorpay.

---

# 12. Configure Razorpay Webhook

Open

Razorpay Dashboard

↓

Settings

↓

Webhooks

↓

Add New Webhook

Webhook URL

```
https://abc123.ngrok-free.app/api/v1/webhooks/razorpay
```

Webhook Secret

```
streamhub-payment-webhook-test
```

Choose the required events.

Example

- payment.authorized
- payment.captured
- payment.failed
- order.paid

Click Save.

---

# 13. Configure Spring Boot

application-dev.yml

```yaml
razorpay:
  webhook-secret: streamhub-payment-webhook-test
```

The value must exactly match the value configured in Razorpay Dashboard.

---

# 14. Testing Flow

```
Customer Pays

↓

Razorpay

↓

POST

https://abc123.ngrok-free.app/api/v1/webhooks/razorpay

↓

ngrok

↓

Spring Boot

↓

Verify Signature

↓

Update Database

↓

HTTP 200 OK
```

---

# 15. Verify ngrok is Working

Open

```
http://localhost:4040
```

ngrok provides a local dashboard.

You can inspect

- Request Headers
- Request Body
- Response
- Response Time
- Status Code

This is extremely useful for debugging webhook issues.

---

# 16. Common Problems

## 404 Not Found

Reason

Wrong webhook URL.

Solution

Verify

```
/api/v1/webhooks/razorpay
```

exists.

---

## Invalid Signature

Reason

Webhook Secret does not match.

Solution

Ensure the same secret is configured in

- Razorpay Dashboard
- Spring Boot

---

## 502 Bad Gateway

Reason

Spring Boot is not running.

Solution

Start the backend.

---

## ngrok URL Changed

Reason

Free ngrok generates a new URL each time it starts.

Solution

Update the Webhook URL in Razorpay Dashboard.

---

## Connection Refused

Reason

Wrong port.

Solution

Verify Spring Boot is running on

```
8083
```

and start ngrok using

```bash
ngrok http 8083
```

---

# 17. Summary

ngrok is a development tool that exposes a local application to the Internet through a secure HTTPS tunnel.

It allows external systems such as Razorpay to communicate with applications running on localhost.

Without ngrok, Razorpay cannot deliver webhook requests to a locally running Spring Boot application.

In production, ngrok is no longer required because the application is deployed on a publicly accessible domain.

During local development, ngrok provides the missing bridge between Razorpay's servers and the developer's machine, making webhook development and testing possible.