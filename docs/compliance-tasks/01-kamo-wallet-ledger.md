# Task 1 — Kamo Wallet / Internal Credit Ledger

## Problem

The business model (and the public site) describes a "Kamo Wallet" — internal
credits that represent escrowed funds — distinct from the real money sitting in
Kammo's Paystack/Ozow merchant balance. Today there is **no wallet entity at
all**. The only money-tracking table is `PaymentRecord`
(`backend/src/main/java/com/kammo/kammobackend/payment/PaymentRecord.java`),
which logs individual CHARGE/PAYOUT/REFUND operations against a `dealId` but
keeps no running balance per user, and has no concept of "funds held pending
buyer confirmation."

This matters for compliance: the CIO's framing is that Kammo is never holding
client money in a bank/trust — it's tracking **credits** that reference money
the processor is holding. Without a ledger, that distinction only exists in
the pitch deck, not in the system of record.

## Relevant existing code

- `backend/src/main/java/com/kammo/kammobackend/payment/PaymentRecord.java` — append-only log of processor operations.
- `backend/src/main/java/com/kammo/kammobackend/payment/PaymentService.java` — writes PaymentRecord rows on charge/verify/payout/refund.
- `backend/src/main/java/com/kammo/kammobackend/payment/PaystackPaymentProvider.java` — actual Paystack API calls (charge, verify, payout/transfer, refund).
- `backend/src/main/java/com/kammo/kammobackend/deal/Deal.java` + `DealStatus` — deal lifecycle.
- `backend/src/main/java/com/kammo/kammobackend/user/AppUser.java` — has seller bank details (`bankCode`, `bankAccountNumber`, `bankAccountName`).

## Required outcome

1. A `WalletAccount` (or `KamoWallet`) entity, one per `AppUser`, with a
   `balance` (BigDecimal) representing credits currently held on the user's
   behalf — this is an internal accounting number, **not** money Kammo
   physically holds in its own bank account.
2. A `WalletLedgerEntry` entity — append-only, double-entry style or at minimum
   signed amount + reason + reference to the triggering `Deal`/`PaymentRecord`
   — so every balance change is auditable. Suggested entry types: `HOLD`
   (buyer's payment confirmed, credits go into escrow), `RELEASE` (buyer
   confirmed receipt, credits become payable to seller), `PAYOUT` (credits
   converted into an actual Paystack/Ozow transfer to seller's bank),
   `REFUND`, `REVERSAL`.
3. Wire `PaymentService`/`DealService` so that:
   - On payment verified (Deal → PAYMENT_SECURED), create a `HOLD` entry
     crediting the wallet — this represents "funds secured, not yet payable."
   - On buyer confirming delivery (Deal → COMPLETED), create a `RELEASE` entry
     moving the held credit to the seller's available balance, then trigger
     the existing Paystack payout flow and record a `PAYOUT` entry that zeroes
     out the released credit once the transfer succeeds.
   - On refund/dispute resolution favoring buyer, create a `REFUND`/`REVERSAL`
     entry instead.
4. Balance must never go negative; enforce this with a DB constraint or
   service-level check before any debit.
5. Add a read endpoint (e.g. `GET /api/wallet/me`) returning current balance
   and recent ledger entries for the authenticated user.

## Explicitly out of scope

- Do not introduce any entity resembling a Kammo-owned bank account or trust
  account. The wallet is credits only; actual money movement still goes
  through Paystack/Ozow's transfer APIs, never through a Kammo bank account.
- Multi-currency support — ZAR only for now.

## Acceptance check

Write a unit/integration test that simulates: charge succeeds → wallet HOLD
entry created with correct amount → buyer confirms → RELEASE + PAYOUT entries
created → wallet balance returns to the pre-hold value for that deal → a
Paystack transfer was invoked exactly once with the correct seller destination.
