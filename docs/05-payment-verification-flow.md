# Payment Verification Flow

## 1. Overview

The **Payment Verification Flow** is the second stage of the payment lifecycle.

This phase begins **after the customer completes payment using Razorpay Checkout**.

Its primary responsibility is to verify that the payment is genuine before updating the business records.

During this phase, the Payment Service:

- Receives payment details from the frontend.
- Verifies the Razorpay payment signature.
- Fetches the actual payment details from Razorpay.
- Updates the Payment record.
- Updates the Payment Intent.
- Records the payment event.
- Returns the verification result.

At the end of this flow, the payment is considered **successfully completed**.

---

# 2. Objectives

The Payment Verification Flow is responsible for:

- Preventing payment fraud.
- Verifying the payment signature.
- Fetching trusted payment details directly from Razorpay.
- Updating payment status.
- Updating payment intent status.
- Recording payment events.
- Returning payment verification status.

---

# 3. High-Level Flow

```text
                    Customer

                        │

                        ▼

             Razorpay Checkout

                        │

                        ▼

             Payment Successful

                        │

                        ▼

           Razorpay Returns

        gatewayOrderId
        gatewayPaymentId
        gatewaySignature

                        │

                        ▼

          POST /payments/verify

                        │

                        ▼

             PaymentController

                        │

                        ▼

              PaymentService

                        │

                        ▼

        Find Payment by Order ID

                        │

                        ▼

      Validate Current Status

                        │

                        ▼

      Verify Razorpay Signature

                        │

                        ▼

    Fetch Payment From Razorpay

                        │

                        ▼

        Update Payment Record

                        │

                        ▼

     Update Payment Intent

                        │

                        ▼

 Create PAYMENT_SUCCESS Event

                        │

                        ▼

        Return Verification Response
```

---

# 4. Request

Endpoint

```http
POST /api/v1/payments/verify
```

Example Request

```json
{
    "paymentReference": "01KXXXXXXXXXXXXXXX",
    "gatewayOrderId": "order_Q2ABC123",
    "gatewayPaymentId": "pay_Q2XYZ456",
    "gatewaySignature": "xxxxxxxxxxxxxxxxxxxx"
}
```

---

# 5. Request Validation

Before verification begins, the request is validated.

Validation Rules

| Field | Validation |
|---------|------------|
| paymentReference | Required |
| gatewayOrderId | Required |
| gatewayPaymentId | Required |
| gatewaySignature | Required |

If validation fails, the verification process stops immediately.

---

# 6. Step 1 – Find Payment

The service loads the payment using the Gateway Order ID.

```text
gatewayOrderId

↓

PaymentRepository

↓

PaymentEntity
```

If the payment does not exist

```text
404 Not Found
```

is returned.

---

# 7. Step 2 – Validate Payment State

Before verifying the signature, the current payment state is validated.

Allowed state

```text
CREATED
```

Rejected states

```text
SUCCESS

FAILED

EXPIRED
```

This prevents duplicate or invalid verification requests.

---

# 8. Step 3 – Verify Razorpay Signature

The service delegates verification to the Gateway Layer.

```text
Payment Service

↓

RazorpayService

↓

Verify Payment Signature

↓

Razorpay SDK
```

The following values are verified:

- Gateway Order ID
- Gateway Payment ID
- Gateway Signature
- Razorpay Secret Key

The secret key never leaves the backend.

If verification fails

```text
400 Bad Request

Invalid Payment Signature
```

is returned.

---

# 9. Step 4 – Fetch Payment Details

After successful signature verification, the backend fetches the payment directly from Razorpay.

```text
Razorpay

↓

Fetch Payment

↓

Payment Details
```

Example information returned

- Payment Method
- Gateway Status
- Amount
- Currency
- Bank
- Wallet
- Email
- Contact
- Complete Gateway Response

This ensures the backend trusts Razorpay rather than the frontend.

---

# 10. Step 5 – Update Payment

The Payment record is updated.

Updated Fields

| Field | Value |
|--------|-------|
| gatewayPaymentId | Razorpay Payment ID |
| gatewaySignature | Razorpay Signature |
| paymentMethod | Retrieved from Razorpay |
| gatewayStatus | Retrieved from Razorpay |
| gatewayResponse | Complete Razorpay Response |
| status | SUCCESS |
| paidAt | Current Timestamp |

Payment Lifecycle

```text
CREATED

↓

SUCCESS
```

---

# 11. Step 6 – Update Payment Intent

The corresponding Payment Intent is updated.

Status

```text
CREATED

↓

COMPLETED
```

The Payment Intent now represents a successfully completed business payment.

---

# 12. Step 7 – Record Payment Event

A new Payment Event is created.

Event

```text
PAYMENT_SUCCESS
```

Source

```text
SYSTEM
```

The event is stored in

```text
payment_events
```

Purpose

Maintain a complete audit history.

---

# 13. Step 8 – Build Response

Example Response

```json
{
    "paymentReference": "01KXXXXXXXXXXXXXXX",
    "paymentIntentReference": "01KYYYYYYYYYYYYYYY",
    "gatewayPaymentId": "pay_Q2XYZ456",
    "status": "SUCCESS",
    "message": "Payment verified successfully."
}
```

---

# 14. Transaction Flow

The verification process executes inside a database transaction.

```text
Find Payment

↓

Verify Signature

↓

Fetch Payment Details

↓

Update Payment

↓

Update Payment Intent

↓

Create Payment Event

↓

Commit
```

If any database operation fails

```text
Rollback
```

No partial updates remain in the database.

---

# 15. Database Changes

## payment_intents

Before

```text
Status

CREATED
```

After

```text
Status

COMPLETED
```

---

## payments

Before

```text
Status

CREATED
```

After

```text
Status

SUCCESS
```

Updated Fields

```text
gatewayPaymentId

gatewaySignature

paymentMethod

gatewayStatus

gatewayResponse

paidAt
```

---

## payment_events

Before

```text
PAYMENT_CREATED
```

After

```text
PAYMENT_CREATED

↓

PAYMENT_SUCCESS
```

---

# 16. Why Fetch Payment Details From Razorpay?

The frontend should never be considered the source of truth.

Instead of trusting values such as:

- Payment Method
- Gateway Status

the backend retrieves them directly from Razorpay.

Benefits

- Prevents client-side manipulation.
- Stores trusted gateway information.
- Captures the complete payment response.
- Simplifies debugging and auditing.

---

# 17. Why Store Gateway Response?

The complete Razorpay response is stored in the `gateway_response` JSONB column.

Example

```json
{
    "id":"pay_Q2XYZ456",
    "method":"upi",
    "status":"captured",
    "bank":"HDFC",
    "email":"customer@example.com",
    "contact":"9999999999"
}
```

Benefits

- Complete payment history.
- Easier debugging.
- Future compatibility.
- No loss of gateway information.

---

# 18. Design Decisions

## Why Verify Signature?

The signature proves that the payment response originated from Razorpay and has not been modified.

Without signature verification, malicious clients could forge successful payment requests.

---

## Why Fetch Payment After Verification?

Signature verification confirms authenticity.

Fetching payment details provides trusted gateway metadata such as payment method and gateway status.

---

## Why Maintain Both Status Fields?

Internal Status

Represents the application's business state.

Gateway Status

Represents Razorpay's payment state.

Keeping both provides better traceability and simplifies troubleshooting.

---

## Why Record PAYMENT_SUCCESS?

Every important payment lifecycle event is recorded.

This creates an immutable audit history that supports debugging, reporting, and future event-driven integrations.

---

# 19. Current Flow Status

Implemented

- Request Validation
- Payment Lookup
- Duplicate Verification Prevention
- Payment State Validation
- Razorpay Signature Verification
- Fetch Payment Details
- Payment Method Mapping
- Gateway Status Update
- Gateway Response Storage
- Payment Update
- Payment Intent Update
- Payment Event Recording
- Transaction Management

Verified

- Razorpay Test Payment Successful
- Signature Verification Successful
- Payment Updated Successfully
- Payment Intent Updated Successfully
- Payment Method Stored
- Gateway Status Stored
- Gateway Response Stored
- PAYMENT_SUCCESS Event Recorded

---

# 20. Next Phase

The Payment Verification Flow completes the synchronous Pay-In lifecycle.

The next phase is **Webhook Processing**, where Razorpay asynchronously notifies the backend about payment events.

Webhook Flow

```text
Razorpay

↓

Webhook Event

↓

Webhook Controller

↓

Verify Webhook Signature

↓

Process Event

↓

Update Database

↓

Record Event

↓

Return HTTP 200
```

Webhook processing ensures the Payment Service remains synchronized with Razorpay even if the frontend never calls the verification API, making it an essential production feature.