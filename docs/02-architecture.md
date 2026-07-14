# Architecture

## 1. Overview

The **StreamHub Payment Service** follows a layered architecture that separates business logic, persistence, and external gateway communication into independent modules.

The primary objective of this architecture is to keep the business domain independent of the payment gateway implementation while maintaining high maintainability, extensibility, and testability.

The service currently supports Razorpay for Pay-In operations but is designed so that additional payment gateways can be integrated in the future without changing the core business logic.

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
             ┌─────────────────┴─────────────────┐
             ▼                                   ▼
      Repository Layer                   Gateway Layer
             │                                   │
             ▼                                   ▼
        PostgreSQL                        Razorpay APIs
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

---

# 6. Service Layer

The service layer contains all business logic.

Responsibilities include:

- Payment validation.
- Payment creation.
- Payment verification.
- State transitions.
- Transaction management.
- Event creation.
- Gateway orchestration.

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
- Verify Signature
- Fetch Payment Details

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

- payment_intents
- payments
- payment_events

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

- metadata
- gatewayResponse
- eventData

Advantages

- Flexible schema.
- Complete gateway payload storage.
- Easier debugging.
- Future compatibility.

---

# 18. Business References

The project uses ULIDs instead of database IDs for external references.

Current references

- Payment Intent Reference
- Payment Reference
- Payment Event Reference

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

---

# 20. Current Architecture Status

## Completed

- Layered Architecture
- Gateway Abstraction
- Repository Pattern
- Manual Mapping
- Transaction Management
- Payment Intent Flow
- Payment Verification Flow
- Razorpay Integration
- JSONB Support
- ULID References
- Global Exception Handling
- Validation
- Auditing

---

# 21. Planned Architectural Enhancements

The following architectural components will be added in future phases.

- Razorpay Webhook Processing
- Read APIs
- Refund Module
- Payout Module
- Multiple Gateway Support
- Monitoring
- Metrics
- OpenAPI Documentation
- Integration Testing
- Unit Testing
- Production Hardening

---

# Architecture Summary

The current architecture provides a clean separation between business logic, persistence, and payment gateway integration. Business services remain independent of Razorpay-specific implementation, ensuring that future enhancements—such as additional payment gateways, refunds, payouts, or asynchronous webhook processing—can be introduced with minimal impact on the existing codebase.

This architecture establishes a scalable and production-oriented foundation for the StreamHub Payment Service.