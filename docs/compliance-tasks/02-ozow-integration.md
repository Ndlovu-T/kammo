# Task 2 — Ozow Payment Provider Integration

## Problem

The marketing site (kammo.co.za, "Payment Secured" step) and the business
description both say buyers pay "via a secure payment link... via Paystack or
Ozow." Today only Paystack is implemented
(`backend/src/main/java/com/kammo/kammobackend/payment/PaystackPaymentProvider.java`).
There is no `OzowPaymentProvider`, no Ozow credentials/config, and no way for
a buyer to choose Ozow at checkout. This is independent of the other 6 tasks
and can be done in parallel.

## Relevant existing code

- `backend/src/main/java/com/kammo/kammobackend/payment/PaymentProvider.java` — the interface to implement: `charge(Deal, AppUser)`, `verifyCharge(String providerReference)`, `payout(Deal)`, `refund(Deal)`.
- `backend/src/main/java/com/kammo/kammobackend/payment/PaystackPaymentProvider.java` — reference implementation. Uses `@ConditionalOnProperty(name = "app.payment.provider", havingValue = "paystack")` to activate, injects `RestClient` configured with base URL + auth header from `application.properties` (`app.paystack.secret-key`, `app.paystack.base-url`).
- `backend/src/main/java/com/kammo/kammobackend/payment/MockPaymentProvider.java` — dev/test stub, check its `@ConditionalOnProperty` pattern too.
- `backend/src/main/java/com/kammo/kammobackend/payment/PaymentService.java` — caller of the provider; currently assumes a single active `PaymentProvider` bean (Spring picks whichever `@ConditionalOnProperty` matches `app.payment.provider`).
- `backend/src/main/java/com/kammo/kammobackend/deal/DealPricing.java` — `totalToPay(deal)` for charge amount.

## Required outcome

1. `OzowPaymentProvider implements PaymentProvider`, gated by
   `@ConditionalOnProperty(name = "app.payment.provider", havingValue = "ozow")`,
   following Ozow's API:
   - `charge`: build an Ozow "Create Payment Request" call (site code, private
     key, hash check per Ozow's hashing spec — SHA512 of concatenated fields +
     private key, lowercase hex), return PENDING with the hosted payment URL.
   - `verifyCharge`: call Ozow's transaction status / "GetTransactionByReference"
     endpoint, map `Complete`/`Cancelled`/`Error`/`Pending` to
     `PaymentStatus.SUCCEEDED/FAILED/PENDING`.
   - `payout`: Ozow doesn't natively do arbitrary payouts the way Paystack
     transfers do for South African EFT in the same way — confirm via Ozow's
     docs whether they expose a payout/refund-to-bank API for this use case.
     If Ozow has no payout API, this method should explicitly throw/return
     FAILED with a clear message rather than silently no-op, and payouts for
     Ozow-funded deals should fall back to Kammo's Paystack payout rail (since
     Kammo still needs *a* processor to push money to the seller — clarify
     with the team whether cross-provider payout is acceptable, or whether
     Ozow-paid deals require the seller to also be Paystack-payable).
   - `refund`: Ozow's refund API if available; otherwise return a clear
     FAILED result indicating manual refund is required.
2. New config properties (mirror the Paystack pattern): `app.ozow.site-code`,
   `app.ozow.private-key`, `app.ozow.api-key`, `app.ozow.base-url`, plus a
   `app.ozow.is-test` flag for sandbox mode.
3. A way for the buyer to pick a provider per-deal. Today `app.payment.provider`
   is a single global Spring property, which means only one provider can be
   active at a time. This needs to become a **per-charge choice**: either (a)
   inject both providers as named beans and have `PaymentService` select by a
   `paymentMethod` field on the charge request, or (b) add a `paymentMethod`
   enum (`PAYSTACK`, `OZOW`) to `Deal`/the charge request DTO and resolve the
   right `PaymentProvider` bean via a `Map<String, PaymentProvider>` lookup in
   `PaymentService`. Don't keep the current "only one provider can ever be
   active" model — that's incompatible with offering buyers a choice.
4. Add an Ozow webhook endpoint (Ozow pushes payment notifications) — check
   existing controllers in `backend/src/main/java/com/kammo/kammobackend/payment/`
   for whether a Paystack webhook controller already exists to mirror its
   pattern (verify webhook signature before trusting it).

## Explicitly out of scope

- Don't change the Paystack flow.
- Don't build a generic "payment provider plugin" abstraction beyond what's
  needed for these two providers — no speculative third provider support.

## Acceptance check

Integration test (with Ozow sandbox credentials or a mocked `RestClient`)
covering: charge initializes and returns a redirect URL, verify maps Ozow's
`Complete` status to SUCCEEDED, and a buyer can complete a deal end-to-end
choosing Ozow instead of Paystack.
