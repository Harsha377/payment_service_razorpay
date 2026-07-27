# Webhook Processing

## Overview

Razorpay sends webhook events whenever the payout status changes.

Our application consumes these webhook events to keep the local database synchronized with Razorpay.

Instead of polling Razorpay continuously, the platform relies on webhook notifications to update payout status, store gateway responses, and create immutable audit events.

---

# Why Webhooks?

When a payout is created, Razorpay immediately accepts the request but the bank transfer happens asynchronously.

Example:

Customer Withdrawal
│
▼
Create Payout API
│
▼
Razorpay Accepts Request
│
▼
Status = queued
│
▼
Bank Processing
│
▼
Webhook Sent
│
▼
Application Updates Database

Without webhooks, the application would never know when the payout was actually completed.

---

# Supported Webhook Events

| Razorpay Event | Internal Status | Description |
|---------------|----------------|-------------|
| payout.pending | PENDING | Payout accepted but waiting for processing |
| payout.processing | PROCESSING | Bank transfer has started |
| payout.processed | PROCESSED | Money successfully transferred |
| payout.failed | FAILED | Transfer failed |
| payout.reversed | REVERSED | Money returned after processing |

---

# Webhook Flow

                    Razorpay
                        │
                        │
             Sends Webhook Event
                        │
                        ▼
              Webhook Controller
                        │
                        ▼
             Verify Signature
                        │
                        ▼
              Route Event
                        │
                        ▼
        Extract Gateway Payout Id
                        │
                        ▼
           Find Payout From Database
                        │
                        ▼
          Duplicate Webhook Check
                        │
            ┌───────────┴────────────┐
            │                        │
         Duplicate                New Event
            │                        │
            ▼                        ▼
        Return 200          Validate Status Transition
                                     │
                                     ▼
                         Update Payout Status
                                     │
                                     ▼
                    Store Latest Gateway Response
                                     │
                                     ▼
                     Insert Payout Event Record
                                     │
                                     ▼
                               Return 200 OK

---

# Duplicate Webhook Handling

## Why Duplicate Webhooks Occur

Webhook delivery is not guaranteed to happen exactly once.

If Razorpay does not receive a successful HTTP 200 response from our application, it automatically retries the webhook.

Examples:

- Application crashed
- Network timeout
- Database temporarily unavailable
- Reverse proxy timeout
- Connection reset

Because of this, the same webhook can arrive multiple times.

Example:

Razorpay

↓

payout.processed

↓

Retry

↓

Retry

↓

Retry

All webhooks contain the same Gateway Payout Id.

---

# How We Handle Duplicate Webhooks

Before updating the payout, we compare the current payout status with the incoming webhook status.

Example:

Database

Status = PROCESSED

Incoming Webhook

Status = PROCESSED

↓

Duplicate

↓

Ignore

↓

Return HTTP 200

This prevents:

- Duplicate database updates
- Duplicate payout events
- Incorrect audit history

---

# Duplicate Detection Logic

if (payout.getStatus() == expectedStatus) {

    log.info("Duplicate webhook ignored.");

    return;
}

---

# Status Transition Validation

Not every status transition is valid.

Example:

CREATED
│
▼
PROCESSING
│
▼
PROCESSED

Once a payout reaches PROCESSED, it should never move back to PROCESSING.

Invalid Example:

PROCESSED

↓

PROCESSING

↓

Rejected

The application validates every transition before updating the database.

This protects data integrity even if invalid webhook events are received.

---

# Database Updates

When a webhook is processed successfully, the application updates:

## payouts

Updated Fields

- status
- gateway_response
- failure_reason
- utr
- fees
- tax
- processed_at
- failed_at
- reversed_at

---

## payout_events

A new immutable event is inserted for every valid webhook.

Example

PAYOUT_CREATED

↓

PAYOUT_PENDING

↓

PAYOUT_PROCESSING

↓

PAYOUT_PROCESSED

No existing event is ever modified.

---

# Event History

The event table provides a complete audit trail.

Example

Payout Created

↓

Pending

↓

Processing

↓

Processed

Every transition is permanently recorded.

This helps with:

- Auditing
- Compliance
- Customer support
- Financial reconciliation
- Debugging

---

# Why Store Gateway Response?

The complete webhook payload is stored as JSON.

Benefits

- Easy debugging
- Future compatibility
- Audit history
- Bank investigation
- Customer support

Example

gateway_response

{

"event": "payout.processed",

"payload": {

      ...

}

}

---

# Webhook Idempotency

Webhook processing is idempotent.

Meaning

Receiving the same webhook multiple times produces exactly the same final result.

Example

Incoming Webhooks

payout.processed

payout.processed

payout.processed

Database

Status

PROCESSED

Only one update occurs.

---

# Production Benefits

Our webhook implementation provides:

✓ Signature Verification

✓ Duplicate Detection

✓ Status Transition Validation

✓ Immutable Event History

✓ Gateway Payload Storage

✓ Audit Logging

✓ Automatic Synchronization with Razorpay

✓ Idempotent Processing

✓ Financial Traceability

✓ Production-Ready Design