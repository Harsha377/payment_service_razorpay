# API Reference

## 1. Overview

This document describes the REST APIs currently implemented by the StreamHub Payment Service.

Current APIs

### Payment Intent

- Create Payment Intent

### Payments

- Verify Payment
- Get Payment
- Get Payment Events

### Refunds

- Create Refund
- Get Refund
- Get Refunds by Payment

### Webhooks

- Razorpay Webhook Endpoint


Future APIs

- Payout APIs
- Settlement APIs
- Reporting APIs

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

Webhook Endpoint

The Razorpay webhook endpoint does not require application authentication.
Instead, requests are authenticated using the `X-Razorpay-Signature` header and the configured webhook secret.

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
  "orderReference":"01K...",
  "customerReference":"01K...",
  "amount":10000,
  "currency":"INR",
  "description":"Premium Subscription",
  "metadata":{
    "plan":"PREMIUM"
  }
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
| metadata | JSON | No | Business-specific metadata |

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

Possible Errors

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

### Already Verified

Verification is idempotent.

If the payment has already been verified—either through a previous verification request or through webhook processing—the API returns a successful response.

HTTP

200 OK

Example

```json
{
    "paymentReference":"01K...",
    "status":"SUCCESS",
    "message":"Payment already verified."
}
```

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

Already SUCCESS?

↓

Yes

↓

Return Success

↓

No

↓

Verify Signature

↓

Fetch Payment Details

↓

Update Payment

↓

Update Payment Intent

↓

Record Event

↓

Return Success
```

## 7. Add Refund
```text
Create Refund
POST /refunds

Description

Creates a refund against a successful payment.

Supports:

Full Refund
Partial Refund
Multiple Partial Refunds
Request
{
"paymentReference":"01K...",
"amount":50000,
"reason":"CUSTOMER_REQUEST",
"speed":"NORMAL",
"initiator":"MERCHANT",
"metadata":{
"ticketId":"SUP-1001"
}
}
Success
201 Created

Example

{
"refundReference":"01K...",
"paymentReference":"01K...",
"status":"PROCESSING",
"message":"Refund created successfully."
}
Possible Errors

400

Refund amount exceeds remaining refundable amount.

404

Payment not found.

500

Gateway failure.
```

```text
8. Get Refund

If implemented.

GET /refunds/{refundReference}
```

```text
9. Get Refunds by Payment
   GET /payments/{paymentReference}/refunds
```
10. Webhook Endpoint

This section needs expansion.

Current Supported Events

* payment.authorized
* 
* payment.captured
* 
* payment.failed
* 
* order.paid
* 
* refund.processed
* 
* refund.failed

### Request Headers

### Headers

| Header | Required | Description |
|---------|----------|-------------|
| X-Razorpay-Signature | Yes | Used to verify webhook authenticity |

### Authentication

HMAC SHA256 Verification

### Idempotency

Duplicate webhook deliveries are safely ignored.
Standard HTTP Status

Add

| 401 | Unauthorized webhook signature (if applicable) |
| 422 | Business validation failure (future) |

Validation Rules

Add

Refund
Field	Validation
paymentReference	Required
amount	> 0
speed	Enum
reason	Enum
initiator	Enum
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

The payment service implements idempotent processing for critical operations.

Implemented

- Payment Verification
- payment.captured webhook
- refund.processed webhook
- refund.failed webhook

Duplicate requests do not create duplicate business records or invalid state transitions.

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

### Request Body

Content-Type

```
application/json
```

Example

```json
{
    "event":"payment.captured",
    "payload":{...}
}
```

---

### Success Response

```
HTTP 200 OK
```

Returned after successful webhook processing.

---

### Error Responses

| Status | Description |
|---------|-------------|
| 400 | Invalid webhook signature or malformed payload |
| 500 | Internal server error |

---


## Payout

```http
POST /payouts

GET /settlements

GET /reports
```

---

# API Summary

The current API provides production-ready support for the complete Pay-In and Refund lifecycle.

Implemented APIs include:

### Payment Intent

- Create Payment Intent

### Payments

- Verify Payment
- Get Payment
- Get Payment Events

### Refunds

- Create Refund
- Get Refund
- Get Refunds by Payment

### Webhooks

- Razorpay Webhook Processing

The APIs support:

- Payment initialization
- Payment verification
- Payment event recording
- Refund creation
- Refund tracking
- Webhook processing
- Idempotent payment and refund handling

Together these APIs implement the complete synchronous and asynchronous payment lifecycle of the StreamHub Payment Service.