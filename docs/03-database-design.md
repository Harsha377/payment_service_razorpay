# Database Design

## 1. Overview

The StreamHub Payment Service uses **PostgreSQL** as its primary relational database.

The database is designed to support a complete payment lifecycle while maintaining transactional consistency, auditability, scalability, and future extensibility.

The current implementation consists of three core tables:

- `payment_intents`
- `payments`
- `payment_events`

Each table has a clearly defined responsibility and together they model the complete Pay-In lifecycle.

---

# 2. Design Goals

The database design follows these objectives.

- Normalize business data.
- Separate business entities from gateway entities.
- Maintain complete payment history.
- Support auditing.
- Support multiple payment attempts in the future.
- Store gateway-specific payloads.
- Provide efficient querying through indexes.
- Maintain referential integrity.

---

# 3. Entity Relationship Diagram

```text
                    payment_intents
                    ----------------
                           │
                           │ 1
                           │
                           ▼
                    payments
                    --------
                           │
                           │ 1
                           │
                           ▼
                   payment_events
```

Relationship

```text
One Payment Intent

↓

Many Payments

↓

Many Payment Events
```

---

# 4. payment_intents

## Purpose

Represents the customer's payment request before any interaction with the payment gateway.

A Payment Intent acts as the business-level representation of a payment.

---

## Table Structure

| Column | Description |
|---------|-------------|
| id | Internal primary key |
| payment_intent_reference | Business reference (ULID) |
| order_reference | Business order reference |
| customer_reference | Customer identifier |
| amount | Amount in the smallest currency unit |
| currency | ISO currency code |
| status | Payment Intent status |
| description | Business description |
| metadata | Additional business information (JSONB) |
| expires_at | Expiration timestamp |
| created_at | Creation timestamp |
| updated_at | Last modification timestamp |

---

## Constraints

Primary Key

```text
id
```

Unique

```text
payment_intent_reference
```

Check Constraint

```text
amount > 0
```

Currency Validation

```text
Length = 3

Uppercase
```

---

## Indexes

```text
order_reference

customer_reference

status
```

---

## Status Lifecycle

```text
CREATED

↓

COMPLETED
```

Future

```text
CREATED

↓

EXPIRED

↓

CANCELLED
```

---

## Metadata

The metadata column uses PostgreSQL JSONB.

```java
@JdbcTypeCode(SqlTypes.JSON)
private JsonNode metadata;
```

Example

```json
{
    "plan":"Premium",
    "coupon":"WELCOME10",
    "tenant":"streamhub"
}
```

---

# 5. payments

## Purpose

Represents an individual payment processed through an external payment gateway.

A single Payment Intent may have multiple Payment records in the future due to retries.

---

## Table Structure

| Column | Description |
|---------|-------------|
| id | Internal primary key |
| payment_reference | Business payment reference |
| payment_intent_id | FK to payment_intents |
| gateway_name | Payment gateway |
| gateway_order_id | Gateway order identifier |
| gateway_payment_id | Gateway payment identifier |
| gateway_signature | Signature received after payment |
| amount | Payment amount |
| currency | Currency |
| payment_method | UPI, Card, Wallet, etc. |
| status | Internal payment status |
| gateway_status | Gateway payment status |
| gateway_response | Complete gateway response (JSONB) |
| paid_at | Payment completion timestamp |
| created_at | Creation timestamp |
| updated_at | Last update timestamp |

---

## Constraints

Primary Key

```text
id
```

Unique

```text
payment_reference

gateway_payment_id
```

Foreign Key

```text
payment_intent_id

↓

payment_intents.id
```

---

## Internal Status

Managed by our application.

```text
CREATED

↓

SUCCESS
```

Future

```text
FAILED

AUTHORIZED

CAPTURED

REFUNDED

EXPIRED
```

---

## Gateway Status

Represents the status returned by Razorpay.

Examples

```text
created

authorized

captured

failed
```

This field is intentionally independent of the internal payment status.

---

## Payment Method

Represents the method used by the customer.

Examples

```text
UPI

CARD

NET_BANKING

WALLET

EMI
```

The payment method is **never trusted from the frontend**.

Instead, it is fetched directly from Razorpay after successful payment verification.

---

## Gateway Response

The complete payment response from Razorpay is stored.

```java
@JdbcTypeCode(SqlTypes.JSON)
private JsonNode gatewayResponse;
```

Example

```json
{
    "id":"pay_xxx",
    "status":"captured",
    "method":"upi",
    "bank":"HDFC",
    "email":"customer@example.com"
}
```

Advantages

- Easier debugging
- Complete audit trail
- Future compatibility
- No information loss

---

# 6. payment_events

## Purpose

Stores every significant event that occurs during a payment lifecycle.

This table acts as an immutable audit log.

---

## Table Structure

| Column | Description |
|---------|-------------|
| id | Internal primary key |
| payment_event_reference | Business reference |
| payment_id | FK to payments |
| event_type | Business event |
| event_source | Origin of event |
| event_data | Additional event information |
| created_at | Event timestamp |

---

## Relationship

```text
One Payment

↓

Many Events
```

---

## Event Types

Implemented

```text
PAYMENT_CREATED

PAYMENT_SUCCESS
```

Future

```text
PAYMENT_FAILED

PAYMENT_CAPTURED

PAYMENT_PENDING

PAYMENT_EXPIRED

PAYMENT_REFUNDED

WEBHOOK_RECEIVED
```

---

## Event Source

Examples

```text
SYSTEM

CLIENT

WEBHOOK

ADMIN
```

---

## Event Data

Stored using JSONB.

```java
@JdbcTypeCode(SqlTypes.JSON)
private JsonNode eventData;
```

Example

```json
{
    "gatewayPaymentId":"pay_xxx",
    "gatewayStatus":"captured",
    "paymentMethod":"UPI"
}
```

---

# 7. JSONB Usage

The project currently uses JSONB for three columns.

| Table | Column |
|--------|--------|
| payment_intents | metadata |
| payments | gateway_response |
| payment_events | event_data |

Advantages

- Flexible schema
- Native PostgreSQL support
- Efficient indexing
- Complete gateway payload storage
- Simplified debugging

---

# 8. Auditing

Every entity extends BaseEntity.

Common columns

```text
id

created_at

updated_at
```

Auditing is automatically managed using Spring Data JPA.

---

# 9. Business References

The project uses ULIDs for all externally exposed identifiers.

Current references

```text
payment_intent_reference

payment_reference

payment_event_reference
```

Advantages

- Globally unique
- Time sortable
- URL friendly
- Safer than exposing database IDs

Database IDs remain internal implementation details.

---

# 10. Data Flow

## Payment Creation

```text
Payment Intent

↓

payment_intents

↓

Create Razorpay Order

↓

payments

↓

payment_events
```

---

## Payment Verification

```text
Verify Signature

↓

Fetch Payment Details

↓

Update payments

↓

Update payment_intents

↓

Create payment_events
```

---

# 11. Database Integrity

The database maintains integrity using

- Primary Keys
- Foreign Keys
- Unique Constraints
- Check Constraints
- Transaction Management

These mechanisms ensure that payment data remains consistent even if an operation fails midway.

---

# 12. Design Decisions

## Why Separate payment_intents and payments?

A Payment Intent represents the customer's business request.

A Payment represents an actual gateway transaction.

This separation allows future support for:

- Multiple payment attempts
- Retry mechanisms
- Multiple gateways
- Payment recovery

without changing the business model.

---

## Why Store JSONB?

Gateway APIs evolve over time.

Instead of continuously modifying the schema for new gateway fields, the complete gateway response is preserved in JSONB while still keeping important business fields in dedicated columns.

---

## Why Store Both Internal Status and Gateway Status?

Internal Status

Represents the application's understanding of the payment lifecycle.

Gateway Status

Represents the payment gateway's state.

Keeping both provides better traceability and simplifies troubleshooting when business and gateway states differ.

---

## Why Maintain payment_events?

Instead of overwriting history, every significant action is recorded.

Benefits

- Complete audit trail
- Easier debugging
- Regulatory compliance
- Future event-driven architecture

---

# 13. Future Enhancements

The current database design supports future additions with minimal schema changes.

Planned enhancements include

- Refund tables
- Payout tables
- Settlement tracking
- Webhook event history
- Idempotency records
- Gateway configuration tables
- Multiple gateway support

The current schema provides a scalable foundation for these future capabilities while preserving backward compatibility.