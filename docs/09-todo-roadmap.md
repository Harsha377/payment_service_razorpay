# TODO & Roadmap

## Overview

This document tracks the implementation roadmap of the StreamHub Payment Service.

The roadmap is organized into phases, allowing incremental development while maintaining a production-oriented architecture.

Each phase represents a functional milestone.

---

# Project Progress

| Phase                                 | Status                         |
| ------------------------------------- | ------------------------------ |
| Phase 1 – Project Setup               | ✅ Completed                    |
| Phase 2 – Database Design             | ✅ Completed                    |
| Phase 3 – Razorpay Integration        | ✅ Completed                    |
| Phase 4 – Business Layer              | ✅ Completed                    |
| Phase 5 – Payment Intent Flow         | ✅ Completed                    |
| Phase 6 – Payment Verification Flow   | ✅ Completed                    |
| Phase 7 – Razorpay Webhook Processing | ✅ Completed                    |
| Phase 8 – Payment Query APIs          | ✅ Completed *(if implemented)* |
| Phase 9 – Refund Module               | ✅ Completed                    |
| Phase 10 – Refund Webhook Processing  | ✅ Completed                    |
| Phase 11 – Manual End-to-End Testing  | ✅ Completed                    |
| Phase 12 – Automated Testing          | ⏳ Pending                      |
| Phase 13 – Production Hardening       | ⏳ Pending                      |
| Phase 14 – Payout Module              | 🔲 Planned                     |
| Phase 15 – Multi-Gateway Support      | 🔲 Planned                     |


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

### refunds

- Entity
- Repository
- Flyway Migration
- Constraints
- Indexes
- JSONB Support

### refund_events

- Entity
- Repository
- Event Tracking
- JSONB Event Data

---

## Phase 3 – Razorpay Integration

Status

✅ Completed

Implemented

* Create Order
* 
* Verify Signature
* 
* Fetch Payment Details
* 
* Create Refund
* 
* Fetch Refund Details
* 
* Verify Webhook Signature
* 
* Payment Method Mapping
* 
* Gateway Response Storage
* 
* Gateway Notes Storage

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
- RefundService 
- RefundEventService 
- WebhookService

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
- Idempotent Verification
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

---
### Phase 7 – Razorpay Webhook Processing

Status

✅ Completed

Implemented

Webhook Controller
Signature Verification
Event Routing
payment.authorized
payment.captured
payment.failed
order.paid
Idempotent Processing
Payment Updates
Payment Intent Updates
Payment Events

Verified

Webhook Signature Validation
Payment Updates
Duplicate Webhook Handling
Event Recording

---

---
### Phase 8 – Refund Module

Status

✅ Completed

Implemented

* Refund API
* Partial Refund
* Full Refund
* Multiple Partial Refunds
* Remaining Refund Validation
* Refund Persistence
* Refund Events
* Refund Query APIs
* Razorpay Refund Integration

Verified

* Full Refund
* Partial Refund
* Database Updates
* Razorpay Refund
* Event Recording
---
---
### Phase 9 – Refund Webhook Processing

Status

✅ Completed

Implemented

* refund.processed
* refund.failed
* Signature Verification
* Refund Updates
* Refund Event Recording
* Idempotent Processing

Verified

* Refund Status Updates
* Duplicate Handling
* Event Recording
---

---
### Phase 10 – Manual End-to-End Testing

Status

✅ Completed

Executed Scenarios

* Payments
* Payment Intent Creation
* Checkout
* Successful Payment
* Signature Verification
* Webhook Processing
* Duplicate Verification
* Refunds
* Full Refund
* Partial Refund
* Multiple Partial Refunds
* Remaining Refund Validation
* Refund Webhooks

---

# Implemented Lifecycle


Completed flow

```text
Implemented Lifecycle

Payment Intent

↓

Checkout

↓

Payment Verification

↓

Payment Webhooks

↓

Refund

↓

Refund Webhooks
```

---

# Next Phase

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

## Future Webhook Enhancements

The following webhook events are planned for future releases.

- Settlement Webhooks
- Payout Webhooks
- Dispute Webhooks
- Subscription Webhooks

---

## Future Payment Enhancements

- Manual Capture Support
- Gateway Fee Tracking
- Net Settlement Amount
- Merchant Account Tracking
- Settlement Reconciliation
- Payment Mismatch Review Workflow
- Fraud Detection
- Payment Retry Analytics
- Payment Links 
- Smart Collect 
- Route 
- Virtual Accounts

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
- Outbox Pattern 
- Inbox Pattern 
- Distributed Transactions 
- Saga Support

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

| Area                 | Status     |
| -------------------- | ---------- |
| Architecture         | ✅ Stable   |
| Database Design      | ✅ Stable   |
| Payment Module       | ✅ Complete |
| Payment Webhooks     | ✅ Complete |
| Refund Module        | ✅ Complete |
| Refund Webhooks      | ✅ Complete |
| Manual Testing       | ✅ Complete |
| Documentation        | ✅ Complete |
| Automated Testing    | ⏳ Pending  |
| Production Hardening | ⏳ Pending  |
| Payout Module        | 🔲 Planned |


---

# Immediate Next Goal

The next implementation milestone is **Automated Testing**.

Priority areas include:

- Unit Testing
- Integration Testing
- Webhook Testing
- Refund Testing
- End-to-End Test Automation

Once automated testing is completed, the focus will shift to **Production Hardening**, including monitoring, observability, security, and performance optimization.

After the service is production-ready, development will continue with the **Payout Module**, extending the payment platform beyond Pay-In and Refund capabilities.