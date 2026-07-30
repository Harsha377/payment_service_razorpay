# findWalletHoldByPayout()

## Overview

The `findWalletHoldByPayout()` operation retrieves the wallet hold associated with a payout.

After a payout has been created, every webhook event contains payout information.

To update the correct wallet, the platform first locates the wallet hold linked to that payout.

---

# Why Do We Need This Method?

Webhook events only identify the payout.

Example

```
Webhook

PAYOUT001
```

The webhook does not know:

- Wallet
- Beneficiary
- Reserved Amount

To obtain that information, the platform searches for the wallet hold.

Relationship

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

---

# When Is It Used?

This method is mainly used while processing payout webhooks.

Typical flow

```
Webhook Received
        │
        ▼
Find Payout
        │
        ▼
findWalletHoldByPayout()
        │
        ▼
Release Hold

or

Cancel Hold
```

---

# What Does This Method Do?

The method performs a lookup.

It searches the database for the wallet hold associated with the given payout.

If found:

- Returns the wallet hold.

Otherwise:

- Throws an exception indicating that no wallet hold exists.

---

# Example

Payout

```
PAYOUT001
```

Database

| Hold Reference | Payout      |
|----------------|-------------|
| HOLD001        | PAYOUT001   |

Result

```
Wallet Hold

HOLD001
```

The webhook can now update the correct wallet.

---

# Why Is This Lookup Important?

Without this relationship, the system would not know:

- Which wallet should be updated.
- Which pending balance should be released.
- Which beneficiary owns the payout.

The wallet hold acts as the bridge between the payout and the wallet.

---

# Real-World Example (NutraHive)

Webhook

```
Status = PROCESSED

Payout = PAYOUT001
```

Platform

```
Find Payout

↓

Find Wallet Hold

↓

Release Wallet Hold

↓

Update Wallet
```

Everything starts by locating the wallet hold.

---

# Summary

`findWalletHoldByPayout()` is a lookup operation that connects the payout lifecycle with the wallet lifecycle. It retrieves the wallet hold associated with a payout, enabling webhook processing to release or cancel reserved funds in the correct wallet.