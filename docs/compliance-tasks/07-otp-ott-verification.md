# Task 7 — Kammo-Owned OTP/OTT Payment Verification Step

## Problem

The CIO's compliance framing explicitly says: *"we use our OWN OTT/OTP system
for a safe payment to be made via paystack or Ozow."* This is an important
distinction for the "we're not a bank/trust, we're a verifying middleman"
story — it implies Kammo performs its own independent confirmation step
around the payment, rather than purely trusting the processor's callback.

Today, the only OTP-related code is a passive status string from Paystack's
async transfer flow:
`backend/src/main/java/com/kammo/kammobackend/payment/PaystackPaymentProvider.java:190`
(`"otp".equals(status)` inside `mapAsyncStatus`) — this is just Paystack's own
3D-Secure/OTP step on the *card* side, not anything Kammo owns or controls.
There is no Kammo-issued one-time token/passcode anywhere in the codebase.

## Clarify intent before building (flag to product/compliance, don't assume)

"OTT/OTP system" could mean different things — confirm which before
implementing:

- **(a)** A Kammo-generated OTP sent to the buyer (SMS/WhatsApp/email) that
  the buyer must enter in the Kammo app to *confirm they personally
  authorized this specific payment*, independent of Paystack/Ozow's own
  checkout auth. This adds a Kammo-controlled verification layer on top of
  the processor flow.
- **(b)** A one-time *token* (OTT) Kammo generates per checkout session to
  prevent replay/tampering of the charge request between app and backend
  (a security/integrity control, not a user-facing OTP).
- **(c)** Both — OTT to secure the charge initiation request, OTP to confirm
  buyer identity/intent before releasing funds at the end of the deal.

Given the CIO described it as part of "a safe payment to be made," **(a)** or
**(c)** seems most likely the intended compliance story — i.e. Kammo itself
verifies the human authorizing the payment, rather than relying solely on
Paystack/Ozow's verification. This task assumes (c); revise if product
disagrees.

## Relevant existing code

- `backend/src/main/java/com/kammo/kammobackend/payment/PaystackPaymentProvider.java` — `charge()` (line 52-75) is where a Kammo OTP step should be inserted before/alongside redirecting to Paystack's checkout.
- `backend/src/main/java/com/kammo/kammobackend/auth/` — check for any existing OTP infrastructure used during signup/login; if phone verification already uses OTP there, **reuse that mechanism** rather than building a second one.
- `backend/src/main/java/com/kammo/kammobackend/user/AppUser.java` — needs a verified phone/email to send the OTP to.

## Required outcome

1. Check `backend/src/main/java/com/kammo/kammobackend/auth/` first — if
   there's already an OTP-sending service (likely, since phone-based signup
   typically needs one), extend it rather than duplicating.
2. Add a `PaymentVerification` step: when a buyer initiates a charge
   (`PaymentService`/`PaystackPaymentProvider.charge`), generate a one-time
   code, send it via SMS/WhatsApp to the buyer's verified phone, and require
   it to be submitted back (`POST /api/deals/{dealId}/payment/verify-otp`)
   before the deal is allowed to proceed to the processor checkout redirect
   — or, if UX requires payment first then confirmation, require it before
   transitioning the deal to `PAYMENT_SECURED` even if Paystack/Ozow already
   confirmed the charge. Decide ordering with product; document the chosen
   order in code comments since it affects the fraud-prevention story.
3. Generate a one-time *token* (OTT) per checkout session — a signed,
   short-lived, single-use token included in the charge initiation request
   to Paystack/Ozow's reference field, so Kammo can detect tampering/replay
   if a charge reference is reused or forged.
4. Expire OTPs after a short window (e.g. 5 minutes) and rate-limit
   generation per deal/user to avoid abuse.
5. Persist OTP/OTT attempts (success/failure, timestamp) — this doubles as
   evidence for Task 5's audit trail.

## Explicitly out of scope

- Don't replace or duplicate Paystack/Ozow's own card-level 3DS/OTP — that
  stays as-is; this is an additional Kammo-side layer, not a replacement.
- Don't build a generic "verification framework" — scope to payment
  confirmation only, reusing existing auth OTP code where possible.

## Acceptance check

Integration test: initiating a charge sends an OTP to the buyer's phone (mock
SMS sender), wrong/expired OTP is rejected, correct OTP allows the deal to
proceed, and the OTT included in the Paystack/Ozow charge reference is
validated on `verifyCharge` to reject mismatched/forged references.
