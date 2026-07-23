# Beneficiary & Fund Account Module

Version: 1.0

---

# Table of Contents

1. Overview
2. Business Requirements
3. Why Beneficiary Module Exists
4. Pay-In vs Beneficiary Module
5. High Level Architecture
6. Package Structure
7. Design Principles
8. Database Design
9. Entity Relationships
10. Beneficiary Entity
11. Fund Account Entity
12. Why Gateway Response is Stored
13. Current Business Rules
14. Future Enhancements

---

# 1. Overview

The Beneficiary Module is responsible for onboarding payout recipients into the payment platform.

Unlike the Pay-In module, which collects money from customers, this module prepares a recipient to receive money from the platform.

The module performs the following responsibilities:

• Creates a Beneficiary within the platform.
• Creates a Contact in RazorpayX.
• Creates a Fund Account in RazorpayX.
• Stores gateway identifiers.
• Stores complete gateway responses.
• Prevents duplicate fund accounts.
• Maintains the default payout account.
• Serves as the prerequisite for Payout creation.

This module is part of the payout pipeline.

Customer
↓
Platform Wallet
↓
Beneficiary
↓
Fund Account
↓
Payout
↓
Bank / UPI

---

# 2. Business Requirements

NutraHive allows nutritionists to earn money from customer appointments.

Example

Customer books appointment

↓

Customer pays ₹1200

↓

Platform Commission ₹200

↓

Nutritionist Earnings ₹1000

The earnings remain inside the platform until a payout is initiated.

Before money can be transferred to a nutritionist, the platform must know where to send the money.

Possible destinations

• Bank Account
• UPI ID

A beneficiary represents that payout destination.

Without a beneficiary, payouts cannot be initiated.

---

# 3. Why Beneficiary Module Exists

Payment gateways never allow transferring money directly to an arbitrary bank account.

Instead, they require a registration process.

For RazorpayX the process is

Create Contact

↓

Create Fund Account

↓

Create Payout

Therefore every payout recipient must first be onboarded into Razorpay.

The Beneficiary module automates this onboarding process.

---

# 4. Pay-In vs Beneficiary Module

Many developers confuse these two modules because both interact with Razorpay.

However, they solve completely different business problems.

----------------------------------------------------------
Pay-In
----------------------------------------------------------

Purpose

Collect money from customers.

Direction

Customer
↓
Platform

Gateway

Razorpay Payment Gateway

Objects Created

Order
Payment
Webhook Events

Money Flow

Incoming Money

Example

Customer pays ₹1200.

----------------------------------------------------------
Beneficiary
----------------------------------------------------------

Purpose

Prepare recipients for receiving money.

Direction

Platform
↓
Nutritionist

Gateway

RazorpayX

Objects Created

Contact
Fund Account

Money Flow

Outgoing Money

Example

Platform sends ₹1000 to Nutritionist.

----------------------------------------------------------

Pay-In and Beneficiary are independent systems.

Pay-In manages incoming money.

Beneficiary prepares outgoing money.

The Payout module connects both systems.

---

# 5. High Level Architecture

                   REST API

                       │

                       ▼

             FundAccountController

                       │

                       ▼

             FundAccountService

                       │

        ┌──────────────┴──────────────┐

        ▼                             ▼

BeneficiaryRepository                Gateway Service

                                      │

                                      ▼

                            RazorpayX Client

                                      │

                                      ▼

                              Razorpay REST API

                                      │

                                      ▼

                                PostgreSQL

---

# 6. Package Structure

beneficiary/

    controller/

        FundAccountController

    dto/

        request/

        response/

    entity/

        BeneficiaryEntity

        FundAccountEntity

    enums/

        BeneficiaryStatus

        FundAccountStatus

        FundAccountType

        OwnerType

    repository/

        BeneficiaryRepository

        FundAccountRepository

    mapper/

        FundAccountMapper

    service/

        FundAccountService

        FundAccountServiceImpl

gateway/

    client/

    mapper/

    dto/

    service/

---

# 7. Design Principles

The module follows a layered architecture.

Controller

↓

Service

↓

Gateway Client

↓

Repository

Every layer has a single responsibility.

--------------------------------------------------

Controller

Responsible for

• Receiving requests
• Returning responses

Contains no business logic.

--------------------------------------------------

Service

Responsible for

• Validation
• Business rules
• Transactions
• Gateway orchestration

--------------------------------------------------

Gateway Client

Responsible for

• HTTP communication
• Serialization
• Error handling

Contains no business rules.

--------------------------------------------------

Repository

Responsible for

• Database persistence

---

# PART 2

# 8. Database Design

Two tables are maintained.

beneficiaries

Stores recipient information.

fund_accounts

Stores payout destinations.

Relationship

Beneficiary

↓

One

↓

Many

↓

Fund Accounts

A beneficiary may own multiple fund accounts.

Current implementation

Maximum

1 Bank Account

1 VPA

Future

Multiple Bank Accounts

Multiple VPAs

---

# 9. Beneficiary Entity

Purpose

Represents a payout recipient.

Fields

id

Database primary key.

beneficiary_reference

Platform generated ULID.

owner_reference

Reference of the actual business owner.

Example

Nutritionist ID

Owner ID

Merchant ID

owner_type

Represents the owner category.

Examples

USER

MERCHANT

PARTNER

EMPLOYEE

name

Beneficiary Name.

email

Email address.

contact_number

Mobile Number.

gateway_name

Payment Gateway.

Example

RAZORPAY

gateway_contact_id

Contact ID returned by Razorpay.

Example

cont_QAbCdEf123

status

ACTIVE

INACTIVE

gateway_response

Complete Razorpay Contact response.

metadata

Business specific custom information.

---

# 10. Fund Account Entity

Purpose

Represents an actual payout destination.

Fields

fund_account_reference

Platform generated ULID.

beneficiary

Foreign key to Beneficiary.

fund_account_type

BANK_ACCOUNT

VPA

account_holder_name

Used only for Bank Accounts.

account_number

Used only for Bank Accounts.

ifsc_code

Used only for Bank Accounts.

vpa

Used only for UPI.

gateway_fund_account_id

Returned by Razorpay.

Example

fa_QABC123XYZ

is_default

Default payout account.

Current Rule

Only one default account.

status

ACTIVE

INACTIVE

gateway_response

Complete Razorpay Fund Account response.

metadata

Application specific information.

---

# 11. Entity Relationship

Beneficiary

beneficiary_reference

↓

gateway_contact_id

↓

gateway_response

↓

FundAccount

↓

gateway_fund_account_id

↓

gateway_response

This separation ensures

Business Data

and

Gateway Data

remain independent.

---

# 12. Why Gateway Response is Stored

The application stores the complete gateway response.

Example

gateway_response

{

"id": "...",

"entity": "...",

"contact_id": "...",

...

}

Reasons

Audit

Complete history of gateway interaction.

Debugging

Exact gateway response available.

Future Compatibility

If Razorpay introduces new fields, they are automatically stored.

Support

Gateway issues can be investigated without reproducing requests.

The application uses only

gateway_contact_id

gateway_fund_account_id

for business logic.

Everything else remains inside gateway_response.

---

# 13. Current Business Rules

Rule 1

A Beneficiary is uniquely identified by

owner_reference

+

owner_type

Rule 2

Only one Bank Account per Beneficiary.

Rule 3

Only one VPA per Beneficiary.

Rule 4

Owner details cannot change.

Name

Email

Phone Number

must always match.

Rule 5

Gateway Contact is created only once.

Rule 6

Gateway Fund Account is created for every new payout destination.

Rule 7

First Fund Account becomes the default payout account.

---

# 14. Future Enhancements

The current implementation intentionally keeps the business rules simple.

Future improvements include

• Multiple Bank Accounts

• Multiple VPAs

• Change Default Fund Account API

• Disable Fund Account

• Delete Fund Account

• Synchronize Beneficiary with Razorpay

• Synchronize Fund Account with Razorpay

• Beneficiary Search APIs

• Beneficiary Update APIs

• Encryption of sensitive bank details

• Gateway Request Storage

• Beneficiary Audit History

The existing architecture already supports these enhancements without major database redesign.