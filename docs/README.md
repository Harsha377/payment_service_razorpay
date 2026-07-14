# StreamHub Payment Service

A production-oriented Payment Service responsible for managing payment intents, payment processing, payment verification, and payment event tracking using Razorpay.

This service is designed with a clean layered architecture and acts as an independent payment module that can be integrated with multiple business applications.

---

# Project Status

Current Status

✅ Pay-In Flow Completed

Implemented Features

- Payment Intent Creation
- Razorpay Order Creation
- Payment Creation
- Payment Verification
- Razorpay Signature Verification
- Fetch Payment Details from Razorpay
- Payment Event Tracking
- PostgreSQL Persistence
- JSONB Support
- ULID References
- Global Exception Handling
- Request Validation

Upcoming Features

- Razorpay Webhooks
- Read APIs
- Unit Testing
- Integration Testing
- Metrics & Monitoring
- OpenAPI Documentation
- Production Hardening

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
│
├── common
│
├── config
│
├── gateway
│
│     ├── config
│     ├── dto
│     ├── mapper
│     ├── service
│
├── payment
│
│     ├── controller
│     ├── dto
│     ├── entity
│     ├── enums
│     ├── mapper
│     ├── repository
│     ├── service
│
└── PaymentServiceApplication
```

---

# Database

Core Tables

- payment_intents
- payments
- payment_events

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

Create Payment Intent

↓

Create Razorpay Order

↓

Create Payment

↓

Create Payment Event

↓

Return Response
```

---

## 2. Verify Payment

```text
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
```

---

# Current APIs

Payment Intent

```
POST /api/v1/payment-intents
```

Payment Verification

```
POST /api/v1/payments/verify
```

---

# Current Database Status

The following entities are fully implemented.

- PaymentIntentEntity
- PaymentEntity
- PaymentEventEntity

Implemented Features

- Foreign Keys
- Constraints
- Indexes
- JSONB Columns
- Auditing
- ULID References

---

# Security

The Razorpay Key Secret is never exposed outside the backend.

The frontend receives only the public Gateway Key required for Razorpay Checkout.

Payment verification is performed using Razorpay Signature Verification.

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

---

# Documentation

Additional documentation is available inside the `docs/` directory.

Planned documents include:

- Project Overview
- Architecture
- Database Design
- Payment Intent Flow
- Payment Verification Flow
- Razorpay Integration
- API Reference
- Design Decisions
- Roadmap

---

# Roadmap

Completed

- Project Setup
- Database Design
- Razorpay Integration
- Payment Intent Creation
- Payment Verification

In Progress

- Documentation

Upcoming

- Razorpay Webhooks
- Read APIs
- Testing
- Production Hardening

---

# License

This project is intended for educational purposes and production architecture learning.