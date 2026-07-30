# Wallet Module

## Overview

The Wallet module is responsible for managing the earnings of beneficiaries (Nutritionists) on the platform.

Whenever a customer successfully pays for an appointment, the platform calculates the beneficiary's earnings and credits the beneficiary's wallet.

The wallet acts as an internal ledger until the beneficiary requests a withdrawal. Once a withdrawal request is approved, the system creates a payout through RazorpayX and transfers the money to the beneficiary's bank account or UPI.

The Wallet module is designed using accounting principles to ensure:

- Accurate balance calculation
- Complete transaction history
- Safe withdrawal processing
- Financial auditability
- Reconciliation with payment gateways

---

# Wallet Architecture

                    Customer Payment
                           │
                           ▼
                  Payment Captured
                           │
                           ▼
             Credit Beneficiary Wallet
                           │
                           ▼
                 Wallet Transaction
                           │
                           ▼
                 Available Balance
                           │
          ┌────────────────┴────────────────┐
          │                                 │
          ▼                                 ▼
Withdraw Request                  Future Earnings
│
▼
Wallet Hold
│
▼
RazorpayX Payout
│
▼
Webhook Confirmation
│
▼
Wallet Balance Updated

---

# Why Three Tables?

The Wallet module uses three separate tables because each table has a different responsibility.

| Table               | Responsibility                       |
|---------------------|--------------------------------------|
| wallets             | Stores the current wallet balance    |
| wallet_transactions | Stores every money movement (ledger) |
| wallet_holds        | Stores temporarily reserved money    |

Separating these responsibilities keeps the financial system accurate, auditable, and scalable.

---

# 1. wallets

## Purpose

The `wallets` table stores the **current financial state** of a beneficiary.

Think of it as the wallet summary.

It answers:

> "How much money does this beneficiary currently have?"

Only one wallet exists per beneficiary.

Example

Beneficiary

Harsha Nutrition

Wallet

Available Balance

₹5,000

Pending Balance

₹1,000

This table is updated whenever money is credited or debited.

---

## Common Fields

| Field             | Purpose                        |
|-------------------|--------------------------------|
| wallet_reference  | Unique wallet identifier       |
| beneficiary_id    | Owner of the wallet            |
| available_balance | Money available for withdrawal |
| pending_balance   | Money temporarily reserved     |
| total_credited    | Lifetime earnings credited     |
| total_debited     | Lifetime withdrawals           |
| currency          | Wallet currency (INR)          |
| status            | ACTIVE / INACTIVE              |
| created_at        | Wallet creation time           |
| updated_at        | Last update time               |

---

# 2. wallet_transactions

## Purpose

The `wallet_transactions` table is the financial ledger.

Every credit or debit creates a new transaction.

It answers:

> "Why is the wallet balance ₹5,000?"

Unlike the wallet table, transactions are **never updated or deleted**.

They provide the complete financial history.

Example

| Type     | Amount |
|----------|--------|
| CREDIT   | ₹1,000 |
| CREDIT   | ₹1,500 |
| DEBIT    | ₹500   |

Current Balance

₹2,000

This table is the source of truth for audits and reports.

---

## Common Fields

| Field                        | Purpose                              |
|------------------------------|--------------------------------------|
| wallet_transaction_reference | Unique transaction identifier        |
| wallet_id                    | Wallet owner                         |
| transaction_type             | CREDIT / DEBIT                       |
| transaction_source           | PAYMENT / PAYOUT / ADJUSTMENT        |
| amount                       | Transaction amount                   |
| balance_before               | Balance before transaction           |
| balance_after                | Balance after transaction            |
| reference_type               | PAYMENT / PAYOUT / REFUND            |
| reference_id                 | Reference to related business entity |
| description                  | Transaction description              |
| metadata                     | Additional information               |
| created_at                   | Transaction time                     |

---

# 3. wallet_holds

## Purpose

The `wallet_holds` table stores money that has been temporarily reserved.

This prevents the beneficiary from withdrawing the same money multiple times while a payout is being processed.

It answers:

> "How much money is currently reserved?"

Example

Available Balance

₹5,000

Beneficiary requests withdrawal

₹3,000

Wallet

Available

₹2,000

Pending

₹3,000

Wallet Hold

Amount

₹3,000

Status

ACTIVE

Once the payout succeeds, the hold is released.

If the payout fails, the hold is cancelled and the money becomes available again.

---

## Common Fields

| Field                 | Purpose                       |
|-----------------------|-------------------------------|
| wallet_hold_reference | Unique hold identifier        |
| wallet_id             | Wallet owner                  |
| payout_id             | Related payout                |
| amount                | Reserved amount               |
| status                | ACTIVE / RELEASED / CANCELLED |
| expires_at            | Hold expiry time              |
| created_at            | Hold creation time            |

---

# How These Tables Work Together

Customer pays ₹1,200

↓

Platform Commission = ₹200

↓

Beneficiary Earnings = ₹1,000

↓

wallet_transactions

Credit ₹1,000

↓

wallets

Available Balance = ₹1,000

↓

Beneficiary requests withdrawal

₹600

↓

wallet_holds

Reserve ₹600

↓

wallets

Available = ₹400

Pending = ₹600

↓

RazorpayX Payout

↓

Webhook

payout.processed

↓

wallet_transactions

Debit ₹600

↓

wallet_holds

Released

↓

wallets

Available = ₹400

Pending = ₹0

---

# Benefits of This Design

- Single source of truth for wallet balance
- Complete financial audit trail
- Prevents duplicate withdrawals
- Supports reconciliation with Razorpay
- Scalable for millions of transactions
- Production-ready financial architecture
- Easy reporting and debugging

---

# Summary

| Table               | Purpose                                             | Updated?    |
|---------------------|-----------------------------------------------------|-------------|
| wallets             | Current wallet balance                              | Yes         |
| wallet_transactions | Complete ledger of credits and debits               | Insert Only |
| wallet_holds        | Temporarily reserved money during payout processing | Yes         |   


# Wallet Enums

## Overview

Enums define the allowed states and transaction types used throughout the Wallet module.

Using enums instead of hardcoded strings provides:

- Type safety
- Better readability
- Compile-time validation
- Consistent business rules
- Easier maintenance

The Wallet module uses the following enums:

- WalletStatus
- WalletTransactionType
- WalletTransactionSource
- WalletHoldStatus
- WalletReferenceType

---

# 1. WalletStatus

## Purpose

Represents the current status of a beneficiary's wallet.

Every wallet always exists in one of these states.

```java
public enum WalletStatus {

    ACTIVE,

    INACTIVE,

    BLOCKED

}
```

### Values

| Status   | Description                                                       |
|----------|-------------------------------------------------------------------|
| ACTIVE   | Wallet is fully operational. Credits and withdrawals are allowed. |
| INACTIVE | Wallet exists but cannot be used yet. Usually during onboarding.  |
| BLOCKED  | Wallet has been blocked by the system or administrator.           |

---

## Business Example

Beneficiary completes onboarding

↓

Wallet Created

↓

Status

ACTIVE

The beneficiary can now receive earnings and request withdrawals.

---

Another Example

Fraud detected

↓

Administrator blocks wallet

↓

Status

BLOCKED

↓

No withdrawals allowed

---

# 2. WalletTransactionType

## Purpose

Represents whether money is entering or leaving the wallet.

```java
public enum WalletTransactionType {

    CREDIT,

    DEBIT

}
```

---

### CREDIT

Money is added to the wallet.

Examples

- Customer payment credited
- Manual adjustment
- Incentive
- Cashback

---

### DEBIT

Money is removed from the wallet.

Examples

- Withdrawal
- Refund adjustment
- Manual deduction

---

## Example

Wallet Balance

₹5,000

Customer Payment

₹1,000

Transaction Type

CREDIT

Wallet Balance

₹6,000

---

Beneficiary Withdrawal

₹2,000

Transaction Type

DEBIT

Wallet Balance

₹4,000

---

# 3. WalletTransactionSource

## Purpose

Represents the business module that generated the wallet transaction.

```java
public enum WalletTransactionSource {

    PAYMENT,

    PAYOUT,

    REFUND,

    ADJUSTMENT

}
```

---

## PAYMENT

Money credited after successful payment.

Example

Customer pays

₹1,200

↓

Platform Commission

₹200

↓

Nutritionist Earns

₹1,000

↓

Wallet Transaction

PAYMENT

---

## PAYOUT

Money debited when beneficiary withdraws.

Example

Beneficiary withdraws

₹500

↓

Wallet Transaction

PAYOUT

---

## REFUND

Money adjusted because of a refund.

Example

Customer refund affects beneficiary earnings.

↓

Wallet Transaction

REFUND

---

## ADJUSTMENT

Manual transaction created by an administrator.

Example

Bonus

↓

Wallet Transaction

ADJUSTMENT

---

# Why do we need Transaction Source?

Without source

Wallet Transaction

Credit

₹1,000

You don't know why it happened.

With source

Transaction Source

PAYMENT

Now the system knows exactly which module generated the transaction.

---

# 4. WalletHoldStatus

## Purpose

Represents the lifecycle of reserved wallet funds.

```java
public enum WalletHoldStatus {

    ACTIVE,

    RELEASED,

    CANCELLED,

    EXPIRED

}
```

---

## ACTIVE

Money has been reserved.

Withdrawal requested

↓

Hold Created

↓

Status

ACTIVE

---

## RELEASED

Money has been successfully transferred.

Withdrawal

↓

Payout Success

↓

Status

RELEASED

---

## CANCELLED

Hold removed because payout failed.

Withdrawal

↓

Payout Failed

↓

Hold Cancelled

↓

Money returned to available balance

---

## EXPIRED

Hold automatically expired.

Example

Withdrawal initiated

↓

Gateway timeout

↓

No response for configured duration

↓

Hold expires

---

# Hold Lifecycle

Withdrawal Requested

↓

ACTIVE

↓

Payout Processing

↓

┌──────────────┬───────────────┐
│              │
▼              ▼
Success        Failure

↓

RELEASED       CANCELLED

---

# 5. WalletReferenceType

## Purpose

Identifies which business entity created the wallet transaction.

```java
public enum WalletReferenceType {

    PAYMENT,

    PAYOUT,

    REFUND

}
```

---

## Why do we need this?

Every wallet transaction is associated with another business entity.

Examples

Payment

↓

Payment Reference

01KYABCD123...

---

Payout

↓

Payout Reference

01KYXYZ789...

---

Refund

↓

Refund Reference

01KYPQRS456...

---

The Reference Type tells the application which module owns the Reference Id.

Without Reference Type

Reference Id

01KYABC123

Impossible to know whether this belongs to

- Payment
- Refund
- Payout

With Reference Type

Reference Type

PAYMENT

Reference Id

01KYABC123

The relationship becomes clear.

---

# Summary

| Enum                    | Purpose                                                             |
|-------------------------|---------------------------------------------------------------------|
| WalletStatus            | Represents the current state of a wallet                            |
| WalletTransactionType   | Represents whether money is credited or debited                     |
| WalletTransactionSource | Represents the business operation that generated the transaction    |
| WalletHoldStatus        | Represents the lifecycle of reserved wallet funds                   |
| WalletReferenceType     | Identifies the business entity associated with a wallet transaction |

---

# Complete Wallet Flow

Customer Payment

↓

PAYMENT Module

↓

WalletTransactionType = CREDIT

↓

WalletTransactionSource = PAYMENT

↓

Wallet Balance Updated

↓

Beneficiary Requests Withdrawal

↓

WalletHoldStatus = ACTIVE

↓

Payout Created

↓

Webhook Received

↓

WalletTransactionType = DEBIT

↓

WalletTransactionSource = PAYOUT

↓

WalletHoldStatus = RELEASED

↓

Wallet Updated