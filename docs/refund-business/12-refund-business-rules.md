# Refund Business Rules

## 1. Overview

This document defines the business rules governing the Refund module within the StreamHub Payment Service.

These rules ensure that refunds are processed consistently, securely, and accurately while maintaining transactional integrity and a complete audit trail.

Business rules are enforced before any communication with Razorpay or any database updates.

---

# 2. Objectives

The Refund module is responsible for:

- Returning funds to customers.
- Supporting full refunds.
- Supporting partial refunds.
- Supporting multiple partial refunds.
- Preventing over-refunding.
- Maintaining refund history.
- Recording refund events.
- Synchronizing refund status with Razorpay.

---

# 3. Payment Eligibility Rules

A refund can only be created for an existing payment.

Rule

```text
Payment Must Exist
```

If the payment does not exist

```text
HTTP 404 Not Found
```

---

A refund is allowed only for successful payments.

Allowed Status

```text
SUCCESS
```

Rejected Statuses

```text
CREATED

FAILED

CANCELLED

EXPIRED
```

---

# 4. Refund Amount Rules

The refund amount must always be greater than zero.

Allowed

```text
Amount > 0
```

Rejected

```text
Amount <= 0
```

---

The refund amount must not exceed the remaining refundable amount.

Formula

```text
Remaining Refundable

=

Payment Amount

-

Total Successful Refund Amount
```

Example

```text
Payment

₹1000

↓

Refunded

₹300

↓

Remaining

₹700
```

Allowed

```text
Refund ₹500
```

Rejected

```text
Refund ₹800
```

---

# 5. Full Refund Rules

A full refund returns the complete payment amount.

Example

```text
Payment

₹1000

↓

Refund

₹1000

↓

Remaining

₹0
```

After a full refund

No further refunds are permitted.

---

# 6. Partial Refund Rules

Partial refunds return only part of the payment amount.

Example

```text
Payment

₹1000

↓

Refund

₹250

↓

Remaining

₹750
```

Multiple partial refunds are supported until the remaining refundable amount reaches zero.

---

# 7. Multiple Partial Refund Rules

The service allows multiple refund requests against the same payment.

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

↓

Refund

₹500

↓

Remaining

₹0
```

The combined successful refund amount must never exceed the original payment amount.

---

# 8. Currency Rules

Refunds always use the same currency as the original payment.

Example

```text
Payment

INR

↓

Refund

INR
```

Currency conversion is not supported.

---

# 9. Gateway Rules

Refunds are always created through Razorpay.

Business services never communicate directly with the Razorpay SDK.

Architecture

```text
Refund Service

↓

Gateway Interface

↓

Razorpay Service

↓

Razorpay API
```

---

# 10. Source of Truth Rules

The Payment Service never trusts refund information supplied by clients.

Instead

- Refund Status
- Gateway Status
- Gateway Refund ID

are retrieved directly from Razorpay.

Razorpay is considered the source of truth.

---

# 11. Refund Status Rules

Current lifecycle

```text
REQUESTED

↓

PROCESSING

↓

SUCCESS
```

Failure lifecycle

```text
REQUESTED

↓

PROCESSING

↓

FAILED
```

Only valid state transitions are allowed.

---

# 12. Refund Reference Rules

Every refund receives a unique business reference.

The reference

- Uses ULID.
- Is globally unique.
- Is externally exposed.

Database IDs are never exposed outside the service.

---

# 13. Gateway Response Rules

The complete Razorpay refund response is stored.

Purpose

- Audit history.
- Easier debugging.
- Future compatibility.

Storage

```text
refunds.gateway_response
```

using PostgreSQL JSONB.

---

# 14. Gateway Notes Rules

Gateway notes are optional.

If provided

- They are forwarded to Razorpay.
- They are stored for future reference.

---

# 15. Metadata Rules

Refund metadata is optional.

Metadata

- Is stored using JSONB.
- Can contain business-specific information.
- Is never interpreted by the Payment Service.

Example

```json
{
  "ticketId":"SUP-1001",
  "requestedBy":"Support Team"
}
```

---

# 16. Refund Event Rules

Every significant refund operation creates a refund event.

Current events

```text
REFUND_REQUESTED

REFUND_SUCCESS

REFUND_FAILED
```

Events are immutable.

Existing events are never modified or deleted.

---

# 17. Transaction Rules

The following operations execute within a single database transaction.

```text
Validate Refund

↓

Create Refund

↓

Persist Refund

↓

Create Refund Event

↓

Commit
```

If any database operation fails

```text
Rollback
```

No partial data remains.

---

# 18. Idempotency Rules

Refund processing must be idempotent.

Duplicate webhook deliveries

```text
↓

Ignored
```

Duplicate processing must not

- Create duplicate events.
- Update the refund multiple times.
- Produce inconsistent data.

---

# 19. Security Rules

The Razorpay Secret Key

- Never leaves the backend.
- Is never exposed through APIs.
- Is used only for server-side gateway communication.

Webhook authenticity is verified using

```text
X-Razorpay-Signature
```

---

# 20. Error Handling Rules

The service rejects requests when:

- Payment does not exist.
- Payment is not successful.
- Refund amount is invalid.
- Refund amount exceeds the remaining refundable amount.
- Gateway request fails.
- Webhook signature is invalid.

Appropriate HTTP responses are returned.

---

# 21. Audit Rules

Every refund operation is fully traceable.

Stored information includes

- Refund Reference
- Gateway Refund ID
- Refund Status
- Refund Amount
- Gateway Response
- Refund Events
- Created Timestamp
- Updated Timestamp

This provides a complete audit history.

---

# 22. Future Business Rules

Future versions will introduce additional rules for:

- Scheduled Refunds
- Instant Refunds
- Bulk Refunds
- Settlement Validation
- Refund Approval Workflow
- Multi-Gateway Refunds
- Currency Conversion

---

# 23. Summary

The Refund Business Rules ensure that refund processing remains secure, predictable, and consistent.

The current implementation guarantees:

- Refunds are created only for successful payments.
- Over-refunding is prevented.
- Multiple partial refunds are supported.
- Razorpay remains the source of truth.
- Every refund operation is audited through immutable refund events.
- All critical operations execute transactionally.

These rules provide a solid foundation for a production-ready refund processing system while allowing future enhancements without compromising data integrity.