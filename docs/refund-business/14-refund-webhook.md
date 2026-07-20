# Refund Webhook Integration

## 1. Overview

The **Refund Webhook Flow** enables the StreamHub Payment Service to receive asynchronous refund notifications directly from Razorpay.

While the Refund API creates a refund request, the actual refund processing is completed asynchronously by Razorpay. Webhooks ensure that the backend remains synchronized with the gateway, even if the merchant never queries the refund status.

During this flow, the Payment Service:

- Receives refund webhook events.
- Verifies the webhook signature.
- Parses the webhook payload.
- Updates the refund status.
- Records refund events.
- Returns an acknowledgment to Razorpay.

---

# 2. Objectives

The Refund Webhook Flow is responsible for:

- Synchronizing refund status with Razorpay.
- Verifying webhook authenticity.
- Preventing duplicate processing.
- Recording refund lifecycle events.
- Maintaining an immutable audit trail.

---

# 3. Supported Refund Events

The current implementation supports:

```text
refund.processed

refund.failed
```

Future webhook events may include:

```text
refund.created

refund.pending

refund.reversed
```

---

# 4. High-Level Flow

```text
Razorpay

↓

Refund Event

↓

POST /api/v1/webhooks/razorpay

↓

WebhookController

↓

WebhookService

↓

Verify Signature

↓

Parse Payload

↓

Route Event

↓

Process Refund Event

↓

Update Refund

↓

Create Refund Event

↓

Return HTTP 200
```

---

# 5. Endpoint

```http
POST /api/v1/webhooks/razorpay
```

---

# 6. Request Headers

| Header | Required | Description |
|----------|----------|-------------|
| X-Razorpay-Signature | Yes | Webhook signature used for verification |

---

# 7. Request Body

Content-Type

```http
application/json
```

Example

```json
{
  "event": "refund.processed",
  "payload": {
    "refund": {
      "entity": {
        "id": "rfnd_ABC123",
        "payment_id": "pay_XYZ456",
        "amount": 50000,
        "status": "processed"
      }
    }
  }
}
```

---

# 8. Step 1 – Receive Webhook

Razorpay sends an HTTP POST request whenever a refund event occurs.

The webhook contains:

- Event Type
- Refund Payload
- Signature Header

---

# 9. Step 2 – Verify Signature

Before processing begins, the webhook signature is verified.

The following values are used:

- Request Body
- X-Razorpay-Signature
- Webhook Secret

Flow

```text
Webhook

↓

Verify Signature

↓

Valid?

↓

Yes

↓

Continue

↓

No

↓

HTTP 400
```

If verification fails

```text
Invalid Webhook Signature
```

is returned.

---

# 10. Step 3 – Route Event

The event type is extracted.

Example

```text
refund.processed
```

The service routes the event to the appropriate handler.

```text
WebhookService

↓

refund.processed

↓

handleRefundProcessed()
```

or

```text
WebhookService

↓

refund.failed

↓

handleRefundFailed()
```

---

# 11. refund.processed Flow

```text
Verify Signature

↓

Find Refund

↓

Already SUCCESS?

↓

Yes

↓

Ignore Duplicate

↓

Return HTTP 200

↓

No

↓

Fetch Refund Details

↓

Update Refund Status

↓

Create REFUND_SUCCESS Event

↓

Return HTTP 200
```

---

# 12. refund.failed Flow

```text
Verify Signature

↓

Find Refund

↓

Already FAILED?

↓

Yes

↓

Ignore Duplicate

↓

Return HTTP 200

↓

No

↓

Update Refund Status

↓

Create REFUND_FAILED Event

↓

Return HTTP 200
```

---

# 13. Idempotency

Webhook delivery is not guaranteed to occur only once.

Razorpay may retry webhook delivery if:

- Network failures occur.
- The server does not return HTTP 200.
- Request timeouts occur.

The Payment Service safely ignores duplicate events.

Example

```text
refund.processed

↓

Refund Already SUCCESS

↓

Ignore

↓

HTTP 200
```

This prevents duplicate database updates and duplicate event creation.

---

# 14. Database Changes

## refunds

Before

```text
PROCESSING
```

After

```text
SUCCESS
```

or

```text
FAILED
```

---

## refund_events

Before

```text
REFUND_REQUESTED
```

After

```text
REFUND_SUCCESS
```

or

```text
REFUND_FAILED
```

---

# 15. Transaction Flow

```text
Verify Signature

↓

Find Refund

↓

Update Refund

↓

Create Refund Event

↓

Commit
```

If any database operation fails

```text
Rollback
```

No partial updates remain.

---

# 16. Error Handling

| Scenario | Result |
|----------|--------|
| Invalid Signature | HTTP 400 |
| Refund Not Found | HTTP 404 |
| Duplicate Event | Ignore and Return HTTP 200 |
| Database Failure | HTTP 500 |

---

# 17. Testing Strategy

The following scenarios should be verified.

## refund.processed

Expected

- Refund Status Updated
- REFUND_SUCCESS Event Created

---

## refund.failed

Expected

- Refund Status Updated
- REFUND_FAILED Event Created

---

## Invalid Signature

Expected

```text
HTTP 400
```

---

## Duplicate Webhook

Expected

- No duplicate updates.
- No duplicate events.
- HTTP 200 returned.

---

## Refund Not Found

Expected

```text
HTTP 404
```

---

# 18. Design Decisions

## Why Verify Every Webhook?

The webhook originates from an external system.

Signature verification ensures that only genuine Razorpay requests are processed.

---

## Why Ignore Duplicate Events?

Webhook delivery is at-least-once.

Ignoring duplicate events makes processing idempotent and prevents inconsistent data.

---

## Why Record Refund Events?

Refund events provide:

- Audit history
- Operational visibility
- Easier troubleshooting
- Future event-driven integrations

---

## Why Keep Refund Flow Separate?

Refund creation and refund completion are separate business operations.

The Refund API initiates the process.

The Refund Webhook confirms the final gateway state.

---

# 19. Current Implementation

Implemented

- Webhook Endpoint
- Signature Verification
- Event Routing
- refund.processed
- refund.failed
- Refund Status Updates
- Refund Event Recording
- Transaction Management
- Idempotent Processing

Verified

- Razorpay Refund Webhooks
- Duplicate Webhook Handling
- Refund Status Synchronization
- Refund Event Recording

---

# 20. Future Enhancements

Planned improvements include:

- Settlement Webhooks
- Payout Webhooks
- Dispute Webhooks
- Subscription Webhooks
- Dead Letter Queue Support
- Retry Monitoring
- Webhook Metrics
- Event Streaming

---

# 21. Related Flows

The Refund Webhook Flow completes the refund lifecycle.

```text
Payment Intent

↓

Payment Verification

↓

Payment Webhook

↓

Refund Flow

↓

Refund Webhook
```

---

# 22. Conclusion

The Refund Webhook Flow provides reliable asynchronous synchronization between Razorpay and the StreamHub Payment Service.

By verifying webhook signatures, processing events transactionally, recording immutable refund events, and safely handling duplicate deliveries, the service ensures that refund information remains accurate, auditable, and consistent with the payment gateway.

Together, the Refund Flow and Refund Webhook Flow provide a complete, production-ready refund lifecycle.