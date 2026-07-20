# Architecture

## 1. Overview

The **StreamHub Payment Service** follows a layered architecture that separates business logic, persistence, and external gateway communication into independent modules.

The primary objective of this architecture is to keep the business domain independent of the payment gateway implementation while maintaining high maintainability, extensibility, and testability.

The service currently integrates with Razorpay and supports both **Pay-In** and **Refund** operations, including synchronous APIs and asynchronous webhook processing.

The architecture has been designed so that additional payment gateways can be integrated in the future without changing the core business logic.

---

# 2. High-Level Architecture

```text
                    Client Application
                           │
                           ▼
                    REST Controllers
                           │
                           ▼
                    Business Services
                           │
          ┌────────────────┴────────────────┐
          ▼                                 ▼
   Repository Layer                 Gateway Layer
          │                                 │
          ▼                                 ▼
     PostgreSQL                      Razorpay APIs
          ▲                                 │
          │                                 ▼
      Webhook Service ◄──────── Razorpay Webhooks
```

---

# 3. Layered Architecture

The project follows the following layers.

```text
Controller
      │
      ▼
Service
      │
      ▼
Mapper
      │
      ▼
Repository
      │
      ▼
Database
```

Each layer has a single responsibility.

---

# 4. Package Structure

```text
src/main/java
│
├── common
│
├── gateway
│
├── payment
│
├── refund
│
├── webhook
│
└── PaymentServiceApplication

src/main/java
│
├── common
│
│   ├── config
│   ├── entity
│   ├── exception
│   ├── handler
│   ├── util
│
├── gateway
│
│   ├── config
│   ├── dto
│   │     ├── request
│   │     └── response
│   ├── mapper
│   ├── service
│   │     └── impl
│
├── payment
│
│   ├── controller
│   ├── dto
│   │     ├── request
│   │     └── response
│   ├── entity
│   ├── enums
│   ├── mapper
│   ├── repository
│   ├── service
│   │     └── impl
│
└── PaymentServiceApplication
```

---

# 5. Controller Layer

## Responsibility

The controller layer is responsible for:

- Receiving HTTP requests.
- Request validation.
- Delegating business operations.
- Returning HTTP responses.

Controllers do **not** contain business logic.

Current Controllers

- PaymentIntentController
- PaymentController
- RefundController
- WebhookController


---

# 6. Service Layer

The service layer contains all business logic.

Responsibilities should now include:

* Payment validation
* Payment verification
* Payment intent management
* Refund validation
* Refund processing
* Webhook processing
* Event recording
* Gateway orchestration
* Transaction management

The service layer coordinates all other layers.

Example

```text
Controller

↓

Service

↓

Gateway

↓

Repository
```

---

# 7. Repository Layer

Repositories are responsible only for database access.

Responsibilities

- CRUD operations.
- Entity retrieval.
- Custom queries.

Repositories never contain business logic.

Current Repositories

- PaymentIntentRepository
- PaymentRepository
- PaymentEventRepository
- RefundRepository
- RefundEventRepository

---

# 8. Mapper Layer

The project uses manual mapping.

Responsibilities

- Entity → DTO
- DTO → Entity
- Gateway DTO conversion

Example

```text
CreatePaymentIntentRequest

↓

PaymentIntentEntity

↓

PaymentResponse
```

Gateway mapping

```text
VerifyPaymentRequest

↓

GatewayPaymentVerificationRequest
```

The mapper layer isolates internal models from API models.

---

# 9. Gateway Layer

The Gateway Layer is responsible for communication with external payment providers.

Current implementation

```text
Gateway Layer

↓

Razorpay
```

Responsibilities

- Create Order
- Verify Payment Signature 
- Fetch Payment Details 
- Create Refund 
- Fetch Refund Details 
- Verify Webhook Signature

The business layer never communicates directly with Razorpay.

Instead

```text
Payment Service

↓

Gateway Interface

↓

Razorpay Implementation
```

This abstraction allows future support for additional gateways.

Example

```text
Payment Service

↓

Gateway Interface

↓

├── Razorpay

├── Stripe

└── Cashfree
```

---

# 10. Database Layer

The database layer stores all payment-related information.

Current Tables

- payment_intent
- payments 
- payment_events 
- refunds 
- refund_events

Relationships

```text
Payment Intent

1

↓

N

Payments

1

↓

N

Payment Events

↓

1

↓

N

Refunds

↓

1

↓

N

Refund Events
```

---

# 11. Request Flow

## Payment Initialization

```text
Client

↓

PaymentIntentController

↓

PaymentIntentService

↓

PaymentIntentRepository

↓

Gateway Service

↓

Razorpay

↓

PaymentRepository

↓

PaymentEventRepository

↓

Response
```
### Refund Flow
Merchant

↓

RefundController

↓

RefundService

↓

Validate Refund

↓

Gateway Service

↓

Razorpay Refund API

↓

RefundRepository

↓

RefundEventRepository

↓

Response


### Webhook Flow

Razorpay

↓

WebhookController

↓

WebhookService

↓

Payment / Refund Service

↓

Repositories

↓

Events

↓

Response

---

## Payment Verification

```text
Frontend

↓

PaymentController

↓

PaymentService

↓

Gateway Verification

↓

Fetch Payment Details

↓

Update Payment

↓

Update Payment Intent

↓

Create Event

↓

Response
```

---

# 12. Transaction Management

Business operations are executed inside transactions.

Example

```text
Verify Payment

↓

Update Payment

↓

Update Payment Intent

↓

Create Payment Event

↓

Commit
```

If any operation fails

```text
Rollback
```

This guarantees database consistency.

---

# 13. Entity Relationships

```text
PaymentIntentEntity

1

↓

N

PaymentEntity

1

↓

N

PaymentEventEntity

↓

1

↓

N

RefundEntity

↓

1

↓

N

RefundEventEntity
```

Each entity has a specific responsibility.

PaymentIntent

Business request.

Payment

Gateway transaction.

PaymentEvent

Audit history.

---

# 14. Gateway Independence

The business layer is unaware of Razorpay implementation details.

Instead

```text
Business Layer

↓

Gateway Interface

↓

Gateway Implementation
```

Current

```text
Gateway Interface

↓

RazorpayServiceImpl
```

Future

```text
Gateway Interface

↓

RazorpayServiceImpl

↓

StripeServiceImpl

↓

CashfreeServiceImpl
```

No business code changes are required when adding new gateways.

---

# 15. Error Handling

The project uses centralized exception handling.

Responsibilities

- Business exceptions.
- Validation errors.
- Gateway failures.
- Database errors.

All exceptions are handled by the GlobalExceptionHandler.

---

# 16. Auditing

All entities inherit from BaseEntity.

Common fields

- id
- createdAt
- updatedAt

Auditing is automatically managed using Spring Data JPA.

---

# 17. JSONB Strategy

Several gateway-related fields are stored using PostgreSQL JSONB.

Current JSONB fields

* PaymentIntent.metadata
* Payment.gatewayResponse
* PaymentEvent.eventData
* Refund.metadata
* Refund.gatewayResponse
* Refund.gatewayNotes
* RefundEvent.eventData

Advantages

- Flexible schema.
- Complete gateway payload storage.
- Easier debugging.
- Future compatibility.

---

# 18. Business References

The project uses ULIDs instead of database IDs for external references.

Current references

* Payment Intent Reference
* 
* Payment Reference
* 
* Payment Event Reference
* 
* Refund Reference
* 
* Refund Event Reference

Advantages

- Globally unique.
- Sortable.
- URL-safe.
- Suitable for distributed systems.

---

# 19. Design Principles

The architecture follows these principles.

### Separation of Concerns

Each layer has one responsibility.

---

### Single Responsibility

Each service performs one business function.

---

### Dependency Inversion

Business logic depends on interfaces rather than gateway implementations.

---

### Extensibility

Additional gateways can be integrated without changing business logic.

---

### Transactional Consistency

Critical operations execute within database transactions.

---

### Auditability

Every important payment action is recorded as a payment event.

---

### Gateway Isolation

Gateway-specific code never leaks into business logic.

---

### Production-Oriented Design

The architecture prioritizes maintainability, scalability, and future extensibility over quick implementation.

### Idempotency

Payment verification and webhook processing are idempotent to safely handle retries.

---

### Immutable Audit Trail

Payment and refund events are append-only records used for auditing.

---

### Gateway Abstraction

External gateway logic is isolated behind interfaces, allowing additional providers to be added with minimal business-layer changes.

---

# 20. Current Architecture Status

## Completed

Completed

✓ Layered Architecture

✓ Gateway Abstraction

✓ Repository Pattern

✓ Manual Mapping

✓ Transaction Management

✓ Payment Module

✓ Payment Webhooks

✓ Refund Module

✓ Refund Webhooks

✓ JSONB Support

✓ ULID References

✓ Validation

✓ Global Exception Handling

✓ Production Logging

✓ Idempotent Processing

---

# 21. Planned Architectural Enhancements

The following architectural components will be added in future phases.

* Payout Module
* 
* Settlement Management
* 
* Financial Reconciliation
* 
* Multiple Gateway Support
* 
* Monitoring
* 
* Metrics
* 
* Distributed Tracing
* 
* OpenAPI Documentation
* 
* Integration Testing
* 
* Unit Testing
* 
* Performance Testing
* 
* Security Hardening
* 
* Production Dashboard

---

# Architecture Decisions

The following decisions guide the implementation of the payment service:

- Business references use ULIDs instead of database IDs.
- Monetary amounts are stored in the smallest currency unit (paise).
- Manual DTO mapping is used instead of MapStruct.
- Gateway integrations are isolated behind service interfaces.
- Payment and refund events are immutable audit records.
- PostgreSQL JSONB is used to preserve complete gateway payloads.
- Webhook processing is idempotent to safely handle retries.
- Flyway is used for version-controlled database schema management.
- Business entities use transactional consistency for state transitions.