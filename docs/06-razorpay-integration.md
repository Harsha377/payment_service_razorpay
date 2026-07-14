# Razorpay Integration

## 1. Overview

The StreamHub Payment Service integrates with **Razorpay** as the payment gateway for processing Pay-In transactions.

The integration is designed to isolate all gateway-specific logic from the business layer, allowing future support for additional payment providers without changing business logic.

The current implementation supports:

- Order Creation
- Payment Verification
- Payment Detail Retrieval
- Signature Verification

The service currently operates in **Test Mode**.

---

# 2. Integration Architecture

The payment service never communicates with Razorpay directly from the business layer.

Instead, all gateway operations pass through the Gateway Layer.

```text
                    Payment Service
                           │
                           ▼
                  RazorpayService
                           │
                           ▼
                 RazorpayServiceImpl
                           │
                           ▼
                     Razorpay SDK
                           │
                           ▼
                     Razorpay APIs
```

This architecture isolates all Razorpay-specific implementation.

---

# 3. Why Gateway Abstraction?

Instead of writing Razorpay code inside business services:

❌ Bad Design

```text
PaymentService

↓

Razorpay SDK
```

The project uses

✅ Good Design

```text
PaymentService

↓

Gateway Interface

↓

Razorpay Implementation
```

Advantages

- Loose coupling
- Easier testing
- Easier maintenance
- Supports multiple gateways
- Cleaner architecture

---

# 4. Configuration

Razorpay credentials are configured using Spring Boot configuration.

Current configuration

```yaml
razorpay:
  key-id: rzp_test_xxxxxxxxx
  key-secret: xxxxxxxxxxxxxxxxx
```

Configuration is loaded through

```text
RazorpayProperties
```

The service accesses the configuration through dependency injection.

```text
application.yml

↓

RazorpayProperties

↓

RazorpayServiceImpl
```

---

# 5. Security

Two credentials exist.

## Public Key (Key ID)

Example

```text
rzp_test_xxxxxxxxx
```

Purpose

- Identifies the merchant account.
- Used by Razorpay Checkout.
- Safe to expose to the frontend.

The backend returns the public key in the Payment Intent response.

---

## Secret Key

Example

```text
xxxxxxxxxxxxxxxxxxxxxxxx
```

Purpose

- Create Orders
- Verify Signatures
- Authenticate with Razorpay APIs

The secret key **never leaves the backend**.

---

# 6. Order Creation

Order creation begins after a Payment Intent is successfully created.

Flow

```text
PaymentIntentService

↓

GatewayOrderRequest

↓

RazorpayService

↓

Razorpay SDK

↓

Create Order API

↓

GatewayOrderResponse
```

The request contains

- Amount
- Currency
- Receipt

Example

```json
{
    "amount":10000,
    "currency":"INR",
    "receipt":"01KXXXXXXXXXXXXX"
}
```

---

# 7. Razorpay Response

Example

```json
{
    "id":"order_Q2ABC123",
    "entity":"order",
    "amount":10000,
    "currency":"INR",
    "status":"created"
}
```

The Payment Service stores

- Gateway Order ID
- Gateway Name

The frontend receives

- Gateway Order ID
- Gateway Name
- Gateway Key

---

# 8. Why Backend Returns Gateway Key?

The Razorpay Checkout SDK executes inside the browser.

The browser needs the public key.

Instead of storing the key in frontend configuration:

```text
Frontend

.env
```

the backend returns it.

Advantages

- Single source of truth.
- No duplicated configuration.
- Environment independent.
- Easier deployment.

The frontend simply uses

```text
gatewayKey
```

received from the backend.

---

# 9. Frontend Checkout Flow

```text
Frontend

↓

POST /payment-intents

↓

Receive

Gateway Key

Gateway Order ID

↓

Open Razorpay Checkout
```

The frontend never knows the secret key.

---

# 10. Payment Verification

After successful payment,

Razorpay Checkout returns

- Gateway Order ID
- Gateway Payment ID
- Gateway Signature

The frontend immediately calls

```http
POST /payments/verify
```

The backend performs signature verification.

Flow

```text
Frontend

↓

Verify API

↓

RazorpayService

↓

Verify Signature

↓

Success
```

---

# 11. Signature Verification

The signature verification uses

- Gateway Order ID
- Gateway Payment ID
- Gateway Signature
- Secret Key

The Razorpay SDK performs the verification.

If verification fails

```text
400 Bad Request

Invalid Payment Signature
```

is returned.

---

# 12. Why Verify Signature?

Without verification

```text
Client

↓

Fake Payment IDs

↓

Backend Accepts

❌
```

With verification

```text
Client

↓

Backend

↓

Verify Signature

↓

Razorpay

↓

Valid

↓

Update Database
```

Only genuine payments are accepted.

---

# 13. Fetch Payment Details

After successful signature verification,

the backend fetches payment details directly from Razorpay.

Flow

```text
PaymentService

↓

RazorpayService

↓

Fetch Payment

↓

Razorpay API
```

The backend retrieves

- Payment Method
- Gateway Status
- Complete Payment Response

---

# 14. Payment Method Mapping

Razorpay returns payment methods as strings.

Example

```text
upi

card

wallet

netbanking
```

Instead of directly using

```java
Enum.valueOf(...)
```

the project uses a dedicated mapper.

```text
Razorpay Method

↓

PaymentMethod Mapper

↓

Internal PaymentMethod Enum
```

Advantages

- Safer
- Easier maintenance
- Handles unknown values
- Gateway-independent mapping

---

# 15. Gateway Response Storage

The complete Razorpay response is stored.

```text
payments.gateway_response
```

using PostgreSQL JSONB.

Example

```json
{
    "id":"pay_Q2XYZ456",
    "status":"captured",
    "method":"upi",
    "bank":"HDFC",
    "wallet":null,
    "email":"customer@example.com"
}
```

Advantages

- Complete audit trail
- Easier debugging
- Future compatibility

---

# 16. Gateway Status

Two status fields exist.

Internal Status

```text
CREATED

↓

SUCCESS
```

Gateway Status

```text
created

authorized

captured

failed
```

These are intentionally stored separately.

---

# 17. Current Integration Flow

```text
Client

↓

Create Payment Intent

↓

Payment Service

↓

Create Razorpay Order

↓

Return Gateway Information

↓

Frontend

↓

Open Razorpay Checkout

↓

Customer Pays

↓

Verify Payment API

↓

Verify Signature

↓

Fetch Payment Details

↓

Update Database
```

---

# 18. Current Razorpay Features

Implemented

- Order Creation
- Signature Verification
- Fetch Payment Details
- Public Key Handling
- Gateway Response Storage
- Payment Method Mapping
- Gateway Status Mapping

Verified

- Razorpay Test Order Creation
- Razorpay Checkout
- Successful Payment
- Signature Verification
- Database Updates
- Gateway Response Storage

---

# 19. Current Limitations

The current implementation does not yet support

- Webhooks
- Refund APIs
- Payout APIs
- Subscription APIs
- Settlement APIs

These will be implemented in future phases.

---

# 20. Future Enhancements

Planned Razorpay integrations

- Webhook Processing
- Refund Management
- Payout Processing
- Settlement Reconciliation
- Subscription Billing
- Retry Handling

The current gateway abstraction allows these features to be added with minimal changes to the business layer.

---

# Integration Summary

The StreamHub Payment Service integrates with Razorpay through a dedicated Gateway Layer, ensuring that business logic remains independent of gateway-specific implementation.

The integration follows production-oriented design principles by:

- Keeping the Secret Key exclusively on the backend.
- Returning only the public Gateway Key to the frontend.
- Verifying every payment signature.
- Fetching trusted payment details directly from Razorpay.
- Persisting gateway metadata for auditing and debugging.
- Using gateway abstraction to support future payment providers.

This design provides a secure, maintainable, and extensible foundation for payment processing.