# Settlement Module

## 1. Overview

The Settlement module is responsible for distributing successfully collected customer payments to the appropriate beneficiary within the StreamHub platform.

A payment only confirms that the platform has received money from the customer. It does not determine how much belongs to the platform or how much belongs to the beneficiary.

The Settlement module applies the platform's business rules to calculate earnings and credits the beneficiary's wallet with the correct amount.

Unlike the Payment module, which focuses on collecting money, the Settlement module focuses on allocating money.

The settlement process begins only after a payment has been successfully completed and verified.

Once the settlement is completed, the beneficiary's earnings become available in their wallet and can later be withdrawn through the Payout module.

# 2. Business Requirement

StreamHub is a marketplace where customers book appointments with nutritionists.

When a customer completes a payment, the entire payment amount is first collected by the platform.

Example

Appointment Amount : $120

Platform Commission : $20

Nutritionist Earnings : $100

The payment itself does not transfer money to the nutritionist.

Instead, the platform must determine:

- How much belongs to the platform.
- How much belongs to the nutritionist.
- Which wallet should receive the earnings.

The Settlement module performs this calculation and credits the beneficiary's wallet.

Business Flow

Customer Payment
│
▼
Payment Completed
│
▼
Settlement Created
│
▼
Calculate Earnings
│
▼
Credit Wallet
│
▼
Settlement Completed

# 3. Why Settlement Module?

A successful payment does not automatically mean that the beneficiary should immediately receive the entire amount.

The platform may apply business rules such as:

- Platform commission
- Promotional discounts
- Taxes
- Revenue sharing
- Future split payments

If these calculations were placed directly inside the Payment module, the Payment module would become tightly coupled to business-specific logic.

Instead, the Settlement module provides a dedicated layer responsible for allocating funds.

Benefits

- Separation of concerns
- Cleaner architecture
- Easier maintenance
- Independent business logic
- Future extensibility
- Better financial traceability

# 4. Payment vs Settlement

| Payment                      | Settlement                      |
|------------------------------|---------------------------------|
| Collects money from customer | Allocates money to beneficiary  |
| Uses Payment Gateway         | Uses internal business rules    |
| Creates Payment              | Creates Settlement              |
| Confirms payment success     | Calculates beneficiary earnings |
| No wallet interaction        | Credits beneficiary wallet      |
| Handles payment lifecycle    | Handles earnings lifecycle      |

# 5. Where Settlement Fits

The Settlement module sits between the Payment module and the Wallet module.

The Payment module is responsible for collecting money from customers.

The Wallet module is responsible for maintaining the beneficiary's earnings.

The Settlement module acts as the bridge between them by determining how much money should be credited to the beneficiary after applying the platform's business rules.

Complete Money Flow

Customer
│
▼
Book Appointment
│
▼
Payment
│
▼
Payment Completed
│
▼
Settlement
│
▼
Calculate Earnings
│
▼
Credit Wallet
│
▼
Beneficiary Requests Payout
│
▼
Payout
│
▼
Bank / UPI

Without the Settlement module, there would be no dedicated component responsible for converting a successful customer payment into beneficiary earnings.

# 6. Settlement Lifecycle

Every settlement progresses through a well-defined lifecycle.

The lifecycle begins after a payment has been successfully completed and ends when the beneficiary's wallet has been credited.

Settlement Lifecycle

CREATED
│
▼
PROCESSING
│
▼
COMPLETED

If an error occurs during processing, the settlement may transition to:

CREATED
│
▼
PROCESSING
│
▼
FAILED

Each status represents a specific stage of the settlement process.

| Status     | Description                                          |
|------------|------------------------------------------------------|
| CREATED    | Settlement record has been created.                  |
| PROCESSING | Settlement calculation is in progress.               |
| COMPLETED  | Beneficiary wallet has been successfully credited.   |
| FAILED     | Settlement could not be completed due to an error.   |

Once a settlement reaches the COMPLETED status, it should never be modified.


# 7. Complete Settlement Flow

The Settlement module is executed only after a payment has been successfully verified.

Settlement Flow

Payment Completed
│
▼
Receive Payment
│
▼
Validate Payment Status
│
▼
Find Beneficiary
│
▼
Find Wallet
│
▼
Calculate Gross Amount
│
▼
Calculate Platform Commission
│
▼
Calculate Net Earnings
│
▼
Create Settlement
│
▼
Credit Wallet
│
▼
Update Settlement Status
│
▼
Settlement Completed

This process ensures that the beneficiary receives only the amount they have earned after all business calculations have been applied.

# 8. Settlement Calculations

The Settlement module is responsible for determining how much money should be credited to the beneficiary.

Example

Appointment Amount : $120

Platform Commission : $20

Nutritionist Earnings : $100

Calculation

Gross Amount

$120

↓

Platform Commission

$20

↓

Net Settlement

$100

Only the Net Settlement amount is credited to the beneficiary's wallet.

The Gross Amount and Commission Amount are still stored as part of the settlement for auditing and reporting purposes.

The settlement record therefore maintains three important values:

- Gross Amount
- Commission Amount
- Net Amount

This provides complete financial transparency and allows future reporting without recalculating historical settlements.

# 9. Settlement Database

The Settlement module stores every completed settlement in the `settlements` table.

Important Columns

- settlement_reference
- payment_id
- beneficiary_id
- wallet_id
- gross_amount
- commission_amount
- net_amount
- currency
- status
- metadata
- created_at
- updated_at

Each settlement belongs to exactly one payment and one beneficiary wallet.

A payment may generate one settlement, while a beneficiary can have many settlements over time.

The settlement record serves as the permanent financial record of how a customer payment was allocated.

# 10. Settlement Statuses

Each settlement moves through a predefined set of statuses.

| Status     | Description                                |
|------------|--------------------------------------------|
| CREATED    | Settlement record created.                 |
| PROCESSING | Business calculations are being performed. |
| COMPLETED  | Wallet successfully credited.              |
| FAILED     | Settlement processing failed.              |

Status Flow

CREATED
│
▼
PROCESSING
│
├──────────────┐
▼              ▼
COMPLETED          FAILED

Only successful settlements credit the beneficiary's wallet.

Failed settlements never modify wallet balances.

# 10. Settlement Statuses

Each settlement moves through a predefined set of statuses.

| Status     | Description                                |
|------------|--------------------------------------------|
| CREATED    | Settlement record created.                 |
| PROCESSING | Business calculations are being performed. |
| COMPLETED  | Wallet successfully credited.              |
| FAILED     | Settlement processing failed.              |

Status Flow

CREATED
│
▼
PROCESSING
│
├──────────────┐
▼              ▼
COMPLETED          FAILED

Only successful settlements credit the beneficiary's wallet.

Failed settlements never modify wallet balances.


# 11. Wallet Integration

The Settlement module is directly integrated with the Wallet module.

After calculating the beneficiary's earnings, the Settlement module credits the beneficiary's wallet.

Flow

Payment Completed
│
▼
Settlement
│
▼
Calculate Net Amount
│
▼
Find Wallet
│
▼
Credit Wallet
│
▼
Create Wallet Transaction
│
▼
Settlement Completed

The wallet is updated only after the settlement calculations have completed successfully.

This guarantees that beneficiaries never receive incorrect earnings.


# 12. Payment Integration

The Settlement module depends on the Payment module.

A settlement can only be created for a successfully completed payment.

Relationship

Payment

↓

Settlement

If the payment fails, expires, or is cancelled, no settlement is created.

Only payments with a successful final status are eligible for settlement.

This prevents invalid or incomplete customer payments from being credited to beneficiary wallets.

# 13. Future Commission Support

The current implementation calculates a single platform commission.

Future versions may support additional business rules such as:

- Category-specific commission
- Nutritionist-specific commission
- Promotional discounts
- Referral incentives
- Tax deductions
- Platform service charges

The Settlement module provides a dedicated place to implement these rules without affecting the Payment or Wallet modules.

# 14. Future Split Settlement

A single customer payment may need to be shared between multiple beneficiaries.

Example

Customer Payment

$200

↓

Nutritionist A

$120

↓

Nutritionist B

$60

↓

Platform

$20

The Settlement module can be extended to support multiple settlement records for a single payment while keeping each beneficiary's earnings independent.

# 15. Error Handling

The Settlement module validates all required information before crediting the wallet.

Possible errors include:

- Payment not found
- Payment not completed
- Beneficiary not found
- Wallet not found
- Invalid settlement amount
- Duplicate settlement request

If an error occurs before the wallet is credited, the settlement is marked as FAILED and no wallet balance is modified.

This guarantees financial consistency.


# 16. Idempotency

Settlement creation must be idempotent.

If the same payment is processed multiple times, only one settlement should be created.

Example

Payment Completed

↓

Settlement Created

↓

Retry

↓

Settlement Already Exists

↓

Ignore

This prevents duplicate wallet credits and ensures that each payment produces at most one settlement.

# 17. Event Tracking (Future)

Future versions may introduce a dedicated settlement event table.

Example

SETTLEMENT_CREATED

↓

SETTLEMENT_PROCESSING

↓

SETTLEMENT_COMPLETED

Each status transition would create a new immutable event.

This would provide:

- Complete audit history
- Easier debugging
- Financial reconciliation
- Operational monitoring

The initial implementation stores only the settlement status within the settlement record.

# 18. Architecture

Client

↓

Payment Module

↓

Settlement Controller

↓

Settlement Service

↓

Wallet Service

↓

Settlement Repository

↓

Database

The Settlement module coordinates business calculations and wallet updates while remaining independent of payment gateway implementations.


# 19. Folder Structure

settlement

├── controller

├── dto

│   ├── request

│   └── response

├── entity

├── enums

├── mapper

├── repository

├── service

└── util

The package structure follows the same layered architecture used throughout the StreamHub platform.


# 20. Future Enhancements

Future improvements planned for the Settlement module include:

- Settlement event history
- Delayed settlements
- Scheduled settlement jobs
- Partial settlements
- Split settlements
- Commission rule engine
- Settlement reconciliation
- Settlement reports
- Settlement APIs
- Settlement dashboard
- Multi-currency support
- Multi-gateway settlement support


Payment (1)
│
│
▼
Settlement (1)
│
├──────────────► Beneficiary (1)
│
├──────────────► Wallet (1)
│
▼
Settlement Events (*)



