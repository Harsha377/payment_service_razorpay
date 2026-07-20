# Payment Intent Flow

## 1. Overview

The **Payment Intent Flow** is the first stage of the synchronous Pay-In lifecycle.

Its responsibility is to initialize a payment by creating the required business and gateway resources before the customer interacts with the payment gateway.

This flow prepares the payment for checkout but **does not collect money**. Payment collection and confirmation occur during the Payment Verification and Webhook flows.

During this phase, the Payment Service:

- Validates the payment request.
- Creates a Payment Intent.
- Creates a Razorpay Order.
- Creates a Payment record.
- Records the initial payment event.
- Returns all information required by the frontend to launch Razorpay Checkout.

At the end of this flow, **no money has been collected yet**. The payment is only initialized.

---

# 2. Objectives

The Payment Intent Flow is responsible for:

- Receiving a payment request.
- Validating business data.
- Creating a business Payment Intent.
- Creating a gateway Order.
- Persisting payment information.
- Recording the first payment event.
- Returning gateway information required for Checkout.
- Maintain transactional consistency.
- Support future payment retries.

---

# 3. High-Level Flow

```text
                 Client
      │
      ▼
POST /payment-intents
      │
      ▼
Begin Transaction
      │
      ▼
Validate Request
      │
      ▼
Create Payment Intent
      │
      ▼
Save Payment Intent
      │
      ▼
Create Razorpay Order
      │
      ▼
Create Payment
      │
      ▼
Save Payment
      │
      ▼
Record PAYMENT_CREATED Event
      │
      ▼
Commit Transaction
      │
      ▼
Return Response
```

---

# 4. Request

Endpoint

```http
POST /api/v1/payment-intents
```

Example Request

```json
{
  "orderReference": "01K0ORDER00000000000000001",
  "customerReference": "01K0CUSTOMER000000000001",
  "amount": 10000,
  "currency": "INR",
  "description": "Premium Subscription",
  "metadata": {
    "plan": "PREMIUM",
    "coupon": "WELCOME10"
  }
}
```

---

# 5. Request Validation

Before creating any records, the request is validated.

Validation Rules

| Field | Validation |
|---------|------------|
| orderReference | Required |
| customerReference | Required |
| amount | Greater than zero |
| currency | Three uppercase ISO characters |
| description | Optional |

If validation fails, the request is rejected before any database operation occurs.
Metadata → Optional JSON object

---

# 6. Step 1 – Create Payment Intent

A new Payment Intent is created.
Amount is stored in the smallest currency unit (paise).

Business Reference

```text
paymentIntentReference

↓

Generated using ULID
```

Status

```text
CREATED
```

Stored information

- Order Reference
- Customer Reference
- Amount
- Currency
- Description
- Metadata
- Expiration Time

The Payment Intent represents the business request.

---

# 7. Step 2 – Persist Payment Intent

created_at and updated_at are automatically populated using Spring Data JPA Auditing.
The Payment Intent is stored in

```text
payment_intents
```

Initial Status

```text
CREATED
```

At this point the business request exists in the database.

---

# 8. Step 3 – Create Razorpay Order

The Payment Service delegates gateway communication.

```text
PaymentIntentService

↓

RazorpayService

↓

Create Order

↓

Razorpay API
```

The service sends

- Amount (Paise)
- Currency
- Receipt (Payment Reference)

Razorpay returns

- Gateway Order ID
- Gateway Status
- Gateway Name
- Public Gateway Key

No payment has been collected at this stage.

Only an Order has been created.

---

# 9. Step 4 – Create Payment Record

A Payment entity is created.
payment_intent_id establishes the relationship with the business request.

Initial values

```text
Gateway Name

↓

RAZORPAY

Status

↓

CREATED
```

Stored information

- Payment Reference
- Payment Intent
- Gateway Name
- Gateway Order ID
- Amount
- Currency

The following fields remain empty because payment has not yet been completed.

- Gateway Payment ID
- Gateway Signature
- Payment Method
- Gateway Status
- Gateway Response
- Paid At

These fields are populated during Payment Verification.

---

# 10. Step 5 – Persist Payment

The Payment entity is stored in

```text
payments
```

Relationship

```text
Payment Intent

1

↓

Payment
```

This establishes the link between the business request and the gateway transaction.

---

# 11. Step 6 – Record Payment Event

Payment events are append-only records and are never updated. 
Every state transition creates a new event, providing a complete audit trail.
The first payment event is created.

Event

```text
PAYMENT_CREATED
```

Source

```text
SYSTEM
```

Stored in

```text
payment_events
```

Purpose

Maintain a complete audit trail of payment lifecycle events.

---

# 12. Step 7 – Build Response

The gatewayKey returned is Razorpay's public Key ID.
The secret key is never exposed outside the backend.

The backend constructs a response for the frontend.

Example

```json
{
    "paymentIntentReference": "...",
    "paymentReference": "...",
    "gatewayName": "RAZORPAY",
    "gatewayKey": "rzp_test_xxxxx",
    "gatewayOrderId": "order_xxxxx",
    "amount": 10000,
    "currency": "INR",
    "status": "CREATED"
}
```

The frontend uses

- Gateway Key
- Gateway Order ID

to launch Razorpay Checkout.

---

# 13. Transaction Flow

Database operations execute within a single transaction.

The call to Razorpay is an external network operation and cannot participate in the database transaction. If the gateway call succeeds but a later database operation fails, compensating actions or operational handling may be required.

```text
Create Payment Intent

↓

Save Payment Intent

↓

Create Payment

↓

Save Payment

↓

Create Payment Event

↓

Commit
```

If any database operation fails

```text
Rollback
```

No partial data remains in the database.

Gateway order creation is an external operation and is not part of the database transaction.

---

# 14. Database Changes

## payment_intents

```text
Status

CREATED
```

---

## payments

```text
Status

CREATED
```

payment_intents.updated_at

↓

Automatically managed

Gateway fields

```text
gatewayOrderId

↓

Available

gatewayPaymentId

↓

NULL

gatewaySignature

↓

NULL

paymentMethod

↓

NULL

gatewayStatus

↓

NULL

gatewayResponse

↓

NULL

paidAt

↓

NULL
```

---

## payment_events

```text
PAYMENT_CREATED
```

---

# 15. Why Separate Payment Intent and Payment?

The Payment Intent represents the business request.

The Payment represents the gateway transaction.

Supports asynchronous webhook processing.

Example

```text
Customer wants to pay ₹100

↓

Payment Intent

↓

Gateway Transaction

↓

Payment
```

This separation allows future support for

- Retry attempts
- Multiple gateways
- Recovery after gateway failures

without changing the business request.

---

# 16. Design Decisions

## Why create Payment Intent first?

The business request should exist before contacting the payment gateway.

If the gateway is temporarily unavailable, the request is still traceable.

### Why store amount in paise?
All monetary values are stored in the smallest currency unit (paise).

Benefits

- Prevents floating-point precision issues.
- Matches Razorpay API requirements.
- Simplifies financial calculations.
- Provides consistent handling across payment and refund modules.

### Why use ULIDs?
Business references use ULIDs instead of database IDs.

Benefits

- Globally unique.
- Time sortable.
- URL safe.
- Prevents exposing internal database identifiers.

---

## Why create a Payment immediately?

A Payment record reserves a gateway transaction for the Payment Intent.

Future verification updates this same record instead of creating another one.

---

## Why create PAYMENT_CREATED event?

The service records every important action.

This creates an immutable audit trail.

Future events include

```text
PAYMENT_SUCCESS

PAYMENT_FAILED

PAYMENT_CAPTURED

PAYMENT_REFUNDED
```

---

## Why return Gateway Key?

The Razorpay Checkout SDK executes in the browser.

It requires the public Gateway Key to identify the merchant account.

The backend returns the public key while keeping the secret key private.

---

# 17. Current Flow Status

Implemented

- Request Validation
- Payment Intent Creation
- ULID Generation
- Payment Intent Persistence
- Razorpay Order Creation
- Payment Creation
- Payment Persistence
- Payment Event Recording
- API Response Construction 
- Metadata Support 
- Production Logging 
- Manual End-to-End Testing

Verified

- Database records created successfully
- Razorpay Test Order created successfully
- Frontend successfully launches Razorpay Checkout
- Complete Payment Initialization flow tested successfully

---

# 18. Related Flows

The Payment Intent Flow represents the initialization stage of the payment lifecycle.

The complete payment lifecycle consists of:

1. Payment Intent Flow
2. Payment Verification Flow
3. Payment Webhook Processing
4. Refund Flow
5. Refund Webhook Processing

Together these flows provide the complete Pay-In and Refund lifecycle of the StreamHub Payment Service.

### Failure Scenarios
Document what happens when something goes wrong.

Example:

| Scenario                      | Result                                 |
| ----------------------------- | -------------------------------------- |
| Validation fails              | 400 Bad Request                        |
| Razorpay order creation fails | Payment Intent transaction rolled back |
| Database save fails           | Transaction rolled back                |
| Duplicate order reference     | 409 Conflict                           |
| Gateway unavailable           | 500 Internal Server Error              |


# 19. Next Phase

The Payment Intent Flow ends after successfully creating the Razorpay Order.

The next phase is **Payment Verification**, where:

```text
Customer Completes Payment

↓

Frontend Receives

Gateway Order ID

Gateway Payment ID

Gateway Signature

↓

Verify Payment

↓

Verify Signature

↓

Fetch Payment Details

↓

Update Payment

↓

Update Payment Intent

↓

Create PAYMENT_SUCCESS Event
```

Together, the Payment Intent Flow and Payment Verification Flow form the complete synchronous Pay-In lifecycle of the StreamHub Payment Service.