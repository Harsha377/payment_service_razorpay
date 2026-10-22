# StreamHub Payment Service — Pay-In & Payout Deep-Dive
### "Customer clicks Pay… what happens in my code?" — step by step, class by class

> Companion to `StreamHub-Payment-Service-Documentation.md`. That file explains the *whole* project; this one zooms into the two most important journeys and explains **exactly which class/method runs, what is written to which table, and which Razorpay API is called.**

**Contents**

- Part 0 — Two ways your code talks to Razorpay (SDK vs RestClient)
- Part 1 — PAY-IN: customer clicks "Pay" (complete timeline)
  - 1.1 Actors · 1.2 Timeline overview · 1.3–1.8 Each phase in detail · 1.9 What if something goes wrong · 1.10 Cheat sheet
- Part 2 — PAYOUT: sending money to a provider's bank (complete timeline, uses RestClient)
  - 2.1 Overview · 2.2 Setup (Contact + Fund Account) · 2.3 Wallet hold + payout · 2.4 Webhook confirmation · 2.5 Failures · 2.6 Cheat sheet
- Part 3 — Side-by-side comparison, notes to remember, self-test questions

---

# Part 0 — Two ways your code talks to Razorpay

Razorpay has **two different products**, and your code uses a different technique for each:

|   | **Razorpay Payments (pay-in)** | **RazorpayX (payouts)** |
|---|--------------------------------|-------------------------|
| Purpose | **Collect** money from customers | **Send** money to bank accounts / UPI |
| How you call it | **Razorpay Java SDK** (`razorpay-java 1.4.8`) → `RazorpayClient` bean | **Spring `RestClient`** (manual HTTP calls) → `razorpayXRestClient` bean |
| Why | The SDK has built-in methods for orders, payments, refunds and signature checks | The Java SDK does **not** cover RazorpayX (contacts, fund accounts, payouts), so you call the REST endpoints yourself |
| Code | `RazorpayServiceImpl` (`createOrder`, `verifyPayment`, `fetchPayment`, `createRefund`, `fetchRefund`, `verifyWebhookSignature`) | `RazorpayXClientImpl` (`createContact`, `createFundAccount`, `createPayout`, `fetchPayout`) — called *through* `RazorpayServiceImpl` |
| Auth | SDK handles it: `new RazorpayClient(keyId, keySecret)` | You build it: header `Authorization: Basic base64(keyId:keySecret)` |
| Config class | `RazorpayClientConfig.razorpayClient()` | `RazorpayClientConfig.razorpayXRestClient()` (base URL `https://api.razorpay.com/v1`, default headers `Authorization` + `Content-Type: application/json`) |

**How the layers connect**

```
Business services (PaymentIntentServiceImpl, PaymentServiceImpl, RefundServiceImpl,
                   FundAccountServiceImpl, PayoutServiceImpl, WebhookServiceImpl)
             │   they ONLY know the interface
             ▼
       RazorpayService  (interface)
             │
             ▼
       RazorpayServiceImpl
        ├── Razorpay SDK (razorpayClient.orders / payments / refunds + Utils.verify…)   ← PAY-IN & REFUND
        └── RazorpayXClient (interface) → RazorpayXClientImpl → RestClient              ← CONTACT, FUND ACCOUNT, PAYOUT
```

Configuration values come from `application-dev.yaml` → `RazorpayProperties` (`keyId`, `keySecret`, `webhookSecret`, `baseUrl`). The RazorpayX source bank account number comes from `razorpayx.source-account-number` (used by `GatewayPayoutMapper`).

---

# Part 1 — PAY-IN: "Customer clicks Pay"

## 1.1 Who is involved

| Actor | Role |
|---|---|
| **Customer** | The person paying |
| **Frontend** (web/mobile app — not in this repo) | Shows the Pay button, opens Razorpay Checkout |
| **Order/Appointment service** (another microservice, not in this repo) | Owns the order; calls the Payment Service |
| **Payment Service** (this project, port 8083) | Creates the order at Razorpay, verifies the payment, records everything |
| **Razorpay** | Hosts the checkout, collects the money, sends webhooks |
| **PostgreSQL** | Stores intents, payments, events, settlements, wallets |

## 1.2 Timeline overview

```
 PHASE A   Customer clicks "Pay"
           Order service → POST /api/v1/payment-intents
           → PaymentIntent + Razorpay Order + Payment(CREATED) + event + idempotency record
                                │
 PHASE B   Frontend opens Razorpay Checkout using gatewayOrderId + gatewayKey
           Customer enters UPI/card/netbanking → Razorpay collects money (auto-capture)
                                │
        ┌───────────────────────┴───────────────────────┐
 PHASE C│ Frontend → POST /api/v1/payments/verify        │ PHASE D  Razorpay → POST /api/v1/webhooks/razorpay
        │ (signature check)  — fast path                 │ (payment.captured) — safety-net path
        └───────────────────────┬───────────────────────┘
                                ▼
 PHASE E   completePayment(): Payment=SUCCESS, Intent=COMPLETED, event PAYMENT_SUCCESS
                                │
 PHASE F   If MARKETPLACE → createSettlement(): provider wallet credited (80%), commission recorded (20%)
```

Important idea: **Phase C and Phase D are two independent routes to the same Phase E.** Whichever arrives first completes the payment; the second one sees `status == SUCCESS` and does nothing.

---

## 1.3 PHASE A — Customer clicks "Pay" → create the Payment Intent

### What the caller sends

```http
POST http://localhost:8083/api/v1/payment-intents
Idempotency-Key: 7c1f6e0a-5f1d-4c63-9a52-0b0e9f1a2c11     ← unique per click/attempt
Content-Type: application/json

{
  "orderReference":    "ORD-2026-0001",      // order in the other service (≤ 26 chars)
  "customerReference": "CUST-1001",          // who is paying (≤ 26 chars)
  "amount":            100000,               // PAISE → ₹1000.00
  "currency":          "INR",
  "description":       "Consultation with Dr. Rao",
  "paymentFlowType":   "MARKETPLACE",        // or "DIRECT"
  "settlementBeneficiaryReference": "PROV-77" // provider id — REQUIRED for MARKETPLACE, FORBIDDEN for DIRECT
}
```

### Code path (in call order)

| # | Class → method | What it does |
|---|---|---|
| 1 | `PaymentIntentController.createPaymentIntent` | Reads the `Idempotency-Key` header, triggers Bean Validation (`@Valid`) on the body: `@NotBlank` references, `@Size(max=26)`, `@Positive` amount, currency regex `^[A-Z]{3}$`, `@NotNull` flow type. Bad input → `GlobalExceptionHandler` → 400. |
| 2 | `PaymentIntentServiceImpl.createPaymentIntent` (**@Transactional** — everything below is one DB transaction) | Orchestrates the rest. |
| 3 | `validateRequest` | Currency must be `INR` (else 400). `paymentIntentRepository.findByOrderReference` — if an intent already exists for this order → **409**. |
| 4 | `validatePaymentFlow` | `MARKETPLACE` ⇒ `settlementBeneficiaryReference` must be present; `DIRECT` ⇒ it must be absent. Else 400. |
| 5 | `HashGenerator.generateSha256(request)` | Converts the request to JSON and makes a SHA-256 fingerprint (`requestHash`). |
| 6 | `IdempotencyService.findByIdempotencyKey` | If the key was used before: not expired + same hash → return the original response; same key but different hash → 409; expired → delete it. (See the note in Part 1.9 about a bug in this step.) |
| 7 | `createPaymentIntentEntity` + `paymentIntentRepository.save` | Inserts `payment_intents` row: new ULID `paymentIntentReference`, `status = CREATED`, `expiresAt = now + 15 min`, flow type, provider ref. |
| 8 | `RazorpayService.createOrder(GatewayOrderRequest)` → `RazorpayServiceImpl.createOrder` | **Razorpay SDK call** `razorpayClient.orders.create({amount, currency, receipt = paymentIntentReference})`. Razorpay returns an `order_…` id. Failure → `ApiException` 500 "Failed to create Razorpay order." |
| 9 | `PaymentServiceImpl.createPayment` | Inserts `payments` row: new ULID `paymentReference`, `gatewayName=RAZORPAY`, `gatewayOrderId=order_…`, amount/currency copied from the intent, `status = CREATED`. |
| 10 | `PaymentEventServiceImpl.recordPaymentCreated` | Inserts `payment_events` row: `PAYMENT_CREATED`, source `SYSTEM`. |
| 11 | `IdempotencyService.save` | Inserts `idempotency_keys`: key, hash, `PAYMENT_INTENT`, the intent reference, HTTP 201, expires in 24 h. |
| 12 | `PaymentMapper.toCreatePaymentIntentResponse` | Builds the response, including Razorpay's **public key id** (`gatewayKey`). |
| — | Transaction commits | All rows saved together. If any step throws, **everything** (except the already-created Razorpay order, which is harmless and unpaid) is rolled back. |

### Response

```json
HTTP 201 Created
{
  "paymentIntentReference": "01JB8K3...",
  "paymentReference":       "01JB8K3...",
  "gatewayOrderId":         "order_Q1xAbC123",
  "amount": 100000,
  "currency": "INR",
  "status": "CREATED",
  "gatewayKey": "rzp_test_xxxxxxxx",      // public key id (safe for the browser)
  "gatewayName": "RAZORPAY"
}
```

### Database after Phase A

| Table | New row (key columns) |
|---|---|
| `payment_intents` | `status=CREATED`, `amount=100000`, `payment_flow_type=MARKETPLACE`, `settlement_beneficiary_reference=PROV-77`, `expires_at=now+15m` |
| `payments` | `status=CREATED`, `gateway_order_id=order_Q1xAbC123`, `gateway_payment_id=NULL`, `payment_method=NULL`, `paid_at=NULL` |
| `payment_events` | `PAYMENT_CREATED / SYSTEM` |
| `idempotency_keys` | key → `PAYMENT_INTENT` + intent reference, 24 h |

> **Nothing about money has moved yet.** At this point we only have a *promise to collect* and an *order at Razorpay*.

---

## 1.4 PHASE B — Razorpay Checkout (happens outside your code)

The frontend uses the response to open Razorpay's hosted payment popup. (This code lives in your frontend, shown here only so you remember the contract.)

```javascript
const options = {
  key:      response.gatewayKey,        // from Phase A
  order_id: response.gatewayOrderId,    // from Phase A
  amount:   response.amount,
  currency: response.currency,
  handler: function (rzp) {
    // Razorpay calls this after SUCCESSFUL payment
    // rzp = { razorpay_payment_id, razorpay_order_id, razorpay_signature }
    fetch('/api/v1/payments/verify', { method:'POST', body: JSON.stringify({
        paymentReference:  response.paymentReference,
        gatewayOrderId:    rzp.razorpay_order_id,
        gatewayPaymentId:  rzp.razorpay_payment_id,
        gatewaySignature:  rzp.razorpay_signature
    })});
  }
};
new Razorpay(options).open();
```

What Razorpay does: shows UPI / card / netbanking, the customer authenticates (OTP/UPI PIN), the bank debits the customer, and — because your account uses **auto-capture** — Razorpay immediately *captures* the money. Razorpay then:

1. Returns `razorpay_payment_id` (`pay_…`), `razorpay_order_id`, and `razorpay_signature` to the browser, **and**
2. Sends a **`payment.captured` webhook** to your server (Phase D).

Your Payment Service is **not involved** during checkout.

---

## 1.5 PHASE C — Verify API (fast path, called by frontend)

```http
POST /api/v1/payments/verify
{
  "paymentReference": "01JB8K3...",        // @NotBlank in the DTO (but the service doesn't use it)
  "gatewayOrderId":   "order_Q1xAbC123",
  "gatewayPaymentId": "pay_Q1xDeF456",
  "gatewaySignature": "9ef4dffbfd84f1318f6739a3ce19f9d85851857ae648f114332d8401e0949a3d"
}
```

| # | Class → method | What it does |
|---|---|---|
| 1 | `PaymentController.verifyPayment` | `@Valid` checks the 4 fields are non-blank. |
| 2 | `PaymentServiceImpl.verifyPayment` (@Transactional) | Starts. |
| 3 | `paymentRepository.findByGatewayOrderId` | Finds our payment from the Razorpay order id. Not found → 404. |
| 4 | Duplicate check | If `status == SUCCESS` → returns "Payment already verified." (safe to call again). If `status != CREATED` (e.g. FAILED) → 400 "cannot be verified in the current state". |
| 5 | `RazorpayServiceImpl.verifyPayment` | **Signature check** using the SDK: `Utils.verifyPaymentSignature({order_id, payment_id, signature}, keySecret)`. Internally Razorpay's rule is `HMAC_SHA256(order_id + "|" + payment_id, keySecret) == signature`. A wrong signature → 400 "Invalid payment signature." *This proves the browser data really came from Razorpay and was not forged.* |
| 6 | `completePayment(payment, gatewayPaymentId, signature)` | Phase E (below). |
| 7 | Returns `VerifyPaymentResponse` | `{paymentReference, paymentIntentReference, gatewayPaymentId, status: SUCCESS, message: "Payment verified successfully."}` |

---

## 1.6 PHASE D — Webhook `payment.captured` (safety-net path, called by Razorpay)

```http
POST /api/v1/webhooks/razorpay
X-Razorpay-Signature: <hmac of body with webhook secret>

{ "event": "payment.captured",
  "payload": { "payment": { "entity": { "id": "pay_Q1xDeF456", "order_id": "order_Q1xAbC123",
                                         "status": "captured", "method": "upi", ... } } } }
```

| # | Class → method | What it does |
|---|---|---|
| 1 | `RazorpayWebhookController.receiveWebhook` | Takes the **raw** body string + signature header. (Raw string matters: the signature is computed over the exact bytes.) |
| 2 | `WebhookServiceImpl.processWebhook` | Calls `razorpayService.verifyWebhookSignature` → `Utils.verifyWebhookSignature(payload, signature, webhookSecret)`. Invalid → 400. |
| 3 | `routeEvent("payment.captured")` → `handlePaymentCaptured` | Reads `payload.payment.entity.id` and `.order_id`; blank → 400. |
| 4 | `paymentRepository.findByGatewayOrderId` | Finds the payment (404 if unknown). |
| 5 | Duplicate check | `status == SUCCESS` → ignore and return (this is what happens when Phase C already ran). |
| 6 | `paymentService.completePayment(payment, gatewayPaymentId, null)` | Same as Phase E; signature is `null` because webhooks have none to store. |
| 7 | Return `200 OK` | Razorpay stops retrying. |

Other payment-related webhooks: `payment.failed` (→ `failPayment`), `payment.authorized` & `order.paid` (logged and ignored).

---

## 1.7 PHASE E — `PaymentServiceImpl.completePayment` (the heart of pay-in)

```java
GatewayPaymentDetailsResponse d = razorpayService.fetchPayment(gatewayPaymentId);   // 1
payment.setGatewayPaymentId(...); setGatewaySignature(...);                         // 2
payment.setGatewayStatus(d.getGatewayStatus());      // "captured"
payment.setGatewayResponse(d.getGatewayResponse());  // full JSON
payment.setPaymentMethod(d.getPaymentMethod());      // UPI / CARD / ...
payment.setStatus(PaymentStatus.SUCCESS);  payment.setPaidAt(now);
paymentRepository.save(payment);
intent.setStatus(PaymentIntentStatus.COMPLETED);  paymentIntentRepository.save(intent);   // 3
paymentEventService.recordPaymentSuccess(payment);                                  // 4
if (intent.getPaymentFlowType() == MARKETPLACE) settlementService.createSettlement(payment, intent.getSettlementBeneficiaryReference());   // 5
```

1. **Fetch the truth from Razorpay** (SDK `razorpayClient.payments.fetch(pay_id)`) — the code never trusts the browser for status/method. `RazorpayPaymentMethodMapper` converts Razorpay's `"upi" / "card" / "netbanking" / "wallet" / "emi"` to your `PaymentMethod` enum (else `UNKNOWN`).
2. Update the **payment**.
3. Update the **intent** → `COMPLETED`.
4. Insert **`payment_events`**: `PAYMENT_SUCCESS / SYSTEM`.
5. If marketplace → Phase F, **in the same transaction**.

### Database after Phase E

| Table | Change |
|---|---|
| `payments` | `status SUCCESS`, `gateway_payment_id=pay_…`, `gateway_status=captured`, `payment_method=UPI`, `gateway_response={…}`, `paid_at=now` |
| `payment_intents` | `status COMPLETED` |
| `payment_events` | + `PAYMENT_SUCCESS / SYSTEM` |

For a **DIRECT** payment the journey ends here — the money is simply the platform's.

---

## 1.8 PHASE F — Settlement (MARKETPLACE only): provider gets paid into the wallet

`SettlementServiceImpl.createSettlement(payment, ownerReference)`

| # | Step | Detail |
|---|---|---|
| 1 | `validatePayment` | `payment.status` must be `SUCCESS` **and** `gatewayStatus` must equal `"captured"` (else 409). |
| 2 | `validateSettlementNotExists` | `settlements.payment_id` is unique → no double settlement (409). |
| 3 | `calculatePercentageSettlement` | `commission = gross × 20 / 100`, `net = gross − commission`. ₹1000 → commission ₹200, net ₹800. |
| 4 | `validateOwnerReference` | provider reference must not be blank. |
| 5 | `WalletService.getOrCreateWallet(providerRef, PROVIDER)` | Finds the provider's wallet, or **creates it** (balances all 0, `ACTIVE`, INR). No bank details needed yet. |
| 6 | Save `settlements` row | `status CREATED`, gross/commission/net, owner, wallet link. |
| 7 | `WalletService.creditWallet(wallet, net, SETTLEMENT, SETTLEMENT, settlementRef, "Settlement Credit", …)` | `availableBalance += 80000`, `totalCredited += 80000`; inserts `wallet_transactions` row `CREDIT` with `balance_before 0 → balance_after 80000`. |
| 8 | Insert `settlement_events` | `SETTLEMENT_COMPLETED / SYSTEM`. |
| 9 | `settlement.status = COMPLETED` | saved. |

### Database after Phase F

| Table | Change |
|---|---|
| `wallets` (owner `PROV-77`/`PROVIDER`) | `available_balance 80000`, `total_credited 80000` (row created if first time) |
| `wallet_transactions` | `CREDIT 80000, source SETTLEMENT, ref SETTLEMENT/<settlementRef>` |
| `settlements` | `gross 100000, commission 20000, net 80000, COMPLETED` |
| `settlement_events` | `SETTLEMENT_COMPLETED` |

---

## 1.9 What if something goes wrong? (pay-in)

| Situation | What your code does |
|---|---|
| Customer's payment **fails** (wrong OTP, insufficient funds) | Razorpay sends `payment.failed`. `handlePaymentFailed` fetches the payment, `failPayment` sets `FAILED`, stores JSON, writes `PAYMENT_FAILED / WEBHOOK`. Never overrides a `SUCCESS`. Intent stays `CREATED`. A FAILED payment can't be re-verified (needs `CREATED`), so a retry needs a **new** order — but note one intent per `orderReference` is allowed, so retries currently need a new order reference or a code change (see Notes). |
| Customer **closes browser** after paying | `/verify` is never called, but the `payment.captured` webhook completes it. ✔ |
| **Verify and webhook arrive together** | Both try `completePayment`; the second sees `SUCCESS` and returns. (A tiny race exists without row locking — the unique settlement constraint stops double credit.) |
| **Forged verify call** (fake signature) | `Utils.verifyPaymentSignature` throws → 400. Nothing changes. |
| **Fake webhook** | Webhook signature check fails → 400. |
| **Client retries Phase A** (double-click) | Meant to be handled by `Idempotency-Key`. **Known bug:** `validateRequest` (the "intent already exists for this order" 409) runs *before* the idempotency lookup, so the replay returns 409 instead of the original response. Fix: move the idempotency block above `validateRequest`. |
| `fetchPayment` returns status **`authorized`** (not yet captured) at verify time | `completePayment` marks SUCCESS, but `SettlementServiceImpl.validatePayment` throws 409 "Gateway payment has not been captured" → the **whole transaction rolls back** (payment stays `CREATED`) and the user sees an error although they paid. The later `payment.captured` webhook then completes it properly. With auto-capture this is rare but possible. |
| Razorpay is down during Phase A | `createOrder` throws → 500; DB transaction rolls back; no rows left behind. |
| Intent older than 15 minutes | `expires_at` is stored but **nothing enforces/expires it** yet. |

## 1.10 Pay-in cheat sheet (one page)

```
CLICK PAY
 └─ POST /payment-intents  (Idempotency-Key)
      PaymentIntentController → PaymentIntentServiceImpl.createPaymentIntent  [@Transactional]
        validate → hash → idempotency check
        INSERT payment_intents(CREATED)
        Razorpay SDK: orders.create  → order_xxx
        INSERT payments(CREATED, order_xxx) ; INSERT payment_events(PAYMENT_CREATED)
        INSERT idempotency_keys
      ← 201 {gatewayOrderId, gatewayKey, refs}

CHECKOUT  (frontend + Razorpay; customer pays; auto-capture)

CONFIRM (either, first wins)
 ├─ POST /payments/verify → PaymentServiceImpl.verifyPayment → signature HMAC check → completePayment
 └─ Webhook payment.captured → WebhookServiceImpl.handlePaymentCaptured → completePayment

completePayment [@Transactional]
   Razorpay SDK: payments.fetch(pay_xxx)
   payments → SUCCESS (+ method, json, paid_at) ; intent → COMPLETED ; event PAYMENT_SUCCESS
   if MARKETPLACE → SettlementServiceImpl.createSettlement
        wallet getOrCreate(provider) ; settlement(CREATED) ; wallet CREDIT net ; ledger row ; event ; settlement COMPLETED
```

---

# Part 2 — PAYOUT: sending money to a provider's bank (uses `RestClient`)

## 2.1 Overview

Payout = platform → provider's real bank account/UPI. It needs **four things in order**:

```
1. Provider has money in their WALLET            (created by settlement in Part 1)
2. Provider has a BENEFICIARY  (= Razorpay Contact)      ┐ onboarding, once
3. Provider has a FUND ACCOUNT (= Razorpay Fund Account) ┘ (RestClient calls #1 and #2)
4. Payout request → wallet HOLD → RazorpayX payout (RestClient call #3) → webhook confirms
```

All three RazorpayX calls use the **same `RestClient`** bean:

```java
RestClient.builder()
   .baseUrl("https://api.razorpay.com/v1")
   .defaultHeader(AUTHORIZATION, "Basic " + base64(keyId + ":" + keySecret))
   .defaultHeader(CONTENT_TYPE, "application/json")
   .build();
```

| RazorpayX API | HTTP | Your method | Returns |
|---|---|---|---|
| Create Contact | `POST /contacts` | `RazorpayXClientImpl.createContact` | `GatewayContactResult` (`cont_…`) |
| Create Fund Account | `POST /fund_accounts` | `RazorpayXClientImpl.createFundAccount` | `GatewayFundAccountResult` (`fa_…`) |
| Create Payout | `POST /payouts` | `RazorpayXClientImpl.createPayout` | `GatewayPayoutResult` (`pout_…`) |
| Fetch Payout | `GET /payouts/{id}` | `RazorpayXClientImpl.fetchPayout` | `GatewayPayoutDetailsResponse` |

### The RestClient pattern used in all four methods

```java
JsonNode gatewayResponse = razorpayXRestClient.post()
        .uri("/payouts")
        .body(buildPayoutRequest(request))          // a Map → JSON
        .retrieve()
        .onStatus(HttpStatusCode::isError, (req, res) -> {   // 4xx / 5xx from Razorpay
            String errorBody = new String(res.getBody().readAllBytes());
            throw buildGatewayException(errorBody, HttpStatus.BAD_GATEWAY);   // → 502 to your caller
        })
        .body(JsonNode.class);                       // raw JSON
GatewayPayoutResponse response = objectMapper.treeToValue(gatewayResponse, GatewayPayoutResponse.class);  // typed copy
return GatewayPayoutResult.builder().response(response).gatewayResponse(gatewayResponse).build();
```

*Why return both?* `response` is the **typed** object used for logic (`id`, `status`); `gatewayResponse` is the **raw JSON** saved into the `jsonb` columns for audit/debugging. `buildGatewayException` reads Razorpay's error JSON (`error.description`, `error.field`) so the real Razorpay message and field reach your API caller.

Exception mapping inside these methods: Razorpay returned an error → `ApiException` 502 (BAD_GATEWAY); network/`RestClientException` → 502; JSON parsing problem → 500.

---

## 2.2 STEP 1 — Onboarding: Beneficiary (Contact) + Fund Account

```http
POST /api/v1/fund-accounts
{
  "ownerReference": "PROV-77",          // the SAME id used as settlementBeneficiaryReference / wallet owner
  "ownerType": "VENDOR",                 // beneficiary OwnerType: USER|MERCHANT|VENDOR|EMPLOYEE|PARTNER
  "name": "Dr. Rao", "email": "rao@example.com", "contactNumber": "9876543210",
  "fundAccountType": "BANK_ACCOUNT",     // or "VPA"
  "accountHolderName": "Dr. Rao", "accountNumber": "1234567890", "ifscCode": "HDFC0001234"
}
```

`FundAccountServiceImpl.createFundAccount`:

| # | Step | Table / API |
|---|---|---|
| 1 | `validateRequest` — BANK needs holder+number+IFSC and no VPA; VPA needs `vpa` and no bank fields | – |
| 2 | Find beneficiary by `(ownerReference, ownerType)` | `beneficiaries` |
| 3a | **Exists:** name/email/contact must match (else 409); same fund-account type twice → 409 | – |
| 3b | **New:** insert beneficiary (`ACTIVE`, ULID `beneficiaryReference`) → **RestClient `POST /contacts`** `{name, email, contact, type, notes}` → save `gateway_contact_id=cont_…` + raw JSON | `beneficiaries` |
| 4 | Build fund account row (`ACTIVE`; **`isDefault = true` if beneficiary has no default yet**) | `fund_accounts` |
| 5 | **RestClient `POST /fund_accounts`** `{contact_id, account_type:"bank_account", bank_account:{name, ifsc, account_number}}` (or `vpa:{address}`) → save `gateway_fund_account_id=fa_…` + raw JSON | `fund_accounts` |
| 6 | Return beneficiary + fund account | – |

After this, Razorpay *knows* who and where; locally you have `beneficiaries` + `fund_accounts` linked by `beneficiary_id`.

**Link to the wallet:** provider wallet `owner_reference` (`PROV-77`) == beneficiary `owner_reference` (`PROV-77`). That value match is the only bridge — there is no foreign key.

---

## 2.3 STEP 2 — Payout request: wallet hold → RazorpayX payout

```http
POST /api/v1/payouts
Idempotency-Key: payout-2026-10-22-0001
{ "beneficiaryReference": "01JB9...",   // the generated ULID of the beneficiary (NOT "PROV-77")
  "amount": 50000, "currency": "INR", "mode": "IMPS", "purpose": "PAYOUT",
  "metadata": { "note": "weekly withdrawal" } }
```

`PayoutServiceImpl.createPayout` (**@Transactional**):

| # | Step | Detail |
|---|---|---|
| 1 | `validateRequest` | currency `INR`, amount > 0 |
| 2 | `validateIdempotency` | `payouts.idempotency_key` exists → 409 |
| 3 | `findBeneficiary` | must exist and be `ACTIVE` |
| 4 | `findDefaultFundAccount` | `isDefault = true` **and** `ACTIVE`, else 409 |
| 5 | `findWallet(beneficiary)` | `walletService.findWalletByOwner(beneficiary.getOwnerReference(), PROVIDER)` — 404 if provider never earned anything |
| 6 | **`WalletService.createWalletHold(wallet, amount)`** | checks wallet ACTIVE, amount>0, `available ≥ amount` (else 409 "Insufficient wallet balance"). Then `available −= amount`, `pending += amount`; inserts `wallet_holds` (`ACTIVE`, `payout_id = NULL`, `expires_at = +30 min`, balance before/after) |
| 7 | Build + save `PayoutEntity` | ULID reference, key, `status CREATED`, `gateway RAZORPAY`, mode, purpose |
| 8 | **`RazorpayService.createPayout` → `RazorpayXClientImpl.createPayout`** (RestClient `POST /payouts`) | body built by `GatewayPayoutMapper`: `account_number` (**your** RazorpayX account from yml), `fund_account_id` (`fa_…`), `amount`, `currency`, `mode`, `purpose` (lower-case), `queue_if_low_balance: true`, `reference_id = payoutReference`, `notes = metadata` |
| 9 | `updatePayout` | saves `gateway_payout_id=pout_…`, raw JSON, status via `mapGatewayStatus` (`queued→CREATED, pending, processing, processed, failed, cancelled, reversed`) |
| 10a | If immediate FAILED/CANCELLED/REVERSED | `cancelWalletHoldBeforePayout` (money back to available) → throw 409 "Gateway payout creation failed." |
| 10b | Else | `attachPayoutToWalletHold` (hold.payout = payout), `createPayoutWalletTransaction` (ledger `DEBIT`, source `PAYOUT`, "Wallet amount reserved for payout."), `createPayoutEvent` (`PAYOUT_xxx / API` with raw JSON) |
| 11 | `catch (Exception)` | if hold is still ACTIVE and unattached → `cancelWalletHoldBeforePayout`, rethrow — so money is never left stuck |
| 12 | Return `CreatePayoutResponse` (201) | |

### Database after STEP 2 (provider had 80000; withdrawing 50000)

| Table | State |
|---|---|
| `wallets` | `available 30000`, `pending 50000`, `total_credited 80000`, `total_debited 0` |
| `wallet_holds` | `amount 50000`, `ACTIVE`, `payout_id → payouts.id`, `balance_before 80000 → balance_after 30000` |
| `wallet_transactions` | + `DEBIT 50000 (PAYOUT)` `80000 → 30000` |
| `payouts` | `CREATED/PENDING/PROCESSING` (whatever Razorpay replied), `gateway_payout_id=pout_…` |
| `payout_events` | + one row (`source API`) |

The money is **reserved**, not yet gone.

---

## 2.4 STEP 3 — Webhook confirms the final result

The bank transfer takes seconds (IMPS/UPI) to hours/days (NEFT/RTGS). Razorpay reports progress via webhooks to the same `/api/v1/webhooks/razorpay` endpoint.

`WebhookServiceImpl` for each of `payout.processing | processed | failed | reversed`:

1. (signature already verified) read `payload.payout.entity.id` → `pout_…`
2. `payoutRepository.findByGatewayPayoutId` (404 if not found)
3. **Duplicate?** current status already equals target → ignore
4. **`validatePayoutStatusTransition`** — e.g. `PROCESSED`/`FAILED`/`REVERSED` are final (409 on any change)
5. `payoutService.updatePayoutFromWebhook(payout, newStatus, eventType, wholeWebhookJson, payoutNode)`:
   - set status; store full webhook JSON in `gateway_response`
   - copy `utr`, `fees`, `tax`, `failure_reason` if present
   - set `processed_at` / `failed_at` / `reversed_at`
   - insert `payout_events` (source `WEBHOOK`)
   - find the hold: `walletService.findWalletHoldByPayout(payout)` and then:

| New status | Wallet action | Result |
|---|---|---|
| `PROCESSING` | none | still reserved |
| **`PROCESSED`** | `releaseWalletHold` | `pending −= 50000`, `total_debited += 50000`, hold → `RELEASED` ✅ money truly left |
| **`FAILED` / `REVERSED` / `CANCELLED`** | `cancelWalletHold` | `pending −= 50000`, `available += 50000`, hold → `CANCELLED` ↩ money back |

### Final wallet numbers

| | available | pending | totalCredited | totalDebited |
|---|---|---|---|---|
| Before payout | 80000 | 0 | 80000 | 0 |
| After hold (STEP 2) | 30000 | 50000 | 80000 | 0 |
| **Payout processed** | 30000 | 0 | 80000 | **50000** |
| **Payout failed** | **80000** | 0 | 80000 | 0 |

---

## 2.5 Failure cases (payout)

| Situation | Behaviour |
|---|---|
| Insufficient balance | 409 at hold creation; nothing else written |
| No default active fund account | 409 |
| Razorpay rejects the request (bad account, low RazorpayX balance with queue off, etc.) | `RestClient.onStatus` → `ApiException` 502 with Razorpay's message; hold cancelled in the `catch`; DB rolls back |
| Razorpay says `failed` right away | hold cancelled, 409 |
| Duplicate `Idempotency-Key` | 409 (does **not** replay the old result) |
| Duplicate webhook | ignored (same status) |
| Out-of-order webhook (e.g. `processing` after `processed`) | 409 on transition validation (Razorpay will keep retrying a non-2xx — consider returning 200 for harmless stale events) |
| Webhook for unknown payout | 404 |
| Payout is slow (>30 min) | ⚠ `releaseWalletHold` checks `expires_at` and throws "Wallet hold has expired" → hold stays ACTIVE. See Documentation §17 item 2 |
| DB commit fails **after** Razorpay accepted the payout | Money was sent but not recorded locally (RestClient call is inside the DB transaction). Webhook then returns 404. Reconcile via `GET /payouts/{pout}/gateway`. A future improvement is "save payout first, call gateway after commit". |

## 2.6 Payout cheat sheet (one page)

```
ONBOARD (once)  POST /fund-accounts
   RestClient POST /contacts       → cont_xxx   (beneficiaries.gateway_contact_id)
   RestClient POST /fund_accounts  → fa_xxx     (fund_accounts.gateway_fund_account_id), first = default

WITHDRAW        POST /payouts (Idempotency-Key)           [@Transactional]
   find beneficiary(ACTIVE) → default fund account → PROVIDER wallet (by beneficiary.ownerReference)
   wallet HOLD   : available−  pending+      (wallet_holds ACTIVE, 30 min)
   INSERT payouts(CREATED)
   RestClient POST /payouts → pout_xxx, status
   failed now? → cancel hold, 409          ok? → hold.payout=payout, ledger DEBIT, payout_event(API)

CONFIRM         Webhook payout.*  (signature verified)
   processing → record only
   processed  → utr/fees/tax, RELEASE hold (pending−, totalDebited+)
   failed/reversed → CANCEL hold (pending−, available+)
```

---

# Part 3 — Comparison, notes & self-test

## 3.1 Pay-in vs Payout side by side

| | **Pay-in** | **Payout** |
|---|---|---|
| Money direction | Customer → Platform | Platform → Provider bank |
| Razorpay product | Payments | RazorpayX |
| Technique | Razorpay Java SDK | Spring RestClient |
| Entry endpoint | `POST /payment-intents` | `POST /payouts` |
| First local record | `payment_intents` | `wallet_holds` + `payouts` |
| Who confirms success | `/payments/verify` **or** webhook `payment.captured` | webhook `payout.processed` (or poll `GET /payouts/{id}/gateway`) |
| Idempotency | `idempotency_keys` table (replay) | `payouts.idempotency_key` unique (409) |
| Wallet effect | Settlement **credits** provider wallet | Hold **reserves**, then **releases/cancels** |
| Extra setup | none | Contact + Fund Account (RestClient) |
| Signature check | HMAC(order_id\|payment_id, **key secret**) | webhook HMAC with **webhook secret** (same endpoint as all webhooks) |

## 3.2 Notes to remember

1. **Never trust the browser.** The code verifies the signature *and* re-fetches the payment from Razorpay.
2. **Webhooks are the source of truth** for async results; the verify API is a convenience. Make every handler idempotent (they are).
3. **All money is Long paise.** ₹1 = 100.
4. **One transaction = payment + settlement + wallet credit.** All or nothing.
5. **Hold before payout.** Reserve first, call the bank second, confirm third.
6. **Wallet ≠ Beneficiary.** Wallet is created automatically on first earning. Beneficiary/fund account is only needed to withdraw. Link = same `ownerReference` value.
7. **Two ids for a provider:** `ownerReference` (your stable id, e.g. `PROV-77`; used for wallet, settlement, `settlementBeneficiaryReference`, and fund-account request) vs `beneficiaryReference` (a generated ULID; used only in `POST /payouts`).
8. **Everything is audited:** `*_events` tables, `wallet_transactions` ledger, raw gateway JSON columns.
9. **Verify request:** the DTO requires `paymentReference` too (`@NotBlank`), even though the service looks the payment up by `gatewayOrderId`. (My first document listed only 3 fields — the DTO has 4.)
10. **Retry on same order:** `validateRequest` blocks a second intent for the same `orderReference`, so after a `FAILED` payment you currently can't create a fresh payment for the same order. Decide whether to allow a new attempt (new Payment under the same intent, or new intent when the old one is FAILED/EXPIRED).

## 3.3 Self-test questions (answers are in this file)

1. Which two things does the frontend need from the Phase-A response to open Checkout?
2. Why does `completePayment` call `razorpayClient.payments.fetch` again after the signature was already verified?
3. What happens if the webhook arrives *before* the frontend calls `/verify`?
4. Which table guarantees a payment is never settled twice?
5. Why can a provider have money in the wallet without any beneficiary?
6. Why do we create a wallet *hold* before calling RazorpayX?
7. After `payout.failed`, what are `available`, `pending`, `totalDebited`?
8. Why did you use `RestClient` for payouts but the SDK for payments?
9. Which id goes in `POST /payouts` — `PROV-77` or the beneficiary ULID?
10. What stops a duplicate refund click? What stops a duplicate payout click? (Are they the same?)

<details><summary>Answers</summary>

1. `gatewayOrderId` and `gatewayKey` (plus amount/currency).
2. To get the authoritative `status` (`captured`), payment method and full JSON from Razorpay rather than trusting client data.
3. The webhook completes the payment; the later `/verify` returns "Payment already verified." (or the webhook is ignored if verify was first).
4. `settlements` — `UNIQUE(payment_id)` plus the `existsByPayment` check.
5. Since V20/V21, wallets are keyed by `(owner_reference, owner_type)`, not by a beneficiary FK.
6. Bank transfers are asynchronous; the hold prevents spending/withdrawing the same money twice while waiting.
7. `available` back to the original amount, `pending 0`, `totalDebited` unchanged.
8. The Razorpay Java SDK covers Payments (orders, payments, refunds, signatures) but not RazorpayX endpoints.
9. The beneficiary **ULID** (`beneficiaryReference`).
10. Refund: `idempotency_keys` table with request hash and replay. Payout: unique `payouts.idempotency_key` → returns 409. Not the same.
</details>

*Generated from the source code on 22 Oct 2026.*
