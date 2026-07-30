# createWalletHold()

## Overview

The `createWalletHold()` operation reserves a specific amount of money in a beneficiary's wallet before initiating a payout.

It temporarily moves the requested amount from the **Available Balance** to the **Pending Balance**, ensuring that the reserved money cannot be used for another payout or withdrawn again while the payout is being processed.

This operation does **not** permanently deduct money from the wallet. It simply locks the amount until the payout succeeds or fails.

---

# Why Do We Need Wallet Hold?

Imagine a beneficiary has an available balance of **$1,000** and requests a payout of **$400**.

If the platform immediately sends the payout request to the payment gateway without reserving the money, several problems can occur:

- The beneficiary could submit another payout request for the same money.
- Multiple payout requests could use the same balance.
- Concurrent requests could result in duplicate withdrawals.

To avoid these issues, the platform first reserves the requested amount.

Once reserved, that amount cannot be used again until the payout is completed or cancelled.

---

# When Is It Used?

This method is used **before creating a payout**.

Typical payout flow:

```
Beneficiary Requests Payout
            │
            ▼
Find Wallet
            │
            ▼
Create Wallet Hold
            │
            ▼
Create Payout
            │
            ▼
Send Request to Razorpay
```

The wallet hold always happens before the payout request is sent to the payment gateway.

---

# What Happens During Wallet Hold?

The following actions occur:

- Verify the wallet has enough available balance.
- Reserve the requested amount.
- Reduce the available balance.
- Increase the pending balance.
- Store a wallet hold record.
- Return the created wallet hold.

At this stage, **no money has left the platform**.

The amount is only reserved internally.

---

# Example

Suppose the wallet currently contains:

| Balance Type      | Amount |
|-------------------|-------:|
| Available Balance | $1,000 |
| Pending Balance   |   $0   |

The beneficiary requests a payout of **$400**.

After creating the wallet hold:

| Balance Type      | Amount |
|-------------------|-------:|
| Available Balance |   $600 |
| Pending Balance   | $400   |

Notice that the total money inside the wallet has not changed.

```
Before Hold

Available = $1,000
Pending   = $0

Total Money = $1,000
```

```
After Hold

Available = $600
Pending   = $400

Total Money = $1,000
```

The money has only moved from one balance to another.

---

# Why Is Money Moved to Pending Balance?

The pending balance represents money that is already committed to an operation.

Although the payout has not yet completed, the platform has already decided that this money should no longer be available for new payout requests.

This prevents:

- Duplicate payouts
- Double spending
- Race conditions
- Concurrent balance updates

---

# Why Isn't a Wallet Transaction Created?

A wallet transaction represents the actual movement of money into or out of a wallet.

During a wallet hold:

- No money has been credited.
- No money has been debited.
- No payout has been completed.

Only the balance classification changes.

For this reason, **no wallet transaction is created**.

The transaction will be created only after the payout has been successfully accepted and linked to the wallet hold.

---

# What Happens Next?

After a wallet hold is created, there are two possible outcomes.

### Scenario 1 – Payout Succeeds

```
Wallet Hold Created
        │
        ▼
Payout Created
        │
        ▼
Gateway Processes Payout
        │
        ▼
Release Wallet Hold
```

The pending balance is finalized, and the money is permanently deducted.

---

### Scenario 2 – Payout Fails

```
Wallet Hold Created
        │
        ▼
Gateway Rejects Payout
        │
        ▼
Cancel Wallet Hold
```

The reserved amount is returned to the available balance.

The beneficiary can request another payout later.

---

# Real-World Example (NutraHive)

A nutritionist has earned **$1,200** from completed consultations.

Current wallet:

| Balance Type      | Amount |
|-------------------|-------:|
| Available Balance | $1,200 |
| Pending Balance   |   $0   |

The nutritionist requests a payout of **$500**.

Instead of immediately sending the money to the bank, the platform first creates a wallet hold.

After the hold:

| Balance Type      | Amount |
|-------------------|-------:|
| Available Balance | $700   |
| Pending Balance   |   $500 |

Now the nutritionist cannot accidentally request another payout using the same $500.

Once Razorpay confirms the payout:

- Pending balance is released.
- The payout is completed.
- The wallet reflects the final deduction.

---

# Summary

`createWalletHold()` is a reservation mechanism that temporarily locks funds before initiating a payout. It moves money from the available balance to the pending balance without permanently deducting it. This ensures financial consistency, prevents duplicate payouts, and guarantees that the same funds cannot be used by multiple payout requests while the payout is still being processed.