# Payout Module

## 1. Overview

The Payout module is responsible for transferring money from the StreamHub platform to a beneficiary's registered Bank Account or UPI account using RazorpayX.

A payout represents an outgoing transaction from the platform.

Unlike the Pay-In module, which collects money from customers, the Payout module sends money from the platform to nutritionists after validating the beneficiary, selected fund account, and payout request.

---

# 2. Business Requirement

StreamHub is a marketplace where customers book appointments with nutritionists.

Example:

Customer books an appointment.

Appointment Amount : ₹1200

Platform Commission : ₹200

Nutritionist Earnings : ₹1000

The ₹1000 does not immediately reach the nutritionist.

Instead,

Customer
│
▼
Pay-In
│
▼
Platform Account
│
▼
Wallet Balance
│
▼
Vendor  clicks Withdraw
│
▼
Payout
│
▼
Bank / UPI

The payout module is responsible for transferring the nutritionist's earnings from the platform account to the beneficiary's registered Bank Account or UPI.

---

# 3. Why Payout Module?

The payout module provides:

- Secure money transfer
- Beneficiary validation
- Fund Account validation
- Idempotent payout creation
- Complete audit trail
- Gateway abstraction
- Event tracking
- Future webhook synchronization

---

# 4. Pay-In vs Payout

| Pay-In                   | Payout                 |
|--------------------------|------------------------|
| Customer → Platform      | Platform → Beneficiary |
| Incoming Money           | Outgoing Money         |
| Razorpay Payment Gateway | RazorpayX              |
| Creates Payment          | Creates Payout         |
| Money Collection         | Money Transfer         |

---

# 5. Technologies Used

Java 21

Spring Boot

Spring Data JPA

PostgreSQL

Flyway

Jackson

RestClient

RazorpayX REST APIs

ULID

Lombok

---

# 6. Why RestClient?

We are using Spring Boot RestClient.

Reason:

• Lightweight

• Production ready

• Spring Boot recommended

• Easy request/response mapping

• Easy authentication

• Better than RestTemplate

We are NOT using Razorpay SDK.

Instead,

StreamHub

↓

RestClient

↓

RazorpayX REST API

↓

JSON Response

↓

ObjectMapper

↓

DTO

This gives us full control over request and response mapping.

---

# 7. Architecture

Client

↓

Controller

↓

Service

↓

Mapper

↓

Gateway Mapper

↓

Gateway Client

↓

RestClient

↓

RazorpayX REST API

↓

Database

---

# 8. Database Tables

## beneficiaries

Stores beneficiary information.

Important columns

beneficiary_reference

owner_reference

gateway_contact_id

gateway_response

status

metadata

---

## fund_accounts

Stores Bank Account / UPI.

Important columns

fund_account_reference

gateway_fund_account_id

is_default

status

gateway_response

---

## payouts

Stores payout transaction.

Important columns

payout_reference

idempotency_key

beneficiary_id

fund_account_id

gateway_payout_id

amount

currency

mode

purpose

status

failure_reason

gateway_response

metadata

---

## payout_events

Immutable audit table.

Stores every payout state transition.

Columns

payout_event_reference

payout_id

event_type

event_source

event_data

---

# 9. Entity Relationship

Beneficiary

1

↓

*

Fund Account

1

↓

*

Payout

1

↓

*

Payout Event

---

# 10. Complete Flow

POST /api/v1/payouts

↓

Validate Request

↓

Validate Idempotency Key

↓

Find Beneficiary

↓

Beneficiary ACTIVE?

↓

Find Default ACTIVE Fund Account

↓

Create Payout Entity

↓

Save CREATED

↓

Create Gateway Request

↓

Gateway Mapper

↓

RestClient

↓

RazorpayX

↓

Gateway Response

↓

Update Payout

↓

Store Raw Gateway Response

↓

Create Payout Event

↓

Return Response

---

# 11. API

POST /api/v1/payouts

Headers

Idempotency-Key

Content-Type

application/json

Request

{
"beneficiaryReference":"01KXXXXXX",
"amount":100000,
"currency":"INR",
"mode":"IMPS",
"purpose":"PAYOUT",
"metadata":{
"requestedBy":"Nutritionist Portal"
}
}

---

Response

{
"payoutReference":"01KXXXXXX",
"beneficiaryReference":"01KXXXXXX",
"fundAccountReference":"01KXXXXXX",
"amount":100000,
"currency":"INR",
"status":"PENDING"
}

---

# 12. Service Flow

createPayout()

↓

validateRequest()

↓

validateIdempotency()

↓

findBeneficiary()

↓

findDefaultFundAccount()

↓

createPayoutEntity()

↓

savePayout()

↓

createGatewayPayout()

↓

updatePayout()

↓

createPayoutEvent()

↓

Return Response

---

# 13. Gateway Flow

CreatePayoutRequest

↓

GatewayPayoutMapper

↓

GatewayPayoutRequest

↓

buildPayoutRequest()

↓

RestClient

↓

POST /payouts

↓

RazorpayX

↓

GatewayPayoutResponse

↓

GatewayPayoutResult

↓

Update Database

---

# 14. Why Gateway DTOs?

Business DTOs should never depend on Razorpay.

Business Layer

↓

Gateway Mapper

↓

Gateway DTO

↓

Razorpay

This keeps business logic independent from gateway implementation.

---

# 15. Idempotency

Every payout request contains an Idempotency-Key.

Purpose

Prevent duplicate payouts.

Flow

Frontend

↓

UUID / ULID

↓

Idempotency-Key Header

↓

Backend

↓

Database

↓

Already Exists?

↓

Yes

↓

409 Conflict

No duplicate payout is created.

---

# 16. Validation Rules

Beneficiary must exist.

Beneficiary must be ACTIVE.

Default Fund Account must exist.

Fund Account must be ACTIVE.

Currency must be INR.

Amount must be greater than zero.

Idempotency Key must be unique.

---

# 17. Logging

Important logs

Request received

Beneficiary found

Fund account found

Gateway request

Gateway response

Database updated

Event created

Payout completed

---

# 18. Error Handling

400

Bad Request

Invalid Amount

Invalid Currency

--------------------

404

Beneficiary Not Found

--------------------

409

Duplicate Idempotency Key

No Default Fund Account

Inactive Beneficiary

--------------------

502

Gateway Failure

---

# 19. Why Store Raw Gateway Response?

Every Razorpay response is stored inside gateway_response.

Advantages

Complete audit trail

Easy debugging

Future reconciliation

Support investigations

Webhook comparison

Instead of storing only gateway IDs, the complete JSON response is stored.

---

# 20. Event Tracking

Every payout state change creates a new record.

CREATED

↓

PENDING

↓

PROCESSING

↓

PROCESSED

or

FAILED

Each transition inserts a new row into payout_events.

No event is ever updated.

This provides a complete immutable audit history.

---

# 21. Folder Structure

payout

├── controller

├── dto

│ ├── request

│ └── response

├── entity

├── enums

├── mapper

├── repository

├── service

└── gateway

---

# 22. Future Enhancements

Wallet Balance Validation

Webhook Synchronization

Retry Failed Payouts

Scheduled Reconciliation

List Payouts

Get Payout Details

Cancel Payout

Webhook Signature Verification

Dashboard Reports

Bulk Payout Support

Multiple Payment Gateways
