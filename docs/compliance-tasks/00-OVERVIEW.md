# Compliance Scope — Kammo Escrow Flow

## Context

Kammo is positioned as a **trusted middleman**, not a bank or trust. We must never
custody client funds directly. The compliant flow is:

1. Buyer and seller agree on a deal (in-app or via WhatsApp).
2. Buyer pays via **Paystack or Ozow** (third-party payment processors). Kammo
   verifies the payment using our own OTT/OTP confirmation step.
3. On confirmed payment, an automated trigger (Cloudflare Worker) fires an email
   (and eventually WhatsApp) telling the seller to start delivery.
4. Seller ships. Buyer has a 72-hour inspection window.
5. Buyer confirms receipt → funds are released from the **Kamo Wallet** (an
   internal credit ledger, not a real bank balance) → Kammo instructs Paystack/Ozow
   to pay out from Kammo's own merchant account to the seller's bank account.

The public site (kammo.co.za) advertises a 4-step deal flow and a 5-step payment
tracker (Initiated → Paid → Transit → Inspect → Released), WhatsApp notifications
on every status change, and an automated evidence trail for disputes.

## Audit findings (2026-06-25)

The core money-flow architecture is already compliant: `PaystackPaymentProvider`
transfers from Paystack's merchant `balance` to the seller's registered bank
account — Kammo's own bank account is never in the funds path. The following
**7 gaps** were identified between what's coded and what's required/advertised.
Each has its own task brief in this directory:

| # | File | Gap | Risk if left undone |
|---|------|-----|----------------------|
| 1 | `01-kamo-wallet-ledger.md` | No internal wallet/credit ledger entity | "Kamo Wallet" is just marketing copy; no internal accounting of held funds, no decoupling from processor balance |
| 2 | `02-ozow-integration.md` | Ozow not integrated, only Paystack | Site promises Ozow as a payment option; buyers who prefer Ozow cannot pay |
| 3 | `03-cloudflare-worker-notify.md` | No Cloudflare Worker triggering payment-confirmed email | Seller isn't told to ship; core flow described to CIO doesn't exist |
| 4 | `04-notification-service.md` | No email/WhatsApp notification service for status changes | Site claims "WhatsApp notifications... every status change" — false advertising risk |
| 5 | `05-audit-evidence-trail.md` | No persistent system event/audit log | Site claims "automated evidence trail for any dispute resolution" — no evidence exists for disputes |
| 6 | `06-deal-status-tracker-mapping.md` | Backend `DealStatus` (12 values) doesn't map to public 5-step tracker | Frontend/API consumers have no canonical way to render the advertised tracker |
| 7 | `07-otp-ott-verification.md` | No Kammo-owned OTP/OTT verification step; relies solely on Paystack's native 3DS | CIO's compliance story ("we use our OWN OTT/OTP system") isn't actually implemented — currently we just delegate to the processor |

## How to use these briefs

Each file is self-contained: it states the problem, the relevant existing code,
the required outcome, and suggested approach — written so an agent (or engineer)
with no prior context on this conversation can pick it up and execute
independently. Dependencies between tasks are called out explicitly where they
exist (e.g. task 4 and task 5 both want a system-event concept; task 3 depends on
task 1's wallet/ledger existing before "payment confirmed" has a clean trigger
point; task 6 depends on task 1's status model if wallet states are folded in).

Suggested execution order: **1 → 3 → 4 → 5 → 6 → 7 → 2** (2/Ozow is independent
and can run in parallel with any of the others).
