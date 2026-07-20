# Refund Flow

## 1. Overview

The **Refund Flow** is responsible for returning funds to a customer after a successful payment.

A refund can be initiated by a merchant due to various business reasons such as customer requests, duplicate payments, failed deliveries, or order cancellations.

The StreamHub Payment Service supports both **Full Refunds** and **Partial Refunds** through Razorpay.

During this flow, the Payment Service:

- Validates the payment.
- Validates the refund request.
- Calculates the remaining refundable amount.
- Creates a Refund.
- Creates a Razorpay Refund.
- Persists refund information.
- Records refund events.
- Returns the refund response.

---

# 2. Objectives

The Refund Flow is responsible for:

- Processing customer refunds.
- Supporting full refunds.
- Supporting partial refunds.
- Preventing over-refunding.
- Maintaining refund history.
- Recording refund events.
- Synchronizing refund status with Razorpay.

---

# 3. What is a Refund?

A refund represents money returned to a customer after a successful payment.

Unlike a payment, which collects money, a refund reverses all or part of that transaction.

Every refund belongs to exactly one successful payment.

Relationship

```text
Payment

1

↓

N

Refunds
```

---

# 4. Refund Types

## Full Refund

The customer receives the complete payment amount.

Example

```text
Payment

₹1000

↓

Refund

₹1000

↓

Remaining Refundable

₹0
```

---

## Partial Refund

Only part of the payment amount is returned.

Example

```text
Payment

₹1000

↓

Refund

₹300

↓

Remaining

₹700
```

---

## Multiple Partial Refunds

A payment may have multiple refunds until the total refunded amount equals the original payment amount.

Example

```text
Payment

₹1000

↓

Refund

₹300

↓

Remaining

₹700

↓

Refund

₹200

↓

Remaining

₹500
```

---

# 5. High-Level Flow

```text
Merchant

↓

POST /api/v1/refunds

↓

RefundController

↓

RefundService

↓

Validate Request

↓

Validate Payment

↓

Validate Refund Amount

↓

Calculate Remaining Refundable Amount

↓

Create Refund Entity

↓

Create Razorpay Refund

↓

Persist Refund

↓

Create Refund Event

↓

Return Response
```

---

# 6. Create Refund API

Endpoint

```http
POST /api/v1/refunds
```

---

## Example Request

```json
{
  "paymentReference": "01KXXXXXXXXXXXXXXX",
  "amount": 50000,
  "reason": "CUSTOMER_REQUEST",
  "speed": "NORMAL",
  "initiator": "MERCHANT",
  "metadata": {
    "ticketId": "SUP-1001"
  }
}
```

---

# 7. Request Validation

Before processing begins, the request is validated.

Validation Rules

| Field | Validation |
|---------|------------|
| paymentReference | Required |
| amount | Greater than zero |
| reason | Required |
| speed | Valid Enum |
| initiator | Valid Enum |
| metadata | Optional |

---

# 8. Step 1 – Validate Payment

The service loads the payment using the payment reference.

The payment must:

- Exist.
- Be in SUCCESS status.

If the payment cannot be refunded,

```
HTTP 400
```

is returned.

---

# 9. Step 2 – Validate Refund Amount

The refund amount must satisfy:

- Greater than zero.
- Less than or equal to the remaining refundable amount.

Example

```text
Payment

₹1000

Already Refunded

₹400

Remaining

₹600

Requested Refund

₹700

↓

Rejected
```

---

# 10. Step 3 – Calculate Remaining Refundable Amount

The service calculates

```text
Remaining Refundable Amount

=

Payment Amount

-

Successful Refund Amount
```

Example

```text
Payment

₹1000

↓

Refunded

₹250

↓

Remaining

₹750
```

Only the remaining amount can be refunded.

---

# 11. Step 4 – Create Refund Entity

A Refund entity is created.

Generated values

- Refund Reference (ULID)
- REQUESTED Status

Stored information

- Payment
- Amount
- Currency
- Refund Reason
- Refund Speed
- Refund Initiator
- Metadata

---

# 12. Step 5 – Create Razorpay Refund

The service delegates refund creation to the Gateway Layer.

```text
RefundService

↓

Gateway Interface

↓

RazorpayService

↓

Create Refund API
```

The request contains

- Gateway Payment ID
- Refund Amount
- Speed
- Notes

Razorpay returns

- Gateway Refund ID
- Gateway Status
- Complete Refund Response

---

# 13. Step 6 – Persist Refund

The refund entity is updated with the Razorpay response.

Updated Fields

- Gateway Refund ID
- Gateway Status
- Gateway Response
- Gateway Notes
- Status
- Processed At

The refund is then persisted.

---

# 14. Step 7 – Record Refund Event

A refund event is created.

Current Event

```text
REFUND_REQUESTED
```

When Razorpay confirms the refund

```text
REFUND_SUCCESS
```

is recorded.

The event history provides an immutable audit trail.

---

# 15. Build Response

Example

```json
{
  "refundReference": "01KXXXXXXXXXXXX",
  "paymentReference": "01KYYYYYYYYYYYY",
  "status": "PROCESSING",
  "message": "Refund created successfully."
}
```

---

# 16. Refund Status Lifecycle

```text
REQUESTED

↓

PROCESSING

↓

SUCCESS
```

Failure

```text
REQUESTED

↓

PROCESSING

↓

FAILED
```

---

# 17. Database Changes

## refunds

Before

```text
No Refund
```

After

```text
Refund

REQUESTED

↓

PROCESSING
```

---

## refund_events

```text
REFUND_REQUESTED
```

Later

```text
REFUND_SUCCESS
```

or

```text
REFUND_FAILED
```

---

# 18. Transaction Flow

```text
Validate Payment

↓

Validate Amount

↓

Create Refund

↓

Call Razorpay

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

No partial refund data remains.

---

# 19. Design Decisions

## Why Separate Refunds from Payments?

Payments represent money collected.

Refunds represent money returned.

Keeping them separate allows:

- Multiple refunds
- Partial refunds
- Independent lifecycle management
- Better audit history

---

## Why Calculate Remaining Refundable Amount?

This prevents merchants from refunding more money than the customer originally paid.

---

## Why Store Gateway Response?

The complete Razorpay refund response is stored using PostgreSQL JSONB.

Benefits

- Easier debugging.
- Complete audit trail.
- Future compatibility.
- No information loss.

---

## Why Record Refund Events?

Every significant refund operation creates an immutable event.

Benefits

- Auditing
- Troubleshooting
- Reporting
- Event-driven integrations

---

# 20. Error Handling

Possible errors

### Validation Error

```
HTTP 400
```

Example

```
Refund amount must be greater than zero.
```

---

### Payment Not Found

```
HTTP 404
```

---

### Payment Not Refundable

```
HTTP 400
```

---

### Refund Amount Exceeded

```
HTTP 400
```

---

### Gateway Failure

```
HTTP 500
```

---

# 21. Testing Strategy

The following scenarios have been tested.

## Successful Full Refund

Expected

- Refund Created
- Razorpay Refund Created
- Refund Event Created

---

## Successful Partial Refund

Expected

- Remaining Refundable Amount Updated

---

## Multiple Partial Refunds

Expected

- Remaining Refundable Amount Calculated Correctly

---

## Refund Exceeds Remaining Amount

Expected

```
HTTP 400
```

---

## Invalid Payment

Expected

```
HTTP 404
```

---

## Gateway Failure

Expected

```
HTTP 500
```

---

# 22. Current Implementation

Implemented

- Full Refund
- Partial Refund
- Multiple Partial Refunds
- Remaining Refund Validation
- Razorpay Refund Integration
- Refund Persistence
- Refund Events
- Manual DTO Mapping
- Transaction Management
- JSONB Gateway Response Storage

Verified

- Razorpay Test Refund
- Database Updates
- Partial Refunds
- Multiple Refunds
- Event Recording

---

# 23. Related Flows

The Refund Flow is part of the complete payment lifecycle.

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

# 24. Future Enhancements

Future improvements include

- Instant Refund Support
- Bulk Refunds
- Scheduled Refunds
- Automatic Refund Retry
- Settlement Reconciliation
- Refund Analytics
- Multi-Gateway Refund Support

---

# 25. Conclusion

The Refund Flow provides a secure and production-oriented mechanism for returning funds to customers.

The implementation supports:

- Full refunds
- Partial refunds
- Multiple partial refunds
- Gateway integration
- Immutable event tracking
- Transaction management
- Complete audit history

Together with the Payment Intent Flow, Payment Verification Flow, and Webhook Processing, the Refund Flow completes the end-to-end payment lifecycle of the StreamHub Payment Service.