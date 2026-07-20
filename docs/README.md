# StreamHub Payment Service

The **StreamHub Payment Service** is a production-oriented payment microservice responsible for managing the complete **Pay-In** and **Refund** lifecycle for the StreamHub platform.

The service provides a centralized payment abstraction over Razorpay, handling payment initialization, payment verification, refund processing, webhook synchronization, and immutable event tracking.

It follows a clean layered architecture with complete separation between business logic and gateway-specific implementation, allowing additional payment gateways to be integrated with minimal changes to the business layer.

---

## Project Status

## Current Status

✅ Pay-In Module Completed
✅ Refund Module Completed
✅ Razorpay Webhook Processing Completed
✅ Payment Intent Idempotency
✅ Refund Idempotency
✅ Scheduled Idempotency Cleanup
✅ End-to-End Manual Testing Completed


## Implemented Features

## Payment Processing

- Payment Intent Creation
- Razorpay Order Creation
- Payment Verification
- Payment Query APIs
- Payment Event Tracking
- Idempotent Payment Intent Creation

## Refund Processing

- Full Refunds
- Partial Refunds
- Multiple Partial Refunds
- Refund Query APIs
- Refund Event Tracking
- Idempotent Refund Creation

## Razorpay Integration

- Order Creation
- Signature Verification
- Fetch Payment Details
- Refund Creation
- Fetch Refund Details
- Webhook Signature Verification

## Infrastructure

- PostgreSQL
- Flyway Migrations
- JSONB Support
- ULID Business References
- Manual DTO Mapping
- Global Exception Handling
- Request Validation
- Transaction Management
- Idempotency Keys
- Request Hash Validation
- Scheduled Cleanup

### Upcoming Features

- Automated Unit Testing
- Integration Testing
- OpenAPI Documentation
- Monitoring & Metrics
- Production Hardening
- Payout Module
- Multi-Gateway Support

---

# Technology Stack

Backend

- Java 21
- Spring Boot 3.5.11
- Spring Data JPA
- Spring Validation
- PostgreSQL
- Flyway
- Razorpay Java SDK
- Jackson
- Lombok

Database

- PostgreSQL

Build Tool

- Maven

Payment Gateway

- Razorpay

---

# Architecture

The project follows a layered architecture.

```text
Client

↓

Controllers

↓

Business Services

↓

Repositories

↓

PostgreSQL

            │

            ▼

Gateway Layer

↓

Razorpay APIs

↓

Webhook Processing
```

The payment gateway is isolated from the business layer through a dedicated Gateway module.

```text
Payment Service
        │
        ▼
Gateway Layer
        │
        ▼
Razorpay
```

This allows future support for additional payment gateways without affecting business logic.

---

# Project Structure

```text
src/main/java

├── common
├── gateway
├── idempotency
├── payment
├── refund
├── webhook
└── PaymentServiceApplication
```

---

# Database

Core Tables

- payment_intents
- payments
- payment_events
- refunds
- refund_events
- idempotency_keys

Relationship

```text
PaymentIntent
      │
      └───────< Payment
                     │
                     ├──────< PaymentEvent
                     │
                     └──────< Refund
                                 │
                                 └──────< RefundEvent
```

---

# Implemented Payment Flow

## 1. Create Payment Intent

```text
Client

↓

Create Payment Intent API

↓

Validate Request

↓

Generate Request Hash

↓

Check Idempotency Key

↓

Existing Request?

├── Yes
│
│   ↓
│ Return Existing Response
│
└── No
    ↓
Create Payment Intent
    ↓
Create Razorpay Order
    ↓
Create Payment
    ↓
Create Payment Event
    ↓
Save Idempotency Record
    ↓
Return Response
```

---

## 2. Verify Payment

Frontend

↓

Razorpay Checkout

↓

Payment Success

↓

Verify Payment API

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

Return Response

---
### Refund Flow
Merchant

↓

Create Refund API

↓

Validate Request

↓

Generate Request Hash

↓

Check Idempotency Key

↓

Existing Request?

├── Yes
│
│ Return Existing Response
│
└── No
↓
Validate Refund
↓
Create Refund
↓
Create Razorpay Refund
↓
Update Refund
↓
Create Refund Event
↓
Save Idempotency Record
↓
Return Response


---
### Webhook Flow

Razorpay

↓

Webhook

↓

Verify Signature

↓

Process Event

↓

Update Database

↓

Record Event

↓

Return HTTP 200



---

## idempotency flow
Client

↓

POST Request

↓

Idempotency Key

↓

Already Exists?

├── Yes
│
│ Return Stored Response
│
└── No
↓
Execute Business Logic
↓
Store Response
↓
Return Response

---

## Architecture Flow

                    Client
                       │
                       ▼
                 REST Controllers
                       │
                       ▼
               Business Services
               ┌────────┴─────────┐
               ▼                  ▼
        Repositories        Gateway Layer
               │                  │
               ▼                  ▼
          PostgreSQL         Razorpay APIs

---
# Current APIs

Payment Intent

POST /api/v1/payment-intents

Payments

POST /api/v1/payments/verify

GET /api/v1/payments/{paymentReference}

Refunds

POST /api/v1/refunds

GET /api/v1/refunds/{refundReference}

GET /api/v1/refunds/payment/{paymentReference}

Webhooks

POST /api/v1/webhooks/razorpay

---

# Current Database Status

The following entities are fully implemented.

- PaymentIntentEntity
- PaymentEntity
- PaymentEventEntity
- RefundEntity
- RefundEventEntity
- IdempotencyKeyEntity

Implemented Features

- Foreign Keys
- Constraints
- Indexes
- JSONB Columns
- Auditing
- ULID References

---

## Security

- Razorpay Secret Key is never exposed outside the backend.
- Only the Razorpay Public Key (Key ID) is returned to the frontend.
- All payment signatures are verified before updating business records.
- All webhook requests are authenticated using Razorpay webhook signature verification.
- Payment Intent creation is idempotent.
  Refund creation is idempotent.
  Duplicate requests safely return the previously stored response.
  Request hash validation prevents reuse of an idempotency key with a different payload.

---

# Development Principles

This project follows the following design principles.

- Clean Layered Architecture
- Separation of Business Logic and Gateway Logic
- Manual Mapping
- Transaction Management
- Production-Oriented Design
- JSONB Support
- ULID Based Business References
- Strong Validation
- Global Exception Handling
- Gateway Abstraction
- Idempotent Processing
- Immutable Event Audit Trail

---

# Documentation

Additional documentation is available inside the `docs/` directory.

Planned documents include:

* README
* 
* Project Overview
* 
* Architecture
* 
* Database Design
* 
* Payment Intent Flow
* 
* Payment Verification Flow
* 
* Webhook Flow
* 
* Refund Flow
* 
* Razorpay Integration
* 
* API Reference
* 
* Business Rules
* 
* Testing Guide
* 
* Roadmap

---

## Roadmap

### Completed

- Project Setup
- Database Design
- Razorpay Integration
- Payment Module
- Payment Webhook Processing
- Refund Module
- Idempotency
- Refund Webhook Processing
- End-to-End Manual Testing
- Technical Documentation

### In Progress

- Automated Testing

### Planned

- Production Hardening
- Payout Module
- Settlement & Reconciliation
- Multi-Gateway Support

---

## Key Features

- Production-oriented layered architecture
- Razorpay Pay-In integration
- Full and partial refund support
- Payment and refund webhook processing
- Manual DTO mapping
- PostgreSQL JSONB support
- ULID-based business references
- Immutable event tracking
- Transaction management
- Flyway database migrations
- Gateway abstraction for future payment providers

---
## Project Modules

| Module | Responsibility |
|---------|----------------|
| common | Shared infrastructure, exceptions, utilities, configuration |
| gateway | Razorpay integration and gateway abstraction |
| payment | Payment Intent and payment lifecycle |
| refund | Refund lifecycle |
| webhook | Razorpay webhook processing |

---

# License

This project is intended for educational purposes and production architecture learning.