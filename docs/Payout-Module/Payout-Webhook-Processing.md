# Webhook Processing

## Overview

Razorpay sends webhook events whenever the payout status changes.

Our application consumes these webhook events to synchronize both the **Payout Module** and the **Wallet Module** with Razorpay.

A webhook is the final confirmation of what actually happened to a payout.

Based on the received event, the application:

- Updates the payout status.
- Stores the latest gateway response.
- Creates an immutable payout event.
- Releases the wallet hold for successful payouts.
- Cancels the wallet hold for failed or reversed payouts.

Instead of polling Razorpay continuously, the platform relies on webhook notifications to keep both the payout and wallet state consistent.
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

| Razorpay Event    | Internal Status | Description                                |
|-------------------|-----------------|--------------------------------------------|
| payout.pending    | PENDING         | Payout accepted but waiting for processing |
| payout.processing | PROCESSING      | Bank transfer has started                  |
| payout.processed  | PROCESSED       | Money successfully transferred             |
| payout.failed     | FAILED          | Transfer failed                            |
| payout.reversed   | REVERSED        | Money returned after processing            |

---

# Webhook Flow

                    Razorpay
                        │
                        ▼
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
             ┌──────────┴──────────┐
             │                     │
        Duplicate              New Event
             │                     │
             ▼                     ▼
      Return HTTP 200     Validate Status Transition
                                   │
                                   ▼
                         Update Payout Status
                                   │
                                   ▼
                      Store Gateway Response
                                   │
                                   ▼
                        Create Payout Event
                                   │
                                   ▼
                     Find Wallet Hold By Payout
                                   │
                     ┌─────────────┴─────────────┐
                     ▼                           ▼
              PROCESSED                     FAILED
                                            CANCELLED
                                            REVERSED
                     │                           │
                     ▼                           ▼
         Release Wallet Hold          Cancel Wallet Hold
                     │                           │
                     ▼                           ▼
                    Return HTTP 200 OK

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

# Wallet Synchronization

The webhook is responsible not only for updating the payout but also for completing the wallet lifecycle.

The wallet hold remains ACTIVE until Razorpay confirms the final payout status.

Depending on the webhook event:

| Webhook Status | Wallet Action       |
|----------------|---------------------|
| PENDING        | No action           |
| PROCESSING     | No action           |
| PROCESSED      | Release Wallet Hold |
| FAILED         | Cancel Wallet Hold  |
| CANCELLED      | Cancel Wallet Hold  |
| REVERSED       | Cancel Wallet Hold  |

This guarantees that beneficiary balances always match the actual payout status.

# Successful Payout Flow

```
Customer Requests Payout
        │
        ▼
Create Wallet Hold
        │
        ▼
Create Payout
        │
        ▼
Create Wallet Transaction
        │
        ▼
Webhook (PROCESSED)
        │
        ▼
Update Payout
        │
        ▼
Create Payout Event
        │
        ▼
Release Wallet Hold
        │
        ▼
Payout Completed
```

# Failed Payout Flow

```
Customer Requests Payout
        │
        ▼
Create Wallet Hold
        │
        ▼
Create Payout
        │
        ▼
Create Wallet Transaction
        │
        ▼
Webhook (FAILED)
        │
        ▼
Update Payout
        │
        ▼
Create Payout Event
        │
        ▼
Cancel Wallet Hold
        │
        ▼
Money Returned To Available Balance
```

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

## wallet_holds

Updated Fields

- status
- updated_at

Status Changes

ACTIVE

↓

RELEASED

or

ACTIVE

↓

CANCELLED

---

## wallets

Updated Fields

- available_balance
- pending_balance
- total_debited (only for successful payouts)

These updates ensure that the beneficiary's wallet accurately reflects the outcome of the payout.

# Webhook Status Mapping

| Razorpay Status | Payout Status | Wallet Hold |
|-----------------|---------------|-------------|
| pending         | PENDING       | No Change   |
| processing      | PROCESSING    | No Change   |
| processed       | PROCESSED     | RELEASED    |
| failed          | FAILED        | CANCELLED   |
| cancelled       | CANCELLED     | CANCELLED   |
| reversed        | REVERSED      | CANCELLED   |

# Why Doesn't the Wallet Update During Payout Creation?

The platform cannot assume that a payout will succeed immediately after it is created.

At payout creation time:

- Razorpay has only accepted the request.
- The bank transfer has not yet completed.

Updating the wallet immediately could permanently deduct money for a payout that later fails.

Instead, the platform creates a wallet hold and waits for the webhook.

Only the webhook represents the final outcome of the payout.

This approach guarantees financial consistency and prevents accidental loss of beneficiary funds.

# Benefits

The webhook processing design provides the following guarantees:

- Keeps payout status synchronized with Razorpay.
- Prevents duplicate webhook processing.
- Maintains an immutable payout event history.
- Synchronizes wallet balances with payout status.
- Automatically releases wallet holds after successful payouts.
- Automatically restores beneficiary balances after failed payouts.
- Ensures that wallet balances always match the actual bank transfer outcome.
- Provides complete financial traceability for auditing and reconciliation.


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