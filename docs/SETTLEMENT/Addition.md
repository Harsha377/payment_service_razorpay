In your schema, payment_id has a unique constraint, which means one payment can have only one settlement. In that case, @OneToOne between SettlementEntity and PaymentEntity is the correct mapping, as shown above.

However, if you later decide to support split settlements (one payment distributed across multiple beneficiaries), you'll need to:

Remove the unique constraint on payment_id.
Change the mapping to @ManyToOne.

For the current NutraHive implementation—one appointment payment → one beneficiary → one settlement—the @OneToOne mapping is the right choice.