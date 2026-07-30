# Method: `debitWallet(...)`

```java
WalletEntity debitWallet(
    WalletEntity wallet,
    Long amount,
    WalletTransactionSource transactionSource,
    WalletReferenceType referenceType,
    String referenceId,
    String description,
    JsonNode metadata
)
```

---

# Overview

The `debitWallet()` method is responsible for deducting money from a beneficiary's wallet.

Whenever the platform needs to remove money from a wallet, this method decreases the available balance, updates the total debited amount, and creates an immutable wallet transaction.

Unlike `createWalletHold()`, this method permanently deducts money from the wallet.

---

# When is this method used?

Typical use cases:

- Manual wallet adjustment by an administrator.
- Platform correction.
- Refund adjustment (if business requires).
- Direct wallet debit.

> **Note:** In the payout flow, this method is **not** used directly. Payouts use `createWalletHold()` first, and the actual deduction is completed later through `releaseWalletHold()`.

---

# Method Parameters

## 1. WalletEntity wallet

The wallet from which money will be deducted.

Example

```
Wallet Reference

WAL01KXYZABC123
```

Current Wallet

| Available Balance | Pending Balance |
|------------------:|----------------:|
|            $500   |            $0   |

---

## 2. Long amount

The amount to deduct from the wallet.

The amount must always be greater than zero.

Example

```
Amount

$150
```

Meaning:

Reduce the wallet balance by $150.

---

## 3. WalletTransactionSource transactionSource

Represents the business operation that caused this debit.

Possible values

```java
PAYOUT
REFUND
ADJUSTMENT
```

Example

```java
WalletTransactionSource.ADJUSTMENT
```

Meaning:

The wallet is being debited because of an administrative adjustment.

---

## 4. WalletReferenceType referenceType

Represents the business entity related to this transaction.

Example values

```java
PAYMENT
PAYOUT
REFUND
```

Example

```java
WalletReferenceType.REFUND
```

Meaning:

This debit belongs to a refund.

---

## 5. String referenceId

Stores the unique reference of the related business object.

Example

```
REF01KXYZ123ABC
```

This allows anyone to trace the wallet transaction back to the refund.

---

## 6. String description

Human-readable explanation of why the debit occurred.

Example

```
Manual wallet adjustment by administrator.
```

Useful for:

- Customer Support
- Audit
- Admin Dashboard

---

## 7. JsonNode metadata

Stores additional business information.

Example

```json
{
    "reason": "Incorrect settlement",
    "adjustedBy": "Admin"
}
```

This information is optional and provides additional context.

---

# Business Flow

```text
Debit Request
      │
      ▼
debitWallet()
      │
      ▼
Validate Amount
      │
      ▼
Validate Available Balance
      │
      ▼
Capture Current Balance
      │
      ▼
Update Wallet
      │
      ▼
Save Wallet
      │
      ▼
Create Wallet Transaction
      │
      ▼
Return Updated Wallet
```

---

# Example

### Before

| Available Balance | Total Debited |
|------------------:|--------------:|
|            $500   |         $100  |

Debit Amount

```
$150
```

### After

| Available Balance | Total Debited |
|------------------:|--------------:|
|            $350   |        $250   |

A debit wallet transaction is also created.

---

# Internal (Sub) Methods

## Step 1 - Validate Amount

Purpose

Ensure the debit amount is valid.

Checks

- Amount is not null.
- Amount is greater than zero.

Example

```
Amount = $150

✓ Valid
```

---

## Step 2 - Validate Available Balance

Purpose

Ensure sufficient funds exist before deducting money.

Current Wallet

```
Available Balance = $500
```

Debit Request

```
$150
```

Since

```
500 >= 150
```

The debit is allowed.

If

```
Available Balance = $100

Debit = $150
```

Result

```
ConflictException

Insufficient wallet balance.
```

---

## Step 3 - Capture Current Balance

Purpose

Store the balance before modification.

Example

```
Balance Before

$500
```

This value becomes the transaction's `balanceBefore`.

---

## Step 4 - Update Wallet

Formula

```
New Available Balance

=

Current Available Balance

-

Debit Amount
```

Example

```
500 - 150 = 350
```

Update Total Debited

```
100 + 150 = 250
```

Updated fields

- availableBalance
- totalDebited

---

## Step 5 - Save Wallet

Persist the updated wallet.

Updated table

```
wallets
```

Updated columns

- available_balance
- total_debited
- updated_at

---

## Step 6 - Create Wallet Transaction

Create an immutable debit transaction.

Example

| Field              | Value           |
|--------------------|-----------------|
| Transaction Type   | DEBIT           |
| Transaction Source | ADJUSTMENT      |
| Amount             | $150            |
| Balance Before     | $500            |
| Balance After      | $350            |
| Reference Type     | REFUND          |
| Reference Id       | REF01KXYZ123ABC |

This transaction becomes part of the permanent wallet ledger.

---

# Difference Between debitWallet() and createWalletHold()

| debitWallet()                             | createWalletHold()                                |
|-------------------------------------------|---------------------------------------------------|
| Money is permanently deducted.            | Money is only reserved.                           |
| Available balance decreases.              | Available balance decreases.                      |
| Pending balance does not change.          | Pending balance increases.                        |
| Creates a wallet transaction immediately. | Does not create a wallet transaction immediately. |
| Used for direct debits and adjustments.   | Used before payouts.                              |

---

# Summary

The `debitWallet()` method permanently removes money from a beneficiary's wallet.
It validates the available balance, updates the wallet's available balance and total debited amount, saves the wallet, and creates an immutable debit transaction.
Every successful debit operation is fully traceable through the wallet transaction ledger.