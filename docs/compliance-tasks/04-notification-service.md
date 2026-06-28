# Task 4 — Status-Change Notification Service (Email / WhatsApp)

## Problem

The site's "Why Kammo" section explicitly promises: *"Every status change
triggers a WhatsApp message to both buyer and seller automatically in real
time."* Today there is **no notification infrastructure** beyond the single
Cloudflare Worker call scoped in Task 3 for payment-confirmed only. There's no
Spring Mail, no Twilio/WhatsApp Business API client, no notification service
class, and `DealService` publishes exactly one event
(`DealCancelledEvent`, line ~328) for inventory cleanup — nothing for
notifying users.

## Relevant existing code

- `backend/src/main/java/com/kammo/kammobackend/deal/DealService.java` — owns all `DealStatus` transitions; this is where every notification trigger point lives.
- `backend/src/main/java/com/kammo/kammobackend/deal/Deal.java` — `DealStatus` enum (CREATED, AWAITING_BUYER_PAYMENT, BUYER_ACCEPTED, SELLER_ACCEPTED, PAYMENT_SECURED, AWAITING_COLLECTION, IN_TRANSIT, DELIVERED, COMPLETED, DISPUTED, REFUNDED, CANCELLED).
- `backend/src/main/java/com/kammo/kammobackend/user/AppUser.java` — needs to be checked for whether phone numbers (for WhatsApp) and email are both present and verified for every user.
- `backend/pom.xml` — no `spring-boot-starter-mail`, no Twilio/WhatsApp SDK currently listed.
- Task 3's Cloudflare Worker (`workers/payment-notify/`) — decide whether WhatsApp/email for *all* status changes routes through that same Worker (extend it) or a separate backend-side notification service. Recommendation: extend the Worker into a general `workers/notify/` since it's already the established pattern for outbound comms, rather than building parallel infra in the Spring backend.

## Required outcome

1. Pick and integrate a WhatsApp Business API provider (e.g. Twilio, or
   Meta's Cloud API directly — check if the team already has a WhatsApp
   Business account before picking a vendor). Add the dependency/config.
2. Define a notification matrix: for each `DealStatus` transition, what
   message goes to buyer vs seller. At minimum cover the 5 public-facing
   tracker steps (see Task 6 for the canonical mapping): deal agreed, payment
   secured, item in transit, inspection window started, payout released.
3. Create a `NotificationService` (or extend the Task 3 Worker) that listens
   for deal status-change events and dispatches the right WhatsApp template +
   email to both parties. Use Spring's `ApplicationEventPublisher` pattern
   already established with `DealCancelledEvent` — add a generic
   `DealStatusChangedEvent(Long dealId, DealStatus from, DealStatus to)`
   published from `DealService` on every transition, and a single listener
   that fans out to WhatsApp/email rather than scattering notification calls
   through business logic.
4. WhatsApp messages must use pre-approved message templates (WhatsApp
   Business API requires template approval for business-initiated messages
   outside a 24h customer service window) — don't assume free-form text
   works in production.
5. Failures to send a notification must never roll back or block the
   underlying deal/payment transaction — notifications are best-effort,
   logged on failure, and ideally retryable.
6. Respect opt-outs / missing contact info gracefully (e.g. user has no
   verified WhatsApp number — fall back to email only, don't throw).

## Explicitly out of scope

- Don't build this as a generic multi-channel notification framework with
  pluggable channels beyond email + WhatsApp — two channels, that's it.
- In-app push notifications are not part of this task unless already
  half-built elsewhere — check `mobile/` for any existing push setup before
  assuming it's needed here.

## Acceptance check

Trigger each of the 5 canonical status transitions in an integration test and
assert the notification listener attempts the correct WhatsApp template +
email for both buyer and seller, with assertions on message content matching
the deal code and item name.
