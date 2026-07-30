# releaseWalletHold()

## Overview

The `releaseWalletHold()` operation finalizes a successful payout.

When Razorpay confirms that the payout has been successfully processed, the reserved money is permanently deducted from the wallet.

This method completes the payout lifecycle.

---

# Why Do We Need This Method?

When a payout request is created, the money is only reserved.

The beneficiary still technically owns that money until the payment gateway successfully transfers it.

Only after receiving a successful webhook should the platform permanently deduct the reserved amount.

This is the responsibility of `releaseWalletHold()`.

---

# When Is It Used?

This method is executed after receiving a successful payout webhook.

Example webhook statuses:

- PROCESSED
- COMPLETED
- SUCCESS

Flow

```

Create Wallet Hold
│
▼
Create Payout
│
▼
Create Wallet Transaction
│
▼
Razorpay Processes Payout
│
▼
Webhook Received
│
▼
releaseWalletHold()

```

---

# What Happens During This Operation?

The reserved money is removed from the pending balance.

The wallet now considers that money permanently paid out.

Changes:

- Pending Balance decreases.
- Total Debited increases.
- Wallet Hold status becomes RELEASED.

The available balance does **not** change because it was already reduced when the wallet hold was created.

---

# Example

Before Release

| Balance       | Amount |
|---------------|-------:|
| Available     |   $600 |
| Pending       |   $400 |
| Total Debited |   $200 |

Webhook

```

Payout Status = PROCESSED

```

After Release

| Balance       | Amount |
|---------------|-------:|
| Available     |   $600 |
| Pending       |     $0 |
| Total Debited |   $600 |

Notice:

The available balance remains the same.

Only the pending balance disappears because the payout has now completed.

---

# Why Doesn't Available Balance Change?

The available balance was already reduced during wallet hold creation.

Timeline

```

Original Balance

Available = $1,000

↓

Wallet Hold

Available = $600
Pending = $400

↓

Payout Success

Available = $600
Pending = $0

```

The money has already left the available balance.

Release simply finalizes the reservation.

---

# Why Increase Total Debited?

`totalDebited` represents money that has actually left the wallet.

During wallet hold:

Money is only reserved.

During release:

Money is officially transferred.

Therefore, total debited increases only now.

---

# Why Isn't Another Wallet Transaction Created?

A wallet transaction already exists.

Creating another debit transaction would record the same payout twice.

Instead:

```

Wallet Hold
↓

Wallet Transaction
↓

Webhook
↓

Release Hold

```

Only the wallet balances and hold status change.

The financial ledger remains unchanged.

---

# Real-World Example (NutraHive)

Nutritionist Wallet

| Balance   | Amount |
|-----------|-------:|
| Available |   $700 |
| Pending   |   $500 |

Razorpay webhook

```

Status = PROCESSED

```

After release

| Balance       | Amount |
|---------------|-------:|
| Available     |   $700 |
| Pending       |     $0 |
| Total Debited |  +$500 |

The payout has now been successfully completed.

---

# What If the Webhook Never Arrives?

The wallet hold remains ACTIVE.

The reserved money stays in the pending balance until:

- Success webhook arrives.
- Failure webhook arrives.
- Manual intervention.
- Scheduled recovery process.

This prevents the platform from assuming that money has left before confirmation.

---

# Summary

`releaseWalletHold()` completes the payout lifecycle after a successful webhook. It removes the reserved amount from the pending balance, increases the total debited amount, and marks the wallet hold as released. Since the wallet transaction was already created earlier, this method only finalizes the reservation and does not create another ledger entry.