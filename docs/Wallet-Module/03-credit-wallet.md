# Method: `creditWallet(...)`

```java
WalletEntity creditWallet(
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

The `creditWallet()` method is responsible for adding money to a beneficiary's wallet.

Whenever the platform decides that money belongs to a beneficiary (for example, after a successful settlement), this method updates the wallet balance and creates an immutable wallet transaction.

This method always performs two operations:

1. Updates the wallet balance.
2. Creates a wallet transaction (ledger entry).

---

# Method Parameters

## 1. WalletEntity wallet

Represents the wallet that should receive the money.

Instead of searching the database again, the wallet is passed directly to this method.

Example

```
Wallet Reference

WAL01KXYZABC123
```

Current Wallet

| Available | Pending |
|-----------|---------|
| $500 | $0 |

---

## 2. Long amount

The amount to be credited into the wallet.

This amount is always positive.

Example

```
Amount

$120
```

Meaning:

Increase wallet balance by $120.

---

## 3. WalletTransactionSource transactionSource

Specifies **why** this money is being credited.

It identifies the business operation that generated the transaction.

Example values

```java
PAYMENT
REFUND
ADJUSTMENT
SETTLEMENT
```

Example

```
Transaction Source

SETTLEMENT
```

Meaning:

The wallet is being credited because a payment has been settled.

---

## 4. WalletReferenceType referenceType

Specifies which business entity this transaction belongs to.

Example values

```java
PAYMENT
PAYOUT
REFUND
SETTLEMENT
```

Example

```
Reference Type

PAYMENT
```

This tells us that the wallet transaction belongs to a payment.

---

## 5. String referenceId

Stores the unique reference of the business object.

Example

```
PAY01KXYZABCD123
```

Later, anyone can trace the wallet transaction back to the original payment.

Relationship

```
Wallet Transaction
        │
        ▼
Reference Type = PAYMENT

Reference Id = PAY01KXYZABCD123
```

---

## 6. String description

A human-readable explanation.

Example

```
Settlement completed for Appointment #1025
```

This is useful for:

- Audit
- Customer Support
- Admin Dashboard

---

## 7. JsonNode metadata

Stores additional information that doesn't belong in fixed database columns.

Example

```json
{
  "appointmentReference": "APT123",
  "customerReference": "CUS001",
  "nutritionistReference": "NUT101"
}
```

Metadata allows future enhancements without changing the database schema.

---

# Business Flow

```text
Settlement Completed
        │
        ▼
creditWallet()
        │
        ▼
Validate Amount
        │
        ▼
Calculate New Balance
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

Before

| Available Balance | Total Credited |
|------------------:|---------------:|
|            $500   |       $1,000  
|

Settlement Amount

```
$120
```

After

| Available Balance | Total Credited |
|------------------:|---------------:|
|            $620   |       $1,120   |

A wallet transaction is also created.

---

# Internal (Sub) Methods

The `creditWallet()` method is typically implemented using smaller logical steps.

## Step 1 - Validate Amount

Purpose

Ensure that the amount is valid before updating the wallet.

Checks

- Amount is not null
- Amount is greater than zero

Example

```
Amount = $120

✓ Valid
```

---

## Step 2 - Capture Current Balance

Purpose

Store the wallet balance before modification.

Example

```
Available Balance

$500
```

This value becomes the transaction's `balanceBefore`.

---

## Step 3 - Update Wallet Balance

Purpose

Increase the available balance.

Formula

```
New Available Balance

=

Current Available Balance

+

Credit Amount
```

Example

```
500 + 120 = 620
```

Also update:

```
Total Credited

1000 + 120

=

1120
```

---

## Step 4 - Save Wallet

Persist the updated wallet.

Database

```
wallets
```

Updated Fields

- available_balance
- total_credited
- updated_at

---

## Step 5 - Create Wallet Transaction

Create an immutable ledger entry.

Example

| Field          | Value            |
|----------------|------------------|
| Type           | CREDIT           |
| Source         | SETTLEMENT       |
| Amount         | $120             |
| Balance Before | $500             |
| Balance After  | $620             |
| Reference Type | PAYMENT          |
| Reference Id   | PAY01KXYZABCD123 |

This record provides a complete audit trail of the credit operation.

---

# Summary

The `creditWallet()` method credits money to a beneficiary's wallet by updating the available balance and total credited amount.
It also creates an immutable wallet transaction that records the source of the funds, the related business reference, and the wallet balance before and after the credit, ensuring complete financial traceability.
