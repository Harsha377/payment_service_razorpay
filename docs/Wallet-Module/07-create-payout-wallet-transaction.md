# createPayoutWalletTransaction()

## Overview

The `createPayoutWalletTransaction()` operation creates an immutable wallet transaction for a payout.

When a payout is successfully created and linked to a wallet hold, the platform records a wallet transaction to maintain a permanent financial audit trail.

Unlike `releaseWalletHold()`, this method **does not modify the wallet balance**. The wallet balances were already updated during `createWalletHold()`.

Its only responsibility is to record that a payout transaction has been initiated.

---

# Why Do We Need This Method?

Every financial movement inside a wallet must be traceable.

Imagine a nutritionist asks:

> "Why was $500 deducted from my wallet?"

Without a wallet transaction, there would be no financial record explaining:

- Why money was reserved.
- Which payout used it.
- What the balance was before.
- What the balance became.

This method creates that permanent audit record.

---

# When Is It Used?

This method is called immediately after:

1. Wallet Hold is created.
2. Payout is created.
3. Wallet Hold is attached to the payout.

Flow

```

Beneficiary Requests Payout
│
▼
Create Wallet Hold
│
▼
Create Payout
│
▼
Attach Payout To Wallet Hold
│
▼
Create Wallet Transaction
│
▼
Wait For Webhook

```

---

# Why Is It Created Before the Webhook?

Creating the wallet transaction early provides a complete financial history.

Even if Razorpay takes several minutes to process the payout, the platform already knows:

- A payout request exists.
- Money has been reserved.
- Which payout owns the reservation.

This makes debugging and auditing much easier.

---

# What Does This Method Store?

A wallet transaction records:

- Transaction Type
- Transaction Source
- Amount
- Balance Before
- Balance After
- Reference Type
- Reference Id
- Description
- Metadata

This information never changes.

---

# Example

Wallet before payout

| Balance   | Amount |
|-----------|-------:|
| Available |   $600 |
| Pending   |   $400 |

Wallet Hold

| Property        | Value  |
|-----------------|--------|
| Hold Amount     | $400   |
| Balance Before  | $1,000 |
| Balance After   | $600   |

Payout

```

PAYOUT001

```

Wallet Transaction

| Field            | Value      |
|------------------|------------|
| Transaction Type | DEBIT      |
| Source           | PAYOUT     |
| Amount           | $400       |
| Balance Before   | $1,000     |
| Balance After    | $600       |
| Reference Type   | PAYOUT     |
| Reference Id     | PAYOUT001  |

Notice that the wallet transaction uses the balance captured by the wallet hold.

It does **not** calculate the current wallet balance again.

---

# Why Use Wallet Hold Balances?

Suppose multiple payout requests are being processed.

Wallet balance may change before this method executes.

If the current wallet balance is used, the transaction history becomes incorrect.

Instead, the wallet hold stores:

```

Balance Before
Balance After

```

at the exact moment the hold was created.

These values guarantee that the wallet transaction always reflects the original financial event.

---

# Does This Method Update Wallet Balance?

No.

The wallet balance was already updated when the wallet hold was created.

Current state

| Balance   | Amount |
|-----------|-------:|
| Available |   $600 |
| Pending   |   $400 |

After creating the wallet transaction

| Balance   | Amount |
|-----------|-------:|
| Available |   $600 |
| Pending   |   $400 |

No balance changes.

---

# Why Is This Transaction Immutable?

Financial records must never be edited.

If a payout later fails, the platform creates new business actions instead of modifying existing transactions.

This ensures:

- Complete audit history.
- Regulatory compliance.
- Accurate financial reporting.

---

# Real-World Example

A nutritionist has an available balance of **$1,000**.

They request a payout of **$400**.

Wallet Hold

```

Available = $600
Pending = $400

```

Payout

```

PAYOUT001

```

Wallet Transaction

```

Debit

Amount = $400

Reference = PAYOUT001

```

Later, the webhook determines whether the payout succeeds or fails, but this transaction remains unchanged.

---

# Summary

`createPayoutWalletTransaction()` records an immutable ledger entry for a payout. It does not modify wallet balances. Instead, it creates a permanent audit record using the balance information captured during wallet hold creation, ensuring complete financial traceability.