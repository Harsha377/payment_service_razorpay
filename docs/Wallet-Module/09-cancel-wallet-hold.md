# cancelWalletHold()

## Overview

The `cancelWalletHold()` operation releases the reserved amount back to the beneficiary's available balance when a payout fails after it has already been associated with a wallet hold.

This method ensures that the beneficiary does not lose money if the payout cannot be completed.

Unlike `releaseWalletHold()`, this method reverses the reservation instead of finalizing it.

---

# Why Do We Need This Method?

Not every payout request succeeds.

The payment gateway may reject the payout because of:

- Invalid bank account
- Bank server issues
- Beneficiary account closed
- Gateway validation failure
- Manual cancellation
- Payout reversal

Since the money was already reserved inside the wallet, it must now be returned to the beneficiary.

This method performs that reversal.

---

# When Is It Used?

This method is executed after receiving a failed payout webhook.

Typical webhook statuses:

- FAILED
- CANCELLED
- REVERSED

Flow

```
Create Wallet Hold
        │
        ▼
Create Payout
        │
        ▼
Attach Payout
        │
        ▼
Gateway Rejects Payout
        │
        ▼
cancelWalletHold()
```

---

# What Happens During This Operation?

The reserved amount is returned to the available balance.

Changes:

- Pending Balance decreases.
- Available Balance increases.
- Wallet Hold status becomes CANCELLED.

No money has left the platform.

The beneficiary can request another payout later.

---

# Example

Before Cancellation

| Balance   | Amount |
|-----------|-------:|
| Available |   $600 |
| Pending   |   $400 |

Webhook

```
Status = FAILED
```

After Cancellation

| Balance   | Amount |
|-----------|-------:|
| Available | $1,000 |
| Pending   |     $0 | 

The reserved money is completely restored.

---

# Why Doesn't Total Debited Increase?

The payout never completed.

Money never left the wallet.

Therefore:

```
Total Debited

No Change
```

Only successful payouts increase the total debited amount.

---

# Why Isn't Another Wallet Transaction Created?

A payout transaction already exists.

Creating another transaction would duplicate the payout history.

Instead, only the wallet balances and wallet hold status are updated.

---

# Real-World Example (NutraHive)

Nutritionist Wallet

| Balance    |   Amount |
|------------|---------:|
| Available  |     $700 |
| Pending    |     $500 |

Razorpay

```
Status = FAILED
```

After cancellation

| Balance   | Amount |
|-----------|-------:|
| Available | $1,200 |
| Pending   |     $0 |

The nutritionist can immediately request another payout.

---

# Summary

`cancelWalletHold()` restores reserved funds back to the beneficiary's available balance when a payout fails after it has been created. It cancels the wallet hold without permanently deducting any money, ensuring the beneficiary does not lose funds because of an unsuccessful payout.