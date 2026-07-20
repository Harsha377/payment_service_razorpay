# Razorpay Integration

## 1. Overview

The StreamHub Payment Service integrates with **Razorpay** as the payment gateway for processing Pay-In transactions.

The integration is designed to isolate all gateway-specific logic from the business layer, allowing future support for additional payment providers without changing business logic.

The current implementation supports both **Pay-In** and **Refund** operations through Razorpay.

Implemented capabilities include:

### Pay-In

- Order Creation
- Payment Verification
- Payment Detail Retrieval
- Payment Method Mapping
- Signature Verification
- Webhook Processing

### Refunds

- Full Refund
- Partial Refund
- Multiple Partial Refunds
- Refund Detail Retrieval
- Refund Speed (Normal / Optimum)
- Refund Webhook Processing

The service currently operates in **Test Mode**.

---

# 2. Integration Architecture

The payment service never communicates with Razorpay directly from the business layer.

Instead, all gateway operations pass through the Gateway Layer.

```text
                     Payment Service
                           │
         ┌─────────────────┴──────────────────┐
         ▼                                    ▼
  Business Services                    Gateway Layer
         │                                    │
         ▼                                    ▼
Repositories                     RazorpayServiceImpl
         │                                    │
         └─────────────────┬──────────────────┘
                           ▼
                     Razorpay APIs
                           │
          ┌────────────────┴────────────────┐
          ▼                                 ▼
    REST APIs                      Webhook Events
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

- Amount (Paise)
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
- Gateway Status

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

After a successful payment, Razorpay Checkout returns:

- Gateway Order ID
- Gateway Payment ID
- Gateway Signature

The frontend typically invokes:

POST /payments/verify

However, in production the payment may also be completed through Razorpay webhooks before the frontend verification request arrives.

Both flows are supported and are designed to be idempotent.

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

Verification is performed locally using the Razorpay SDK and the merchant's Secret Key.

No external API call is required. 

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

The backend ignores any payment metadata supplied by the frontend and always retrieves trusted information directly from Razorpay.

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

The complete response is stored without modification to preserve gateway-specific information for future troubleshooting.

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

Refund Integration
The Gateway Layer also manages refund operations through Razorpay.

Supported operations include:

- Create Refund
- Fetch Refund Details

The Refund flow follows the same gateway abstraction used for payments.

```text
RefundService

↓

Gateway Interface

↓

RazorpayService

↓

Razorpay Refund API
```
Supported refund capabilities

* Full Refund
* Partial Refund
* Multiple Partial Refunds
* Refund Speed
* Refund Notes
* Gateway Response Storage
* Webhook Synchronization

### Gateway Idempotency
## Idempotency

The payment service is designed to safely handle duplicate gateway interactions.

Implemented idempotent flows include:

- Payment Verification
- payment.captured webhook
- refund.processed webhook
- refund.failed webhook

Duplicate requests or webhook deliveries do not create duplicate business records or invalid state transitions.

# 16. Gateway Status

Two status fields exist.

Internal Status

```text
CREATED

↓

SUCCESS

↓

(Refund lifecycle managed separately)
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

Payment Intent API

↓

Create Razorpay Order

↓

Frontend Checkout

↓

Customer Pays

↓

───────────────┐
               │
               ▼
     Verify Payment API
               │
               ▼
        Verify Signature
               │
               ▼
     Fetch Payment Details
               │
               ▼
       Update Database

               OR

───────────────┐
               │
               ▼
 Razorpay Webhook

↓

Verify Webhook Signature

↓

Update Database

↓

Record Events
```

---

# 18. Current Razorpay Features

Implemented

✓ Order Creation

✓ Signature Verification

✓ Fetch Payment Details

✓ Public Key Handling

✓ Gateway Response Storage

✓ Payment Method Mapping

✓ Gateway Status Mapping

✓ Payment Webhooks

✓ Refund Creation

✓ Fetch Refund Details

✓ Refund Webhooks

✓ Refund Speed Support

✓ Gateway Notes

✓ JSONB Payload Storage

Verified

- Razorpay Test Order Creation
- Razorpay Checkout
- Successful Payment
- Signature Verification
- Database Updates
- Gateway Response Storage

---
## Webhook Integration

The StreamHub Payment Service uses Razorpay Webhooks to receive asynchronous payment notifications directly from Razorpay.

The webhook implementation includes:

- Webhook endpoint
- Signature verification using HMAC SHA256
- Event routing
- Payment lifecycle processing
- Production-ready webhook architecture

Supported Events

- payment.captured
- payment.failed
- payment.authorized
- order.paid
- refund.processed
- refund.failed

Webhook processing is fully implemented and supports idempotent event processing.

A detailed explanation of the webhook lifecycle, supported events, request validation, signature verification, and event routing is available in:

**10-webhook-integration.md**

### 19. Current Limitations

The current implementation does not yet support

- Payout APIs
- Settlement APIs
- Subscription APIs
- Smart Collect
- Route
- Payment Links

These will be implemented in future phases.

---

# 20. Future Enhancements

Planned Razorpay integrations

- Payout Processing
- Settlement Reconciliation
- Smart Collect
- Route
- Subscription Billing
- Multiple Gateway Support
- Currency Conversion
- Monitoring & Metrics
- Distributed Tracing
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