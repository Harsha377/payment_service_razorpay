# 19. How Does Razorpay Know the Webhook Was Processed Successfully?

A common question during webhook implementation is:

> We never call Razorpay after processing the webhook, so how does Razorpay know whether our backend processed the webhook successfully?

The answer lies in the HTTP request-response lifecycle.

When Razorpay sends a webhook, it behaves like an **HTTP Client**, and our Spring Boot application behaves like an **HTTP Server**.

---

## Step 1 - Razorpay Sends the Webhook Request

Razorpay initiates an HTTP POST request to our webhook endpoint.

```http
POST /api/v1/webhooks/razorpay HTTP/1.1

Host: payment.streamhub.com

X-Razorpay-Signature: <signature>

Content-Type: application/json

{
    "event":"payment.captured",
    ...
}
```

After sending this request, Razorpay waits for our server's response.

---

## Step 2 - Spring Boot Receives the Request

Spring Boot maps the request to our controller.

```java
@PostMapping("/razorpay")
public ResponseEntity<Void> receiveWebhook(
        @RequestHeader("X-Razorpay-Signature") String signature,
        @RequestBody String payload) {

    GatewayWebhookVerificationRequest request =
            GatewayWebhookVerificationRequest.builder()
                    .signature(signature)
                    .payload(payload)
                    .build();

    webhookService.processWebhook(request);

    return ResponseEntity.ok().build();
}
```

The controller delegates all business logic to the `WebhookService`.

During this time Razorpay is still waiting for our response.

---

## Step 3 - Business Logic Executes

Our application performs the following operations.

- Verify webhook signature
- Parse JSON payload
- Identify webhook event
- Execute business logic
- Update database
- Record payment events

Only after all these operations complete successfully does the controller continue.

---

## Step 4 - HTTP Response Is Sent Back

Finally, Spring executes

```java
return ResponseEntity.ok().build();
```

This does **not** return to another method inside our application.

Instead, Spring converts it into an HTTP response.

Internally it becomes

```http
HTTP/1.1 200 OK
Content-Length: 0
```

This response is automatically sent back over the same HTTP connection that Razorpay opened.

---

## Complete Request-Response Flow

```
Razorpay
      │
      │ HTTP POST
      ▼
Webhook Controller
      │
      ▼
Webhook Service
      │
      ▼
Verify Signature
      │
      ▼
Process Business Logic
      │
      ▼
Update Database
      │
      ▼
return ResponseEntity.ok()
      │
      │ HTTP 200 OK
      ▼
Razorpay
```

Notice that there is **no separate API call** to Razorpay.

The HTTP response itself is the acknowledgement.

---

## Why Do We Return HTTP 200?

HTTP 200 tells Razorpay:

> "I successfully received and processed your webhook."

Once Razorpay receives HTTP 200, it marks the webhook delivery as successful and stops retrying.

---

## What Happens If HTTP 200 Is Not Received?

Suppose our application successfully updates the database, but due to a network interruption, the HTTP response never reaches Razorpay.

```
Razorpay

↓

Webhook

↓

Our Backend

↓

Database Updated Successfully

↓

Network Failure

↓

HTTP 200 Lost
```

From Razorpay's perspective, the webhook may not have been processed.

Since Razorpay never received the acknowledgement, it retries the webhook.

```
payment.captured

↓

Retry

↓

Retry

↓

Retry
```

This retry mechanism ensures that important payment events are not lost.

---

## Why Is Idempotency Important?

Because webhook retries are expected in distributed systems.

If the same webhook is delivered multiple times, our application must not perform the same business operation multiple times.

For example,

```
payment.captured

↓

Payment = SUCCESS

↓

HTTP 200 Lost

↓

Razorpay Retries

↓

payment.captured Again
```

Without idempotency, the application may:

- Record duplicate payment events.
- Send duplicate emails.
- Generate duplicate invoices.
- Trigger duplicate downstream business logic.

With idempotency, the application recognizes that the payment has already been processed and safely ignores the duplicate webhook while still returning **HTTP 200 OK**.

---

## Key Takeaway

The controller does **not** explicitly call Razorpay after processing the webhook.

Instead,

```java
return ResponseEntity.ok().build();
```

is automatically converted by Spring Boot into an HTTP response.

That HTTP response is sent back to Razorpay over the same request-response connection.

For Razorpay, receiving **HTTP 200 OK** means:

> "The merchant has successfully processed this webhook."

If Razorpay does not receive HTTP 200, it assumes the webhook delivery failed and retries the webhook according to its retry policy.