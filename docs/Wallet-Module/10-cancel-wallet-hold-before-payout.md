# cancelWalletHoldBeforePayout()

## Overview

The `cancelWalletHoldBeforePayout()` operation releases reserved funds when the payout could not even be created.

This method is used when an error occurs before a payout exists.

Unlike `cancelWalletHold()`, there is no payout associated with the wallet hold.

---

# Why Do We Need This Method?

Sometimes the payout creation process fails before reaching the payment gateway.

Examples:

- Razorpay API unavailable
- Network timeout
- Internal server exception
- Database failure
- Authentication failure
- Invalid request

In these situations:

- Wallet Hold exists.
- Payout does NOT exist.

The reserved money must still be returned.

---

# When Is It Used?

This method is executed inside the payout creation process.

Example

```
Create Wallet Hold
        │
        ▼
Create Payout
        │
        ▼
Exception Occurred
        │
        ▼
cancelWalletHoldBeforePayout()
```

Since no payout exists, there is nothing to attach.

---

# What Happens During This Operation?

The operation simply restores the wallet.

Changes:

- Pending Balance decreases.
- Available Balance increases.
- Wallet Hold status becomes CANCELLED.

No payout is updated because no payout exists.

---

# Example

Wallet

| Balance   | Amount |
|-----------|-------:|
| Available |   $500 |
| Pending   |   $300 |

System attempts to create payout.

```
Gateway Timeout
```

After cancellation

| Balance   | Amount |
|-----------|-------:|
| Available |   $800 |
| Pending   |     $0 |

The beneficiary's money is fully restored.

---

# Difference Between cancelWalletHold() and cancelWalletHoldBeforePayout()

| cancelWalletHold()              | cancelWalletHoldBeforePayout()            |
|---------------------------------|-------------------------------------------|
| Payout exists                   | Payout does not exist                     |
| Triggered by webhook            | Triggered by exception                    |
| Uses payout relationship        | No payout relationship                    |
| Executed after gateway response | Executed before gateway request completes |

---

# Real-World Example (NutraHive)

Nutritionist requests a payout of **$500**.

Wallet Hold

```
Available = $700
Pending = $500
```

While creating the payout, Razorpay API becomes unavailable.

Since no payout was created, the platform immediately restores the wallet.

Final Wallet

```
Available = $1,200
Pending = $0
```

The nutritionist can retry the payout once the system is available.

---

# Summary

`cancelWalletHoldBeforePayout()` handles failures that occur before a payout is successfully created. It safely restores the reserved funds to the beneficiary's available balance without requiring a payout reference, ensuring that temporary system failures never lock a beneficiary's money.