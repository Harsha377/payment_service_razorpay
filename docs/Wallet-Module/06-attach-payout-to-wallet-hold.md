# attachPayoutToWalletHold()

## Overview

The `attachPayoutToWalletHold()` operation links a newly created payout with an existing wallet hold.

A wallet hold is created **before** sending a payout request to the payment gateway. At that point, the hold only knows that a certain amount of money has been reserved, but it does not know which payout it belongs to.

Once the payout has been successfully created, this method associates the payout with the wallet hold, establishing a permanent relationship between them.

This operation does **not** modify any wallet balances.

---

# Why Do We Need This Method?

When a payout request begins, the platform performs the following steps:

1. Reserve the requested amount using a wallet hold.
2. Create a payout record.
3. Send the payout request to Razorpay.

Since the payout does not exist when the wallet hold is created, the hold cannot immediately store a payout reference.

After the payout is successfully created, the platform needs a way to connect both records.

That is the purpose of `attachPayoutToWalletHold()`.

---

# When Is It Used?

This method is executed immediately after the payout has been successfully created.

Typical flow:

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

Without this step, the wallet hold and payout remain completely unrelated.

---

# Why Can't We Attach the Payout Earlier?

At the time the wallet hold is created, the payout record does not yet exist.

Sequence:

```
Step 1

Wallet Hold

✓ Exists

Payout

✗ Not Created
```

After creating the payout:

```
Step 2

Wallet Hold

✓ Exists

Payout

✓ Exists
```

Only now can both entities be connected.

---

# What Happens During This Operation?

This method performs only one responsibility:

- Assign the payout to the wallet hold.
- Save the updated wallet hold.

Nothing else changes.

It does **not**:

- Debit the wallet.
- Credit the wallet.
- Update balances.
- Create wallet transactions.
- Call Razorpay.

Its only responsibility is creating the relationship.

---

# Example

Suppose the following records exist.

## Wallet Hold

| Property       | Value    |
|----------------|----------|
| Hold Reference | HOLD001  |
| Amount         | $400     |
| Payout         | NULL     |
| Status         | ACTIVE   |

---

## Payout

| Property         | Value     |
|------------------|-----------|
| Payout Reference | PAYOUT001 |
| Amount           | $400      |
| Status           | PENDING   |

After calling this method:

## Wallet Hold

| Property       | Value     |
|----------------|-----------|
| Hold Reference | HOLD001   |
| Amount         | $400      |
| Payout         | PAYOUT001 |
| Status         | ACTIVE    |

The only difference is that the wallet hold now knows which payout owns the reserved money.

---

# Why Is This Relationship Important?

Later, when Razorpay sends webhook events such as:

- processed
- failed
- cancelled
- reversed

The platform receives only payout information.

To update the wallet correctly, the system must determine which wallet hold belongs to that payout.

Relationship:

```
Webhook
    │
    ▼
Payout
    │
    ▼
Wallet Hold
    │
    ▼
Wallet
```

Without this relationship, the system cannot determine which reserved balance should be released or cancelled.

---

# Real-World Example (NutraHive)

A nutritionist requests a payout of **$500**.

Current process:

```
Wallet

Available = $1,200
Pending = $0
```

Create Wallet Hold

```
Available = $700
Pending = $500
```

Now create payout.

```
Payout Reference

PAYOUT001
```

The wallet hold currently looks like:

```
Hold Reference

HOLD001

Payout

NULL
```

After attaching:

```
Hold Reference

HOLD001

Payout

PAYOUT001
```

Now whenever Razorpay reports anything about `PAYOUT001`, the platform immediately knows which wallet hold should be updated.

---

# What Happens Next?

Once the payout has been attached, the platform creates the wallet transaction and waits for webhook events.

Possible outcomes:

### Scenario 1 - Payout Processed

```
Wallet Hold
        │
        ▼
Webhook (Processed)
        │
        ▼
Release Wallet Hold
        │
        ▼
Money Permanently Deducted
```

---

### Scenario 2 - Payout Failed

```
Wallet Hold
        │
        ▼
Webhook (Failed)
        │
        ▼
Cancel Wallet Hold
        │
        ▼
Money Returned To Available Balance
```

---

# Why Doesn't This Method Update Wallet Balances?

The balances were already updated during `createWalletHold()`.

At this stage:

- Available balance is already reduced.
- Pending balance is already increased.

Attaching a payout is simply creating a reference between two records.

Updating balances again would produce incorrect financial data.

---

# Why Doesn't This Method Create a Wallet Transaction?

No financial movement occurs.

The money is already reserved.

The payout has not yet completed.

Since there is no new credit or debit, there is no reason to create another wallet transaction.

---

# Summary

`attachPayoutToWalletHold()` establishes the relationship between a wallet hold and the payout that owns it. It is executed immediately after a payout is created and allows future webhook events to locate the correct wallet hold. This method does not modify balances or create ledger entries—it simply connects two business entities so that the payout lifecycle can be completed correctly.