# API Reference

## 1. Overview

This document describes the REST APIs currently implemented by the StreamHub Payment Service.

Current APIs

- Create Payment Intent
- Verify Payment

Future APIs

- Get Payment
- Get Payment Intent
- Get Payment Events
- Razorpay Webhooks
- Refund APIs
- Payout APIs

---

# 2. Base URL

Development

```text
http://localhost:8083/api/v1
```

Example

```text
http://localhost:8083/api/v1/payment-intents
```

---

# 3. Authentication

Current Version

Authentication is **not implemented**.

Future versions will support

- API Keys
- OAuth2
- JWT
- Service-to-Service Authentication

---

# 4. Content Type

Request

```http
Content-Type: application/json
```

Response

```http
Content-Type: application/json
```

---

# 5. Create Payment Intent

## Endpoint

```http
POST /payment-intents
```

---

## Description

Creates a Payment Intent, creates a Razorpay Order, creates a Payment record, records the initial payment event, and returns the information required to launch Razorpay Checkout.

---

## Request

```json
{
    "orderReference": "01K0ORDER00000000000000001",
    "customerReference": "01K0CUSTOMER000000000001",
    "amount": 10000,
    "currency": "INR",
    "description": "Premium Subscription"
}
```

---

## Request Fields

| Field | Type | Required | Description |
|---------|------|----------|-------------|
| orderReference | String | Yes | Business order reference |
| customerReference | String | Yes | Customer reference |
| amount | Long | Yes | Amount in the smallest currency unit (Paise for INR) |
| currency | String | Yes | ISO 4217 currency code |
| description | String | No | Payment description |

---

## Success Response

HTTP Status

```http
201 Created
```

Example

```json
{
    "paymentIntentReference": "01KXXXXXXXXXXXXXXX",
    "paymentReference": "01KYYYYYYYYYYYYYYY",
    "gatewayName": "RAZORPAY",
    "gatewayKey": "rzp_test_xxxxxxxxx",
    "gatewayOrderId": "order_Q2ABC123",
    "amount": 10000,
    "currency": "INR",
    "status": "CREATED"
}
```

---

## Business Flow

```text
Client

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

## Possible Errors

### Validation Error

HTTP

```http
400 Bad Request
```

Example

```json
{
    "message": "Amount must be greater than zero."
}
```

---

### Duplicate Order

HTTP

```http
409 Conflict
```

Example

```json
{
    "message": "Payment Intent already exists."
}
```

---

### Gateway Error

HTTP

```http
500 Internal Server Error
```

Example

```json
{
    "message": "Failed to create Razorpay order."
}
```

---

# 6. Verify Payment

## Endpoint

```http
POST /payments/verify
```

---

## Description

Verifies the Razorpay payment signature, fetches payment details from Razorpay, updates the Payment and Payment Intent records, and records a payment success event.

---

## Request

```json
{
    "paymentReference": "01KYYYYYYYYYYYYYYY",
    "gatewayOrderId": "order_Q2ABC123",
    "gatewayPaymentId": "pay_Q2XYZ456",
    "gatewaySignature": "xxxxxxxxxxxxxxxxxxxxxxxx"
}
```

---

## Request Fields

| Field | Type | Required | Description |
|---------|------|----------|-------------|
| paymentReference | String | Yes | Business payment reference |
| gatewayOrderId | String | Yes | Razorpay Order ID |
| gatewayPaymentId | String | Yes | Razorpay Payment ID |
| gatewaySignature | String | Yes | Razorpay Signature |

---

## Success Response

HTTP Status

```http
200 OK
```

Example

```json
{
    "paymentReference": "01KYYYYYYYYYYYYYYY",
    "paymentIntentReference": "01KXXXXXXXXXXXXXXX",
    "gatewayPaymentId": "pay_Q2XYZ456",
    "status": "SUCCESS",
    "message": "Payment verified successfully."
}
```

---

## Business Flow

```text
Find Payment

↓

Validate State

↓

Verify Signature

↓

Fetch Payment Details

↓

Update Payment

↓

Update Payment Intent

↓

Create PAYMENT_SUCCESS Event

↓

Return Response
```

---

## Database Updates

### payment_intents

```text
CREATED

↓

COMPLETED
```

---

### payments

Updated Fields

```text
gatewayPaymentId

gatewaySignature

paymentMethod

gatewayStatus

gatewayResponse

status

paidAt
```

---

### payment_events

```text
PAYMENT_CREATED

↓

PAYMENT_SUCCESS
```

---

## Possible Errors

### Validation Error

HTTP

```http
400 Bad Request
```

Example

```json
{
    "message": "Gateway Payment ID is required."
}
```

---

### Invalid Signature

HTTP

```http
400 Bad Request
```

Example

```json
{
    "message": "Invalid payment signature."
}
```

---

### Payment Not Found

HTTP

```http
404 Not Found
```

Example

```json
{
    "message": "Payment not found."
}
```

---

### Already Verified

HTTP

```http
409 Conflict
```

Example

```json
{
    "message": "Payment is already verified."
}
```

---

### Invalid Payment State

HTTP

```http
400 Bad Request
```

Example

```json
{
    "message": "Payment cannot be verified in the current state."
}
```

---

### Gateway Failure

HTTP

```http
500 Internal Server Error
```

Example

```json
{
    "message": "Failed to fetch payment details from Razorpay."
}
```

---

# 7. Standard HTTP Status Codes

| Status Code | Meaning |
|-------------|---------|
| 200 | Request processed successfully |
| 201 | Resource created successfully |
| 400 | Invalid request |
| 404 | Resource not found |
| 409 | Business conflict |
| 500 | Internal server error |

---

# 8. Error Response Format

All API errors follow a consistent response structure.

Example

```json
{
    "timestamp": "2026-07-14T12:15:35Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid payment signature.",
    "path": "/api/v1/payments/verify"
}
```

---

# 9. Validation Rules

## Create Payment Intent

| Field | Validation |
|---------|------------|
| orderReference | Required |
| customerReference | Required |
| amount | Greater than zero |
| currency | Three uppercase characters |
| description | Optional |

---

## Verify Payment

| Field | Validation |
|---------|------------|
| paymentReference | Required |
| gatewayOrderId | Required |
| gatewayPaymentId | Required |
| gatewaySignature | Required |

---

# 10. Idempotency

Current Version

Idempotency is **not yet implemented**.

Future versions will support idempotent payment creation to prevent duplicate requests caused by retries or network failures.

---

# 11. API Versioning

Current Version

```text
/api/v1
```

Future versions will follow URI versioning.

Examples

```text
/api/v2
```

without breaking existing clients.

---

# 12. Future APIs

The following endpoints are planned.

## Payment APIs

```http
GET /payments/{paymentReference}
```

```http
GET /payment-intents/{paymentIntentReference}
```

```http
GET /payments/{paymentReference}/events
```

---

## Webhook

```http
POST /webhooks/razorpay
```

---

## Refund

```http
POST /refunds
```

---

## Payout

```http
POST /payouts
```

---

# API Summary

The current API exposes two production-ready endpoints:

1. **Create Payment Intent** – Initializes a payment, creates a Razorpay Order, persists business records, and returns the information required to launch Razorpay Checkout.

2. **Verify Payment** – Verifies the Razorpay signature, retrieves trusted payment details from Razorpay, updates the payment lifecycle, records audit events, and completes the Pay-In process.

These APIs together implement the complete synchronous Pay-In workflow for the StreamHub Payment Service.