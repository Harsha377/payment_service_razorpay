# Project Overview

## 1. Introduction

The **StreamHub Payment Service** is a standalone payment microservice responsible for managing the complete payment lifecycle for the StreamHub platform.

It provides a centralized payment solution that abstracts payment gateway integration from business applications. Instead of allowing multiple services to communicate directly with a payment gateway, all payment-related operations are handled through this dedicated service.

The current implementation integrates with **Razorpay** and supports a complete Pay-In flow from payment initialization to payment verification.

The architecture has been designed so that additional payment gateways can be integrated in the future without changing the business logic.

---

# 2. Project Objectives

The primary objectives of this project are:

- Provide a centralized payment service.
- Support secure payment processing.
- Separate business logic from gateway-specific implementation.
- Maintain complete payment history.
- Track payment events for auditing.
- Provide production-ready architecture.
- Support future gateway integrations.
- Maintain extensibility and scalability.

---

# 3. Current Scope

The current version of the service supports both **Pay-In** and **Refund** operations.

### Implemented Capabilities

#### Payment Processing

- Payment Intent Creation
- Razorpay Order Creation
- Payment Creation
- Payment Verification
- Razorpay Signature Verification
- Fetch Payment Details from Razorpay
- Payment Status Management
- Payment Intent Status Management
- Payment Event Recording
- Payment Query APIs

#### Razorpay Webhooks

- Webhook Signature Verification
- payment.authorized
- payment.captured
- payment.failed
- order.paid
- Idempotent Webhook Processing

#### Refund Processing

- Full Refund
- Partial Refund
- Multiple Partial Refunds
- Refund Validation
- Remaining Refundable Amount Calculation
- Razorpay Refund API Integration
- Refund Event Recording
- Refund Query APIs

#### Refund Webhooks

- refund.processed
- refund.failed
- Idempotent Refund Webhook Processing

### Planned Future Enhancements

- Payout Service
- Settlement Management
- Reconciliation Engine
- Multiple Payment Gateway Support
- Monitoring & Metrics
- Notification Service
- Production Dashboard

# 4. Responsibilities

The Payment Service is responsible for:

- Creating payment intents.
- Creating payment records.
- Communicating with Razorpay.
- Verifying payment signatures.
- Fetching payment details from Razorpay.
- Updating payment status.
- Updating payment intent status.
- Recording payment events.
- Maintaining payment history.
- The Payment Service is responsible for:
- Creating payment intents.
- Creating payment records.
- Communicating with Razorpay.
- Verifying payment signatures.
- Fetching payment details from Razorpay.
- Updating payment status.
- Updating payment intent status.
- Recording payment events.
- Creating refunds.
- Processing full and partial refunds.
- Fetching refund details.
- Recording refund events.
- Processing Razorpay webhooks.
- Maintaining complete payment and refund history.

The Payment Service is **not responsible** for:

- User authentication
- Product management
- Order management
- Inventory
- Business-specific workflows

These responsibilities belong to the consuming services.

---

# 5. High-Level Architecture

```text
                 Client Application
                        │
                        ▼
             StreamHub Payment Service
                        │
         ┌──────────────┴──────────────┐
         ▼                             ▼
 Business Layer                 Gateway Layer
         │                             │
         └──────────────┬──────────────┘
                        ▼
                    Razorpay
                        │
         ┌──────────────┴──────────────┐
         ▼                             ▼
    Payment APIs                 Webhook Events
                        │
                        ▼
                   PostgreSQL
```

The business layer manages payment logic, while the gateway layer is responsible for communication with external payment providers.

This separation allows the payment service to remain independent of any specific payment gateway.

---

# 6. Payment Lifecycle

The payment lifecycle currently consists of two major stages.

## Stage 1 – Payment Initialization

```text
Client

↓

Create Payment Intent

↓

Create Razorpay Order

↓

Create Payment Record

↓

Create Payment Event

↓

Return Response
```

---

## Stage 2 – Payment Verification

```text
Customer Pays

↓

Frontend Receives Payment Details

↓

Verify Payment

↓

Verify Razorpay Signature

↓

Fetch Payment Details

↓

Update Payment

↓

Update Payment Intent

↓

Record Payment Event

↓

Return Success
```

## Stage 3 – Refund Processing

```text
Merchant Requests Refund

↓

Validate Payment

↓

Validate Refund Amount

↓

Create Refund

↓

Create Razorpay Refund

↓

Record Refund Event

↓

Return Response
```

---

## Stage 4 – Refund Webhook Processing

```text
Razorpay

↓

refund.processed / refund.failed

↓

Verify Webhook Signature

↓

Fetch Refund Details

↓

Update Refund

↓

Record Refund Event

↓

Return Success
```

---

# 7. Database Overview

The service currently maintains five primary tables.

## payment_intents

Represents a customer's payment request.

---

## payments

Represents an individual payment processed through a payment gateway.

---

## payment_events

Immutable audit history of payment events.

---

## refunds

Represents a refund initiated against a successful payment.

---

## refund_events

Immutable audit history of refund events.

Relationship

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


## payment_intents

Represents a customer's payment request.

---

## payments

Represents an individual payment processed through a payment gateway.

---

## payment_events

Stores the complete audit history of payment-related events.

Relationship:

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

# 8. Current Project Status

## Completed

### Core Infrastructure

- Project Setup
- Layered Architecture
- PostgreSQL Integration
- Flyway Migrations
- Entity Design
- Repository Layer
- Manual DTO Mapping
- Validation
- Global Exception Handling
- Transaction Management

### Payment Module

- Payment Intent Flow
- Razorpay Order Creation
- Payment Verification
- Signature Verification
- Fetch Payment Details
- Payment Events
- Payment Query APIs

### Payment Webhooks

- payment.authorized
- payment.captured
- payment.failed
- order.paid
- Idempotent Processing

### Refund Module

- Full Refund
- Partial Refund
- Multiple Partial Refunds
- Remaining Refundable Amount Validation
- Razorpay Refund Integration
- Refund Events
- Refund Query APIs

### Refund Webhooks

- refund.processed
- refund.failed
- Idempotent Processing

### Additional Features

- JSONB Support
- ULID Business References
- Production Logging
- Manual End-to-End Testing

---

## In Progress

- Technical Documentation

---

## Planned

- Payout Module
- Settlement Management
- Reconciliation
- Multiple Gateway Support
- Metrics & Monitoring
- OpenAPI Documentation
- Unit Tests
- Integration Tests
- Performance Testing
- Security Hardening

---

# 9. Design Principles

This project follows the following engineering principles.

- Clean Layered Architecture
- Separation of Concerns
- Gateway Abstraction
- Production-Oriented Design
- Manual DTO Mapping
- Transactional Consistency
- Strong Validation
- Auditable Payment History
- Extensible Payment Gateway Integration
- Minimal Coupling Between Modules
- Idempotent Payment Processing
- Idempotent Webhook Processing
- Immutable Event Audit Trail

---
# 10. Architecture Decisions

- Business references use ULIDs instead of database IDs.
- Amounts are stored in the smallest currency unit (paise).
- Manual DTO mapping is used instead of MapStruct.
- Payment and Refund events are immutable audit records.
- Razorpay integration is isolated behind a gateway abstraction.
- Payment verification and webhook processing are designed to be idempotent.
- PostgreSQL JSONB is used for gateway payload storage.
- Flyway is used for version-controlled database migrations.

# 11. Future Vision

The long-term vision of this service is to evolve into a reusable enterprise payment platform capable of supporting:

- Multiple Payment Gateways
- Pay-In Processing
- Refund Management
- Payout Management
- Settlement Management
- Financial Reconciliation
- Subscription Billing
- Multi-Tenant Support
- Event-Driven Architecture
- Monitoring & Observability
- Distributed Tracing
- High Availability
- Horizontal Scalability
- Production Operations Dashboard

The current implementation establishes the foundation required to achieve these future enhancements while maintaining a clean and extensible architecture.