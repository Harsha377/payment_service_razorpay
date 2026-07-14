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

The current version of the service focuses only on **Pay-In** operations.

Implemented capabilities include:

- Payment Intent Creation
- Razorpay Order Creation
- Payment Creation
- Payment Verification
- Razorpay Signature Verification
- Fetch Payment Details from Razorpay
- Payment Event Recording

The following features are planned for future phases:

- Razorpay Webhooks
- Payment Query APIs
- Refunds
- Payouts
- Multiple Payment Gateway Support
- Monitoring and Metrics
- Production Hardening

---

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

---

# 7. Database Overview

The service currently maintains three primary tables.

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

- Project Setup
- Layered Architecture
- PostgreSQL Integration
- Flyway Migrations
- Entity Design
- Repository Layer
- Service Layer
- Manual Mapping
- Validation
- Global Exception Handling
- Razorpay Configuration
- Razorpay Order Creation
- Payment Intent Flow
- Payment Verification Flow
- Signature Verification
- Fetch Payment Details
- JSONB Support
- ULID Business References
- Transaction Management

---

## In Progress

- Project Documentation

---

## Planned

- Razorpay Webhooks
- Payment Query APIs
- Unit Testing
- Integration Testing
- Metrics & Monitoring
- OpenAPI Documentation
- Production Hardening
- Refund Support
- Payout Support

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

---

# 10. Future Vision

The long-term vision of this service is to become a reusable payment platform capable of supporting:

- Multiple payment gateways
- Multiple business applications
- Refund management
- Payout management
- Subscription billing
- Event-driven integrations
- Monitoring and observability
- High availability and scalability

The current implementation establishes the foundation required to achieve these future enhancements while maintaining a clean and extensible architecture.