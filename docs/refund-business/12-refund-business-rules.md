# Refund Service Roadmap

## Overview

The Refund Service is responsible for returning money to customers after a successful payment.

Unlike the Payment Service, where money flows from the customer to the merchant, the Refund Service manages the reverse flow—from the merchant back to the customer through Razorpay.

The Refund Service is designed as a production-ready, extensible module that supports auditing, webhook processing, partial refunds, idempotency, and future financial reconciliation.

---

# Goals

The Refund Service should provide:

- Production-grade refund lifecycle.
- Full and partial refunds.
- Multiple partial refunds.
- Secure Razorpay integration.
- Complete audit history.
- Webhook-driven status synchronization.
- Idempotent processing.
- Extensible architecture.

---

# Current Scope (Version 1)

The following features will be implemented in Version 1.

---

## Phase 8.1 – Business Understanding

Understand the refund domain before implementation.

### Refund Types

- Full Refund
- Partial Refund
- Multiple Partial Refunds

### Refund Speed

- Normal Refund
- Instant Refund (if supported by the gateway/account)

### Refund Scenarios

- Customer cancelled the order.
- Merchant cancelled the order.
- Duplicate payment.
- Fraud detected.
- Customer support approved.
- Merchant initiated refund.

---

## Refund Lifecycle

```
Payment SUCCESS

↓

Refund Requested

↓

Refund Processing

↓

Refund Successful
```

Alternative flow

```
Payment SUCCESS

↓

Refund Requested

↓

Refund Failed
```

---

## Deliverable

```
11-refund-overview.md
```

---

# Phase 8.2 – Functional Requirements

Version 1 supports

- Full Refund
- Partial Refund
- Multiple Partial Refunds
- Refund History
- Refund Status Tracking
- Refund Events
- Razorpay Refund API
- Razorpay Refund Webhooks
- Refund Speed Selection
- Refund Audit Trail

---

# Phase 8.3 – Database Design

## Tables

### refunds

Stores refund transactions.

### refund_events

Stores immutable refund event history.

---

## Relationships

```
PaymentIntent

1

↓

Many Payments

1

↓

Many Refunds

1

↓

Many Refund Events
```

---

## Refund Table Responsibilities

Store

- Refund Reference
- Payment Reference
- Gateway Refund Id
- Refund Amount
- Refund Reason
- Refund Speed
- Refund Status
- Gateway Response
- Requested By
- Requested At
- Processed At

---

## Database Design

Design

- Primary Keys
- Foreign Keys
- Unique Constraints
- Indexes
- Check Constraints

---

# Phase 8.4 – Refund State Machine

Define refund lifecycle.

```
REQUESTED

↓

PROCESSING

↓

SUCCESS
```

Alternative

```
REQUESTED

↓

FAILED
```

Define

- Allowed State Transitions
- Invalid State Transitions
- Terminal States

---

# Phase 8.5 – API Design

Design REST APIs.

### Create Refund

```
POST /refunds
```

### Get Refund

```
GET /refunds/{refundReference}
```

### List Refunds

```
GET /payments/{paymentReference}/refunds
```

Define

- Request DTOs
- Response DTOs
- Validation Rules
- Error Responses

---

# Phase 8.6 – Razorpay Integration

Study and integrate

- Create Refund API
- Fetch Refund API
- Refund Status
- Refund Failure
- Refund Speed
- Gateway Response

---

# Phase 8.7 – Business Rules

Validate

- Payment must exist.
- Payment must be SUCCESS.
- Refund amount must be greater than zero.
- Refund amount cannot exceed remaining refundable amount.
- Multiple partial refunds are allowed.
- Total refunded amount cannot exceed payment amount.
- Gateway Refund Id must be unique.
- Refund Reference must be unique.

Calculate

- Total Refunded Amount.
- Remaining Refundable Amount.

---

# Phase 8.8 – Core Implementation

Implement

- Flyway Migrations
- Entities
- Repositories
- DTOs
- Services
- Mappers
- Validation
- Gateway Integration
- Refund Processing

---

# Phase 8.9 – Refund Webhooks

Implement webhook support.

Supported Events

- refund.processed
- refund.failed

Webhook Responsibilities

- Verify Signature
- Parse Payload
- Route Event
- Update Refund
- Record Refund Event
- Idempotent Processing

---

# Phase 8.10 – Idempotency

Prevent duplicate processing.

Support

- Duplicate refund requests.
- Duplicate webhook deliveries.
- Duplicate refund events.

---

# Phase 8.11 – Testing

Test

- Full Refund
- Partial Refund
- Multiple Partial Refunds
- Invalid Refund Amount
- Duplicate Refund
- Refund Failure
- Refund Webhook
- Duplicate Webhook
- Refund Validation
- Remaining Refundable Amount

---

# Phase 8.12 – Production Hardening

Review

- Logging
- Exception Handling
- Transactions
- Concurrency
- Idempotency
- Security
- Performance

---

# Phase 8.13 – Documentation

Prepare

- Refund Overview
- Refund API
- Refund Webhooks
- Testing Guide

---

# Future Enhancements

The following features are intentionally excluded from Version 1.

---

## Settlement Tracking

Track

- Settlement Status
- Settlement Date
- Gateway Fee
- GST
- Net Settlement Amount
- Settlement Reports

---

## Merchant Financial Reporting

Track

- Merchant Earnings
- Total Refunded Amount
- Outstanding Refund Amount
- Settlement History

---

## Payout Service

Implement

- Contacts
- Fund Accounts
- Payouts
- Payout Webhooks
- Bank Transfers
- UPI Transfers

---

## Financial Reconciliation

Compare

- Internal Database
- Razorpay
- Bank Settlement

Detect

- Missing Payments
- Settlement Mismatch
- Refund Mismatch

---

## Monitoring & Metrics

Monitor

- Refund Success Rate
- Refund Failure Rate
- Webhook Success Rate
- API Latency
- Processing Time

---

## Advanced Refund Features

Future capabilities

- Scheduled Refunds
- Bulk Refunds
- Approval Workflow
- Refund Analytics
- Customer Notification Integration
- Automatic Refund Rules
- Retry Policies

---

# High-Level Implementation Flow

```
Merchant

↓

Create Refund Request

↓

Validate Business Rules

↓

Create Refund Record

↓

Call Razorpay Refund API

↓

Store Gateway Response

↓

Receive Razorpay Webhook

↓

Update Refund Status

↓

Create Refund Event

↓

Return Refund Status
```

---

# Architecture

```
PaymentIntent
      │
      └──────────────┐
                     │
                 Payment
                     │
         ┌───────────┴───────────┐
         │                       │
     Payment Events          Refunds
                                  │
                                  │
                           Refund Events
```

---

# Design Principles

- Every refund belongs to a successful payment.
- Refunds never belong directly to PaymentIntent.
- Every refund is fully auditable.
- Every state change is event-driven.
- Webhooks are the source of truth for asynchronous updates.
- Business operations must be idempotent.
- The Refund Service is independent of settlement and payout modules.