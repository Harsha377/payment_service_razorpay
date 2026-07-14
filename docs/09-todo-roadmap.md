# TODO & Roadmap

## Overview

This document tracks the implementation roadmap of the StreamHub Payment Service.

The roadmap is organized into phases, allowing incremental development while maintaining a production-oriented architecture.

Each phase represents a functional milestone.

---

# Project Progress

| Phase | Status |
|---------|--------|
| Phase 1 – Project Setup | ✅ Completed |
| Phase 2 – Database Design | ✅ Completed |
| Phase 3 – Razorpay Integration | ✅ Completed |
| Phase 4 – Business Layer | ✅ Completed |
| Phase 5 – Payment Intent Flow | ✅ Completed |
| Phase 6 – Payment Verification Flow | ✅ Completed |
| Phase 7 – Razorpay Webhooks | ⏳ Pending |
| Phase 8 – Read APIs | ⏳ Pending |
| Phase 9 – Testing | ⏳ Pending |
| Phase 10 – Production Hardening | ⏳ Pending |
| Phase 11 – Refund Module | 🔲 Planned |
| Phase 12 – Payout Module | 🔲 Planned |
| Phase 13 – Multi-Gateway Support | 🔲 Planned |

---

# Completed Milestones

## Phase 1 – Project Setup

Status

✅ Completed

Implemented

- Spring Boot Project
- Maven Configuration
- PostgreSQL
- Flyway
- Global Exception Handling
- Validation
- ULID Utility
- BaseEntity
- Auditing
- Configuration Classes

---

## Phase 2 – Database Design

Status

✅ Completed

Implemented

### payment_intents

- Entity
- Repository
- Flyway Migration
- Constraints
- Indexes
- JSONB Metadata

### payments

- Entity
- Repository
- Relationships
- Gateway Fields
- JSONB Gateway Response

### payment_events

- Entity
- Repository
- Event Tracking
- JSONB Event Data

---

## Phase 3 – Razorpay Integration

Status

✅ Completed

Implemented

- Razorpay Configuration
- Razorpay Client
- Gateway Layer
- Create Order
- Verify Signature
- Fetch Payment Details
- Payment Method Mapping
- Gateway Response Storage

---

## Phase 4 – Business Layer

Status

✅ Completed

Implemented

- PaymentIntentService
- PaymentService
- PaymentEventService
- Manual Mapping
- Transaction Management

---

## Phase 5 – Payment Intent Flow

Status

✅ Completed

Implemented

- Create Payment Intent
- Request Validation
- Payment Intent Persistence
- Razorpay Order Creation
- Payment Creation
- Payment Event Recording
- API Response

Verified

- Payment Intent Created
- Razorpay Order Created
- Database Records Created
- Frontend Checkout Working

---

## Phase 6 – Payment Verification

Status

✅ Completed

Implemented

- Verify Payment API
- Payment Lookup
- Duplicate Verification Prevention
- Payment State Validation
- Signature Verification
- Fetch Payment Details
- Payment Method Mapping
- Gateway Status Update
- Gateway Response Storage
- Payment Update
- Payment Intent Update
- PAYMENT_SUCCESS Event

Verified

- Payment Verification
- Database Updates
- Gateway Response Stored
- Payment Method Stored
- Gateway Status Stored
- Payment Event Stored

---

# Current Status

The synchronous Pay-In flow is fully implemented and tested.

Completed flow

```text
Create Payment Intent

↓

Create Razorpay Order

↓

Create Payment

↓

Open Checkout

↓

Customer Pays

↓

Verify Payment

↓

Verify Signature

↓

Fetch Payment Details

↓

Update Database

↓

Record Event
```

---

# Next Phase

# Phase 7 – Razorpay Webhooks

Priority

⭐⭐⭐⭐⭐ Critical

Status

⏳ Pending

Objective

Synchronize payment status directly from Razorpay using asynchronous webhooks.

Tasks

- Create Webhook Controller
- Configure Webhook Secret
- Verify Webhook Signature
- Parse Webhook Payload
- Process Razorpay Events
- Update Payment
- Update Payment Intent
- Record Webhook Events
- Prevent Duplicate Webhook Processing
- Return HTTP 200 Response

Events to Support

- payment.authorized
- payment.captured
- payment.failed
- order.paid

Deliverables

- Production-ready webhook endpoint
- Signature verification
- Event processing
- Audit logging

---

# Phase 8 – Read APIs

Priority

⭐⭐⭐⭐

Status

⏳ Pending

Endpoints

```http
GET /payments/{paymentReference}
```

```http
GET /payment-intents/{paymentIntentReference}
```

```http
GET /payments/{paymentReference}/events
```

Implementation

- Controllers
- DTOs
- Mappers
- Repository Queries
- Validation
- Error Handling

Deliverables

- Payment Details API
- Payment Intent API
- Payment History API

---

# Phase 9 – Testing

Priority

⭐⭐⭐⭐⭐

Status

⏳ Pending

Unit Tests

- Services
- Mappers
- Utility Classes

Integration Tests

- Controllers
- Repositories
- Database
- Gateway Layer

End-to-End Tests

- Create Payment Intent
- Verify Payment
- Database Validation

Coverage Goal

Minimum

```text
80%
```

---

# Phase 10 – Production Hardening

Priority

⭐⭐⭐⭐⭐

Status

⏳ Pending

Planned Work

## Idempotency

- Duplicate Request Prevention
- Idempotency Keys

---

## Logging

- Structured Logging
- Correlation IDs
- Gateway Logs
- Business Logs

---

## Monitoring

- Micrometer
- Prometheus
- Grafana


---

## API Documentation

- OpenAPI
- Swagger

---

## Security

- API Authentication
- API Authorization
- Secret Management
- Vault Integration

---

## Performance

- Database Optimization
- Connection Pool Tuning
- Query Optimization

---

## Observability

- Metrics
- Tracing
- Health Checks

---

# Phase 11 – Refund Module

Priority

⭐⭐⭐

Status

🔲 Planned

Features

- Refund API
- Partial Refund
- Full Refund
- Refund Events
- Refund Status
- Refund History

---

# Phase 12 – Payout Module

Priority

⭐⭐⭐

Status

🔲 Planned

Features

- Beneficiary Management
- Fund Account Management
- Payout Requests
- Payout Verification
- Payout Events
- Payout Status

---

# Phase 13 – Multi-Gateway Support

Priority

⭐⭐⭐

Status

🔲 Planned

Future Gateways

- Stripe
- Cashfree
- PhonePe
- PayU

Implementation

- Gateway Factory
- Gateway Selection Strategy
- Routing Layer
- Gateway Configuration

---

# Future Improvements

Potential Enhancements

- Payment Retry Support
- Scheduled Retry Jobs
- Dead Letter Handling
- Event Streaming
- Kafka Integration
- Redis Caching
- Distributed Locking
- Rate Limiting
- Multi-Currency Support
- Settlement Reconciliation
- Subscription Billing
- Invoice Generation
- Admin Dashboard

---

# Documentation Checklist

| Document | Status |
|-----------|--------|
| README | ✅ Completed |
| Project Overview | ✅ Completed |
| Architecture | ✅ Completed |
| Database Design | ✅ Completed |
| Payment Intent Flow | ✅ Completed |
| Payment Verification Flow | ✅ Completed |
| Razorpay Integration | ✅ Completed |
| API Reference | ✅ Completed |
| Known Design Decisions | ✅ Completed |
| TODO & Roadmap | ✅ Completed |

---

# Current Project Health

| Area | Status |
|------|--------|
| Architecture | ✅ Stable |
| Database Design | ✅ Stable |
| Payment Intent Flow | ✅ Complete |
| Payment Verification | ✅ Complete |
| Razorpay Integration | ✅ Complete |
| Documentation | ✅ Complete (Current Phase) |
| Webhooks | ⏳ Next |
| Testing | ⏳ Pending |
| Production Hardening | ⏳ Pending |

---

# Immediate Next Goal

The next implementation milestone is **Phase 7 – Razorpay Webhooks**.

This phase introduces asynchronous payment synchronization, ensuring that the Payment Service remains consistent with Razorpay even if the frontend never calls the payment verification API.

Once Phase 7 is completed, the service will support both:

- **Synchronous payment confirmation** (frontend verification)
- **Asynchronous payment confirmation** (Razorpay webhooks)

This will significantly improve the reliability and production readiness of the StreamHub Payment Service.