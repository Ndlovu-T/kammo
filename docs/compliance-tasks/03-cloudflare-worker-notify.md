# Task 3 — Cloudflare Worker: Payment-Confirmed Notification Trigger

## Problem

Per the CIO's description of the compliant flow: once a buyer's payment is
confirmed, "an automated trigger message via Cloudflare Worker with email"
tells the seller to start delivery. Today **no Cloudflare Worker exists
anywhere in this repository** — there's no `wrangler.toml`, no worker source
directory, nothing referencing "cloudflare" at all. This is a hard gap between
what's been described to the CIO/compliance and what's actually deployed.

## Why a Worker (and not just a backend service call)

The intent (confirm with the team if this assumption is wrong) is to decouple
the "tell the seller to ship" notification from the main Spring backend, so
that even if the backend's internal job processing has a hiccup, the
payment-confirmed → email trigger is a thin, fast, independently-deployed
edge function that's simple to audit and hard to silently break. The backend
becomes the source of truth (it still owns `DealStatus`); the Worker is purely
the delivery mechanism for the email notification once the backend tells it a
payment cleared.

## Required outcome

1. New directory at the repo root: `workers/payment-notify/` containing:
   - `wrangler.toml` configured for a Worker with a route/binding for an
     HTTP endpoint (e.g. `POST /notify/payment-confirmed`).
   - `src/index.ts` (or `.js`) — the Worker handler.
2. The Worker exposes one endpoint that the backend calls **after** it
   transitions a `Deal` to `PAYMENT_SECURED` (see
   `backend/src/main/java/com/kammo/kammobackend/deal/DealService.java` for
   where status transitions happen, and `DealCancelledEvent` for the existing
   `ApplicationEventPublisher` pattern to follow — add an analogous
   `DealPaymentConfirmedEvent`).
3. Request payload from backend → Worker: `dealCode`, `sellerEmail`,
   `sellerName`, `itemName`, `price`, `dealId`. Authenticate the call with a
   shared secret header (`X-Kammo-Worker-Secret`) — never expose this endpoint
   unauthenticated, since anyone could spam sellers with fake "you got paid"
   emails otherwise.
4. The Worker sends the email via a transactional email API (e.g. Resend,
   Postmark, or Cloudflare Email Workers / MailChannels — pick whichever the
   team already has an account for; check `backend/src/main/resources/application*.properties`
   for any existing email API keys before assuming none exist).
5. Email content: tell the seller payment is confirmed for deal `{dealCode}`
   and they must dispatch the item; include the 72-hour buyer inspection
   window context so the seller understands urgency.
6. The Worker call from the backend must be **fire-and-forget but logged** —
   if the Worker call fails, do not block or fail the payment confirmation
   transaction; log the failure so it can be retried/alerted on. Consider a
   retry queue (Cloudflare Queues) if reliability matters, but a simple
   logged failure is acceptable for v1.
7. Add a corresponding outbound HTTP client call in the backend
   (`backend/src/main/java/com/kammo/kammobackend/payment/` or a new
   `notification` package) — `app.worker.payment-notify-url` and
   `app.worker.shared-secret` config properties, mirroring the
   `@Value`-injected `RestClient` pattern already used in
   `PaystackPaymentProvider.java`.

## Explicitly out of scope

- WhatsApp sending itself — that's Task 4. This task is specifically the
  Cloudflare Worker + email-on-payment-confirmed piece the CIO called out by
  name.
- Don't build a generic "Worker for everything" — scope this Worker to the
  payment-confirmed notification only; other status-change notifications
  belong in Task 4's design (which may or may not also route through this
  Worker — flag that decision back to the team after this task is scoped).

## Acceptance check

Local `wrangler dev` test: POST to the Worker endpoint with a valid shared
secret and payload sends a real (or sandboxed) email to a test address.
Backend integration test confirms `DealPaymentConfirmedEvent` is published
exactly once when a Deal transitions into `PAYMENT_SECURED`, and that a
listener attempts the Worker HTTP call.
