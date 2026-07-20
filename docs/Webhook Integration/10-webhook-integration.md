# 10. Webhook Integration

## 1. Introduction

Payment processing does not end after the customer completes the payment on the checkout page. Once the payment is processed by the payment gateway, our backend must be informed about the final payment status. This is achieved through **Webhooks**.

A webhook is an HTTP callback sent by Razorpay whenever an important event occurs. Instead of our application continuously asking Razorpay whether the payment status has changed, Razorpay automatically notifies our backend.

The webhook implementation in the StreamHub Payment Service provides a secure and reliable mechanism to synchronize payment status between Razorpay and our system.

---

## 2. What is a Webhook?

A webhook is an HTTP POST request automatically sent by Razorpay to our backend whenever a configured event occurs.

Instead of polling Razorpay repeatedly for payment status, Razorpay pushes the event to our webhook endpoint.

For example,

- Customer completes payment
- Razorpay captures the payment
- Razorpay immediately sends a webhook request to our backend

Our backend receives the request, verifies its authenticity, processes the event, updates the database, and returns an HTTP 200 response.

---

### Simple Explanation

Imagine ordering food online.

Without a webhook:

You would continuously call the restaurant asking:

> "Is my food ready?"

every few seconds.

With a webhook:

The restaurant calls you and says:

> "Your food is ready."

That's exactly how a webhook works.

---

## 3. Why Do We Need Webhooks?

During checkout, the frontend calls the Verify Payment API after receiving the Razorpay success response.

However, relying only on the frontend is not reliable.

Consider the following situations:

- Customer closes the browser immediately after payment.
- Internet connection is lost.
- Mobile application crashes.
- Browser refreshes.
- Verify Payment API is never called.

In these situations, the payment may actually succeed in Razorpay, but our backend never receives confirmation.

This causes inconsistency between Razorpay and our database.

Webhooks solve this problem.

Whenever Razorpay processes a payment event, Razorpay directly informs our backend regardless of whether the frontend exists or not.

---

## 4. Polling vs Webhooks

### Polling

Polling means our application repeatedly asks Razorpay for payment status.

Example:

```
Backend
    │
    ▼
Is payment successful?
    │
    ▼
No
    │
    ▼
Wait
    │
    ▼
Ask Again
    │
    ▼
No
```

Problems with Polling

- Too many unnecessary API calls.
- Higher server load.
- Increased latency.
- Wastes network resources.
- More expensive at scale.

---

### Webhook

Webhook means Razorpay notifies us only when something important happens.

```
Customer Pays
        │
        ▼
Razorpay
        │
        ▼
Webhook
        │
        ▼
Our Backend
```

Advantages

- Real-time updates.
- No unnecessary API calls.
- Faster synchronization.
- Reliable event delivery.
- Lower infrastructure cost.

---

## 5. Why Does Our Payment Service Need Webhooks?

The StreamHub Payment Service is responsible for maintaining the correct payment state.

The frontend should never be considered the single source of truth.

Instead, Razorpay becomes the trusted source for payment events.

Our webhook implementation ensures that:

- Payment status is synchronized with Razorpay.
- Failed payments are recorded.
- Successful payments are completed.
- Future payment events can be processed.
- Payment data remains consistent.

---

## 6. Who Calls the Webhook?

Many developers initially think that the frontend calls the webhook.

This is incorrect.

The frontend never calls the webhook.

The customer never calls the webhook.

Only Razorpay calls the webhook.

Complete Flow

```
Customer
     │
     ▼
Frontend
     │
     ▼
Razorpay Checkout
     │
     ▼
Customer Completes Payment
     │
     ▼
Razorpay Servers
     │
     ▼
POST /api/v1/webhooks/razorpay
     │
     ▼
StreamHub Payment Service
```

The webhook request originates from Razorpay's servers.

---

## 7. Why Not Depend Only on Verify Payment API?

Our frontend currently performs this flow.

```
Frontend

↓

Checkout

↓

Payment Success

↓

Verify Payment API

↓

Backend
```

This works only if the frontend remains active.

Suppose:

- Browser closes.
- Internet disconnects.
- Mobile app crashes.
- Customer closes the application.

Then Verify Payment API is never executed.

Without a webhook:

Database

```
Payment

CREATED
```

But Razorpay

```
Payment

CAPTURED
```

Both systems become inconsistent.

Webhook guarantees eventual consistency by informing our backend independently of the frontend.

---

## 8. Benefits of Using Webhooks

Our webhook implementation provides the following benefits.

- Real-time payment synchronization.
- Automatic payment updates.
- Reliable backend communication.
- Secure event verification.
- Reduced dependency on frontend.
- Better fault tolerance.
- Easier auditing.
- Production-ready payment processing.

---
# 9. Webhook Endpoint

The StreamHub Payment Service exposes the following endpoint.

```
POST /api/v1/webhooks/razorpay
```

Content-Type

```
application/json
```

Required Header

```
X-Razorpay-Signature
```

Request Body

Raw JSON payload sent by Razorpay.

Example

```json
{
  "event": "payment.captured",
  "payload": {
    ... 
  }
}
```

Our controller receives the request and forwards it to the Webhook Service.

---

# 10. Webhook Security

A webhook endpoint is publicly accessible.

Anyone could potentially send an HTTP POST request to our endpoint.

For example,

```http
POST /api/v1/webhooks/razorpay
```

with

```json
{
    "event":"payment.captured"
}
```

If our backend trusted every request, anyone could mark payments as successful.

This is a major security vulnerability.

To prevent this, Razorpay signs every webhook request.

---

# 11. Webhook Secret

When configuring a webhook in Razorpay Dashboard, we define a secret.

Example

```
streamhub-payment-webhook-secret
```

This secret is known only to

- Razorpay
- Our Backend

The frontend never knows this secret.

The secret is stored securely in our backend configuration.

```
razorpay.webhook-secret
```

---

# 12. Signature Verification

Every webhook request contains

```
X-Razorpay-Signature
```

This signature is generated using

- Webhook Payload
- Webhook Secret
- HMAC SHA256

Our backend performs the same calculation.

If both signatures match,

```
Request came from Razorpay
```

Otherwise,

```
Reject Request
```

This prevents

- Fake webhook requests
- Data tampering
- Unauthorized payment updates

---

# 13. Why Signature Verification Is Mandatory

Without verification,

anyone could send

```json
{
    "event":"payment.captured"
}
```

and our backend might update

```
Payment

SUCCESS
```

without an actual payment.

Signature verification ensures

- Data integrity
- Authenticity
- Secure communication

---

# 14. Webhook Events Used

Our current implementation supports four Razorpay events.

| Event | Description | Our Action |
|--------|-------------|------------|
| payment.captured | Payment successfully captured | Complete Payment |
| payment.failed | Payment failed | Mark Payment as FAILED |
| payment.authorized | Payment authorized | Log only |
| order.paid | Razorpay Order marked as Paid | Log only |

---

# 15. Why Only These Events?

Razorpay provides many webhook events.

Examples

- refund.processed
- refund.failed
- settlement.processed
- payout.processed
- subscription.activated
- dispute.created

Our current Payment Service only supports the payment lifecycle.

Additional events will be implemented in future phases.

---

# 16. Auto Capture vs Manual Capture

Our current implementation uses **Auto Capture**.

In Auto Capture,

Razorpay automatically captures the payment immediately after authorization.

Therefore,

```
payment.authorized
```

is only an intermediate event.

The important business event is

```
payment.captured
```

Future versions of the Payment Service may support Manual Capture.

In Manual Capture,

the merchant decides when to capture the payment after receiving the

```
payment.authorized
```

event.

---

# 17. Webhook Processing Pipeline

Every webhook received by our backend follows the same processing pipeline.

```
Receive Webhook
        │
        ▼
Verify Signature
        │
        ▼
Parse JSON Payload
        │
        ▼
Identify Event
        │
        ▼
Route Event
        │
        ▼
Execute Business Logic
        │
        ▼
Return HTTP 200
```

This pipeline is common for all supported webhook events.

---

# 18. Current Implementation Summary

The StreamHub Payment Service currently performs the following tasks.

- Receives Razorpay webhook requests.
- Verifies webhook signature.
- Rejects invalid requests.
- Parses webhook payload.
- Identifies webhook event.
- Routes event to the appropriate handler.
- Processes payment success.
- Processes payment failure.
- Logs informational events.
- Returns HTTP 200 after successful processing.

This implementation provides a secure and reliable webhook processing mechanism suitable for production environments.

---

# 19. Complete Webhook Processing Flow

The following diagram represents the complete webhook lifecycle implemented in the StreamHub Payment Service.

```
Customer
      │
      ▼
Frontend
      │
      ▼
Razorpay Checkout
      │
      ▼
Customer Completes Payment
      │
      ▼
Razorpay Processes Payment
      │
      ▼
Webhook Triggered
      │
      ▼
POST /api/v1/webhooks/razorpay
      │
      ▼
Webhook Controller
      │
      ▼
Webhook Service
      │
      ▼
Verify Webhook Signature
      │
      ▼
Parse JSON Payload
      │
      ▼
Identify Event
      │
      ▼
Route Event
      │
      ▼
Business Logic
      │
      ▼
Update Database
      │
      ▼
Return HTTP 200
```

Every webhook follows the above processing pipeline.

---

# 20. Webhook Controller Flow

The controller is responsible only for receiving the incoming webhook request.

Responsibilities

- Receive HTTP POST request.
- Read the webhook signature.
- Read the raw JSON payload.
- Build the verification request object.
- Delegate processing to the service layer.

The controller contains no business logic.

```
Razorpay

↓

POST Request

↓

Webhook Controller

↓

Webhook Service
```

Keeping the controller lightweight improves readability and follows the Single Responsibility Principle.

---

# 21. Signature Verification Flow

The first responsibility of the Webhook Service is verifying the request.

Processing Steps

```
Receive Request

↓

Read Payload

↓

Read Signature Header

↓

Read Webhook Secret

↓

Generate HMAC SHA256

↓

Compare Signatures

↓

Valid ?

      │

 Yes

↓

Continue Processing


---------------------

No

↓

Return HTTP 400
```

If the signatures do not match, processing stops immediately.

No database operations are performed.

This prevents unauthorized webhook requests.

---

# 22. Payload Parsing Flow

After verification succeeds, the payload is converted into a JSON object.

```
Raw JSON

↓

ObjectMapper

↓

JsonNode

↓

Extract Event

↓

Route Event
```

If the payload is malformed,

the webhook is rejected with HTTP 400.

---

# 23. Event Routing

After parsing the payload, the application identifies the webhook event.

```
payment.captured

↓

handlePaymentCaptured()


------------------------

payment.failed

↓

handlePaymentFailed()


------------------------

payment.authorized

↓

handlePaymentAuthorized()


------------------------

order.paid

↓

handleOrderPaid()
```

Each event has its own handler method.

This keeps the implementation modular and easy to maintain.

---

# 24. payment.captured Event

This is the most important webhook event in the payment lifecycle.

Meaning

> Razorpay has successfully captured the customer's payment.

This is the event that marks a payment as successful in our application.

---

## Processing Steps

The application performs the following operations.

### Step 1

Extract

- Gateway Order Id
- Gateway Payment Id

from the webhook payload.

---

### Step 2

Find the Payment using

```
gatewayOrderId
```

---

### Step 3

Fetch the latest payment details from Razorpay.

This provides

- Payment Method
- Gateway Status
- Complete Gateway Response

The gateway becomes the trusted source of truth.

---

### Step 4

Update Payment.

The following fields are updated.

- Gateway Payment Id
- Gateway Signature
- Payment Method
- Gateway Status
- Gateway Response
- Paid At
- Payment Status = SUCCESS

---

### Step 5

Update Payment Intent.

```
Payment Intent

↓

COMPLETED
```

---

### Step 6

Create Payment Event.

```
PAYMENT_SUCCESS
```

This creates an immutable audit trail.

---

### Step 7

Return HTTP 200.

The webhook has now been processed successfully.

```text
Find Payment

↓

Already SUCCESS?

↓

Yes

↓

Ignore duplicate webhook

↓

Return HTTP 200

No

↓

Complete Payment
```
---

# 25. Database Changes for payment.captured

Payment Table

```
Status

CREATED

↓

SUCCESS
```

Payment Intent Table

```
Status

CREATED

↓

COMPLETED
```

Payment Event Table

```
New Row

PAYMENT_SUCCESS
```

---

# 26. payment.failed Event

Meaning

> Razorpay attempted the payment but the payment failed.

The customer may retry using another payment method.

---

## Processing Steps

### Step 1

Extract

- Gateway Order Id
- Gateway Payment Id

---

### Step 2

Find the Payment.

---

### Step 3

Fetch the latest payment details from Razorpay.

---

### Step 4

Update Payment.

The following fields are updated.

- Gateway Payment Id
- Payment Method
- Gateway Status
- Gateway Response
- Payment Status = FAILED

---

### Step 5

Create Payment Event.

```
PAYMENT_FAILED
```

---

### Step 6

Do NOT update Payment Intent.

Reason

The customer may retry the payment.

Therefore

```
Payment Intent

CREATED
```

remains unchanged.

---

### Step 7

Return HTTP 200.

---

# 27. Database Changes for payment.failed

Payment Table

```
Status

CREATED

↓

FAILED
```

Payment Intent Table

```
No Change

CREATED
```

Payment Event Table

```
New Row

PAYMENT_FAILED
```

---

### Refund Webhook Flow

refund.processed

↓

Verify Signature

↓

Find Refund

↓

Fetch Refund Details

↓

Update Refund Status

↓

Create REFUND_SUCCESS Event

↓

Return HTTP 200

And similarly for:

refund.failed

↓

Update Refund Status = FAILED

↓

Create REFUND_FAILED Event

---

# 28. payment.authorized Event

Meaning

> The customer's bank has approved the payment.

In our implementation,

Auto Capture is enabled.

Therefore,

this event is only informational.

The payment will immediately proceed to

```
payment.captured
```

---

## Processing Steps

The application performs the following.

- Verify Signature.
- Parse Payload.
- Extract Gateway IDs.
- Write logs.

No database changes are performed.

No payment event is created.

---

## Why?

The payment will soon be captured automatically.

Updating the database here would create unnecessary intermediate states.

Future versions supporting Manual Capture will implement additional business logic here.

---

# 29. order.paid Event

Meaning

> The Razorpay Order has been completely paid.

This event is generated after Razorpay marks the Order as PAID.

---

## Processing Steps

The application performs the following.

- Verify Signature.
- Parse Payload.
- Extract Gateway Order Id.
- Write logs.

No database changes are performed.

---

## Why?

The payment has already been completed when

```
payment.captured
```

was processed.

Therefore,

Payment

```
SUCCESS
```

Payment Intent

```
COMPLETED
```

already exist.

There is nothing left to update.

---

# 30. Database Summary

The following table summarizes database updates performed for each supported webhook.

| Webhook Event      | Payment    | Refund      | Event           |
|--------------------|------------|-------------|-----------------|
| payment.captured   | SUCCESS    | —           | PAYMENT_SUCCESS |
| payment.failed     | FAILED     | —           | PAYMENT_FAILED  |
| refund.processed   | —          | SUCCESS     | REFUND_SUCCESS  |
| refund.failed      | —          | FAILED      | REFUND_FAILED   |
| payment.authorized | No Change  | No Change   | No Change       |
| order.paid         | No Change  | No Change   | No Change       |

This table represents the current implementation of the StreamHub Payment Service webhook processing.


# 31. Error Handling

The webhook endpoint validates every incoming request before processing any business logic.

| Scenario          | Result                |
| ----------------- | --------------------- |
| Invalid signature | HTTP 400              |
| Malformed payload | HTTP 400              |
| Payment not found | Log + reject          |
| Refund not found  | Log + reject          |
| Duplicate webhook | Ignore and return 200 |
| Database failure  | Rollback + 500        |

The following validations are performed.

## Invalid Signature

If the webhook signature verification fails,

```
HTTP 400 Bad Request
```

is returned.

Reason

The request cannot be trusted because it may have been tampered with or may not have originated from Razorpay.

---

## Invalid Payload

If the JSON payload cannot be parsed,

```
HTTP 400 Bad Request
```

is returned.

Reason

The request body is malformed and cannot be processed.

---

## Resource Not Found

If the corresponding payment cannot be found,

the request is rejected and logged for further investigation.

---

## Internal Server Error

Unexpected exceptions such as

- Database failure
- Network issues
- External service failure

result in

```
HTTP 500 Internal Server Error
```

These errors should be investigated through application logs.

---

# 32. Idempotency

Webhook delivery is not guaranteed to occur only once.

If Razorpay does not receive an HTTP 200 response, it retries the webhook.

Example

```
Razorpay

↓

payment.captured

↓

Our Server

↓

Database Updated

↓

Network Timeout

↓

Razorpay Did Not Receive HTTP 200

↓

Retry

↓

payment.captured Again
```

Therefore webhook processing must be idempotent.

Processing the same webhook multiple times should not create duplicate business operations.

Current implementation prevents duplicate processing by checking the current payment status before updating the database.

Example

```
Payment Status

SUCCESS

↓

Ignore Duplicate payment.captured
```

This ensures the payment is completed only once.

---

# 33. Logging Strategy

The webhook implementation uses structured logging throughout the processing pipeline.

Typical logs include

```
Received Razorpay webhook.

↓

Webhook signature verified successfully.

↓

Processing payment.captured.

↓

Payment updated successfully.

↓

Payment Event recorded successfully.
```

Sensitive information such as

- Razorpay Secret
- Webhook Secret
- API Keys

must never be written to application logs.

---

# 34. Transaction Management

Database updates are performed within transactional boundaries.

The following operations execute inside a single transaction.

- Update Payment
- Update Payment Intent
- Create Payment Event

If any operation fails,

the entire transaction is rolled back.

This guarantees data consistency.

---

# 35. Security Considerations

The webhook endpoint is publicly accessible.

Therefore several security measures are implemented.

## Signature Verification

Every webhook request is verified using

HMAC SHA256

before processing.

---

## Secret Storage

Webhook Secret is stored securely in application configuration.

It is never exposed to

- Frontend
- API Response
- Browser

---

## HTTPS

In production,

the webhook endpoint should always be exposed over HTTPS.

---

## Request Validation

Every request validates

- Signature
- Payload
- Required Fields

before executing business logic.

---

# 36. Testing Strategy

The webhook implementation should be tested using Razorpay Test Mode.

Recommended test scenarios.

## payment.captured

Expected Result

- Payment Status → SUCCESS
- Payment Intent → COMPLETED
- Payment Event Created

---

## payment.failed

Expected Result

- Payment Status → FAILED
- Payment Intent Unchanged
- Payment Event Created

---

## payment.authorized

Expected Result

- Logs Generated
- No Database Updates

---

## order.paid

Expected Result

- Logs Generated
- No Database Updates

### refund.processed

Expected

- Refund Status → SUCCESS
- Refund Event Created

### refund.failed

Expected

- Refund Status → FAILED
- Refund Event Created

---

## Invalid Signature

Expected Result

```
HTTP 400
```

---

## Invalid Payload

Expected Result

```
HTTP 400
```

---

## Duplicate Webhook

Expected Result

No duplicate database updates.

---

# 37. Production Considerations

The following improvements should be considered before deploying to production.

- Monitoring and alerting.
- Retry handling.
- Audit logging.
- Rate limiting.
- Secure secret management.
- Metrics collection.
- Centralized logging.
- Distributed tracing.
- Correlation IDs 
- Dead-letter queue (if asynchronous processing is introduced later)
- Webhook retry monitoring 
- Alerting for repeated webhook failures 
- Metrics for processing latency

These improvements increase reliability and operational visibility.

---

# 38. Future Enhancements

The current implementation focuses only on the payment lifecycle.

Future webhook support may include

- Settlement Events
- Payout Events
- Subscription Events
- Dispute Events

Future payment enhancements

- Manual Capture
- Settlement Reconciliation
- Gateway Fee Tracking
- Net Settlement Amount
- Merchant Account Details
- Fraud Detection
- Payment Analytics
- Multi-Gateway Support

---

# 39. Key Takeaways

The StreamHub Payment Service uses Razorpay Webhooks to maintain payment synchronization between Razorpay and our database.

The webhook implementation provides

- Secure signature verification
- Automatic event processing
- Reliable payment synchronization
- Production-ready architecture
- Extensible event handling
- Clear separation of responsibilities

Currently supported webhook events

* payment.captured
* payment.failed
* payment.authorized
* order.paid
* refund.processed
* refund.failed

The webhook implementation ensures that payment status remains accurate even if the frontend fails to notify the backend.

This makes the payment service more reliable, fault tolerant, and suitable for production environments.

---

## Architecture Decisions

• Webhook processing is idempotent.
• Business logic is isolated from Razorpay behind the Gateway Layer.
• Payment events and refund events are immutable.
• Signature verification occurs before any database operation.
• Business entities are updated within a single transaction.

# 40. Conclusion

Webhook integration is an essential component of every modern payment system.

Instead of depending on the frontend to notify payment completion, Razorpay directly informs our backend whenever a configured payment event occurs.

By combining

- Checkout Verification
- Secure Webhook Verification
- Event-Based Processing

the StreamHub Payment Service achieves reliable, secure, and production-ready payment processing.

The webhook architecture implemented in this phase establishes a strong foundation for future payment capabilities such as settlements, refunds, disputes, payouts, subscriptions, and multi-gateway integrations.