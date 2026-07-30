# Wallet Service Guide

## Method: `createWallet(BeneficiaryEntity beneficiary)`

### Overview

Creates a new wallet for a beneficiary. Every beneficiary must own exactly one wallet before they can receive settlements or request payouts.

This method is typically executed immediately after a beneficiary is successfully onboarded.

---

## Business Scenario

Consider the following onboarding flow:

1. A nutritionist registers on the platform.
2. A beneficiary record is created in Razorpay.
3. The beneficiary is stored in the local database.
4. A wallet is created for the beneficiary.
5. The wallet is now ready to receive future settlements.

Without a wallet, the beneficiary cannot receive earnings or request payouts.

---

## Execution Flow

```mermaid
flowchart TD
    A[Beneficiary Created] --> B[WalletService.createWallet()]
    B --> C{Wallet Already Exists?}

    C -->|Yes| D[Throw ConflictException]

    C -->|No| E[Generate Wallet Reference]
    E --> F[Initialize Wallet]
    F --> G[Available Balance = 0]
    G --> H[Pending Balance = 0]
    H --> I[Total Credited = 0]
    I --> J[Total Debited = 0]
    J --> K[Status = ACTIVE]
    K --> L[Save Wallet]
    L --> M[Return Wallet]
```

---

## Step-by-Step Execution

### Step 1 - Receive Beneficiary

The service receives a valid `BeneficiaryEntity`.

```java
WalletEntity wallet = walletService.createWallet(beneficiary);
```

Example:

| Property              | Value                    |
|-----------------------|--------------------------|
| Beneficiary Reference | BEN01K7H4T5P8N2M6Q9R1X3Y |
| Name                  | Dr. John                 |
| Status                | ACTIVE                   |

---

### Step 2 - Validate Beneficiary

The service validates that:

- Beneficiary is not `null`
- Beneficiary exists
- Beneficiary status is `ACTIVE`

If any validation fails, wallet creation stops immediately.

---

### Step 3 - Check Existing Wallet

The repository checks whether the beneficiary already owns a wallet.

```java
walletRepository.findByBeneficiary(beneficiary);
```

If a wallet already exists:

```
ConflictException

Wallet already exists for beneficiary.
```

This enforces the one-wallet-per-beneficiary rule.

---

### Step 4 - Create Wallet

A new wallet is initialized.

| Field             | Initial Value |
|-------------------|---------------|
| Available Balance | 0             |
| Pending Balance   | 0             |
| Total Credited    | 0             |
| Total Debited     | 0             |
| Status            | ACTIVE        |

At this point, no money has been credited.

---

### Step 5 - Persist Wallet

The wallet is saved.

```java
walletRepository.save(wallet);
```

---

## Database Changes

### Before

### wallets

_No record exists._

### After

| Column            | Value                    |
|-------------------|--------------------------|
| wallet_reference  | WAL01K7H5B4C9T8P2Q6M1N4X |
| beneficiary_id    | 15                       |
| available_balance | 0                        |
| pending_balance   | 0                        |
| total_credited    | 0                        |
| total_debited     | 0                        |
| status            | ACTIVE                   |

---

## Wallet Balance

### Before

| Balance        | Amount  |
|----------------|---------|
| Available      | $0      |
| Pending        | $0      |
| Total Credited | $0      |
| Total Debited  | $0      |

### After

| Balance         | Amount |
|-----------------|--------|
| Available       | $0     |
| Pending         | $0     |
| Total Credited  | $0     |
| Total Debited   | $0     |

No financial balance changes because wallet creation is not a monetary operation.

---

## Wallet Transactions

No wallet transaction is created.

Reason:

A wallet transaction represents movement of money.

Creating a wallet does not move funds.

---

## Exceptions

| Scenario              | Exception                   |
|-----------------------|-----------------------------|
| Beneficiary not found | `ResourceNotFoundException` |
| Beneficiary inactive  | `ConflictException`         |
| Wallet already exists | `ConflictException`         |

---

## Summary

`createWallet()` prepares a beneficiary for future financial operations. It creates a single active wallet with zero balances and ensures the beneficiary is ready to receive settlements and request payouts.


---