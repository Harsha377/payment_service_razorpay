# Known Design Decisions

## Overview

This document records the important architectural and implementation decisions made during the development of the StreamHub Payment Service.

The purpose of this document is to explain **why** specific decisions were made, not just **what** was implemented.

Understanding these decisions will help future development, maintenance, and onboarding.

---

# 1. Dedicated Payment Service

## Decision

The payment functionality is implemented as a standalone service.

## Reason

Payment processing is a business capability that can be reused by multiple applications.

Keeping it separate provides:

- Better maintainability
- Independent deployment
- Easier scaling
- Better security isolation
- Gateway abstraction

---

# 2. Layered Architecture

## Decision

The project follows a layered architecture.

```text
Controller

↓

Service

↓

Mapper

↓

Repository

↓

Database
```

## Reason

Each layer has a single responsibility.

Benefits

- Separation of concerns
- Easier testing
- Cleaner code
- Better maintainability

---

# 3. Gateway Abstraction

## Decision

Business services never communicate directly with Razorpay.

Instead

```text
Payment Service

↓

Gateway Interface

↓

RazorpayServiceImpl
```

## Reason

Avoids coupling business logic with a specific payment gateway.

Future support

- Stripe
- Cashfree
- PhonePe
- PayU

can be added with minimal business code changes.

---

# 4. Payment Intent and Payment Separation

## Decision

Payment Intent and Payment are stored in separate tables.

## Reason

A Payment Intent represents the business request.

A Payment represents a gateway transaction.

Future benefits

- Payment retries
- Multiple gateways
- Failed payments
- Recovery
- Multiple attempts

---

# 5. Payment Event Table

## Decision

Every important payment action creates a Payment Event.

Examples

```text
PAYMENT_CREATED

PAYMENT_SUCCESS
```

## Reason

Instead of overwriting history,

the system records every important business event.

Benefits

- Complete audit trail
- Easier debugging
- Compliance
- Future event-driven architecture

---

# 6. ULID Business References

## Decision

Business references use ULIDs instead of database IDs.

Current references

- Payment Intent Reference
- Payment Reference
- Payment Event Reference

## Reason

Advantages

- Globally unique
- Sortable
- URL friendly
- Safe to expose publicly

Database IDs remain internal.

---

# 7. PostgreSQL JSONB

## Decision

Gateway-related payloads are stored using JSONB.

Current fields

```text
metadata

gatewayResponse

eventData
```

## Reason

Gateway payloads evolve over time.

JSONB provides

- Flexible schema
- No frequent database migrations
- Complete gateway response storage
- Easier debugging

---

# 8. OffsetDateTime

## Decision

All timestamps use

```java
OffsetDateTime
```

## Reason

Payment systems may operate across multiple time zones.

OffsetDateTime preserves timezone information.

Examples

- Payment time
- Expiration
- Auditing
- Gateway timestamps

---

# 9. Spring Data Auditing

## Decision

Auditing is automatic.

Using

- @CreatedDate
- @LastModifiedDate

## Reason

Avoids manually setting timestamps.

Ensures consistency.

---

# 10. Amount Storage

## Decision

Amounts are stored as

```java
Long
```

in the smallest currency unit.

Example

```text
₹100

↓

10000 paise
```

## Reason

Avoids floating-point precision errors.

Matches Razorpay API requirements.

---

# 11. Currency

## Decision

Currency is stored as a 3-character ISO code.

Example

```text
INR

USD

EUR
```

## Reason

Standardized format.

Supports future international currencies.

---

# 12. Manual Mapping

## Decision

DTO mapping is performed manually.

MapStruct is intentionally not used.

## Reason

Advantages

- Full control
- Easier debugging
- No generated code
- Explicit mappings

---

# 13. Transaction Management

## Decision

Business operations execute inside transactions.

Example

```text
Create Payment Intent

↓

Create Payment

↓

Create Event

↓

Commit
```

## Reason

Ensures data consistency.

If one operation fails,

the transaction rolls back.

---

# 14. Gateway Verification

## Decision

Every payment must pass signature verification.

## Reason

Prevents forged payment requests.

Only payments verified by Razorpay are accepted.

---

# 15. Payment Details Source

## Decision

Payment Method

Gateway Status

Gateway Response

are **not accepted from the frontend**.

Instead,

they are fetched directly from Razorpay.

## Reason

The frontend cannot be trusted.

Razorpay is the source of truth.

---

# 16. Payment Method Mapping

## Decision

Payment methods are mapped using a dedicated mapper.

Instead of

```java
Enum.valueOf(...)
```

the project uses

```text
Razorpay String

↓

Mapper

↓

PaymentMethod Enum
```

## Reason

Prevents runtime failures.

Supports future payment methods.

Centralizes mapping logic.

---

# 17. Gateway Response Storage

## Decision

The complete Razorpay payment response is stored.

## Reason

Benefits

- Debugging
- Auditing
- Future compatibility
- Historical analysis

No information is lost.

---

# 18. Internal Status vs Gateway Status

## Decision

Both statuses are stored separately.

Internal

```text
CREATED

SUCCESS
```

Gateway

```text
created

captured

authorized
```

## Reason

Business state and gateway state represent different concepts.

Keeping both simplifies troubleshooting.

---

# 19. Public Gateway Key

## Decision

The backend returns the public Gateway Key.

The frontend does **not** maintain its own Razorpay Key ID.

## Reason

Single source of truth.

Advantages

- No duplicated configuration
- Easier deployments
- Test and Live environments remain synchronized
- Prevents configuration drift

The secret key never leaves the backend.

---

# 20. Secret Key Protection

## Decision

The Razorpay Secret Key is never exposed.

Only the backend uses it.

## Reason

Required for

- Order Creation
- Signature Verification
- Gateway Authentication

Exposing it would compromise payment security.

---

# 21. Business References in APIs

## Decision

APIs expose business references.

Never expose

```text
Database IDs
```

## Reason

Database IDs are implementation details.

Business references remain stable even if internal IDs change.

---

# 22. Current Gateway

## Decision

Only Razorpay is currently implemented.

## Reason

The architecture is gateway-independent,

but development focuses on one gateway until the complete Pay-In flow is stable.

Future gateways will reuse the same abstraction.

---

# 23. Exception Handling

## Decision

Centralized exception handling.

Using

```text
GlobalExceptionHandler
```

## Reason

Provides

- Consistent responses
- Cleaner controllers
- Easier maintenance

---

# 24. Validation

## Decision

Bean Validation is performed at the API layer.

## Reason

Reject invalid requests before business logic executes.

Improves reliability.

---

# 25. API Versioning

## Decision

All APIs begin with

```text
/ api / v1
```

## Reason

Allows future API evolution without breaking existing clients.

---

# 26. Current Project Scope

Implemented

- Pay-In
- Payment Intent
- Payment Verification

## Webhook Decisions

### Auto Capture

The Payment Service currently uses Razorpay Auto Capture.

Manual Capture is reserved for future implementation.

---

### Source of Truth

The `payment.captured` webhook is considered the source of truth for successful payment completion.

The application updates Payment and Payment Intent only after receiving this event.

---

### payment.authorized

The `payment.authorized` event is currently treated as an informational event.

No database updates are performed because Auto Capture is enabled.

---

### order.paid

The `order.paid` event is also treated as an informational event.

Payment completion has already been processed during `payment.captured`.

---

### Signature Verification

Every incoming webhook is verified using Razorpay's HMAC SHA256 signature verification before any business logic is executed.

---

### Event Processing

Each supported webhook event is processed independently using dedicated handler methods.

This design improves maintainability and simplifies future webhook expansion.

Not Yet Implemented

- Refunds
- Payouts
- Read APIs

## Reason

Development follows an incremental approach.

The synchronous payment flow is completed before introducing asynchronous processing.

---

# 27. Production-First Philosophy

## Decision

The project prioritizes production-ready architecture over rapid implementation.

Examples

- Layered architecture
- Gateway abstraction
- Transaction management
- Audit history
- JSONB storage
- Business references
- Manual mapping

## Reason

The objective is to build a payment service that can evolve into an enterprise-grade solution rather than a demo application.

---

# Future Review

This document should be updated whenever a significant architectural or design decision is made.

Examples include:

- Webhook processing strategy
- Refund architecture
- Payout architecture
- Multi-gateway routing
- Idempotency implementation
- Retry mechanisms
- Monitoring and observability
- Security enhancements

Maintaining this document ensures that future developers understand the reasoning behind design choices, reducing the need to rediscover decisions by reading source code alone.