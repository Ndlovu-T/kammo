# Task 5 — Automated Evidence Trail for Disputes

## Problem

The site's "Why Kammo" section promises: *"Every message, payment action, and
delivery update is recorded and available for any dispute resolution."* Today:

- `backend/src/main/java/com/kammo/kammobackend/message/DealMessage.java`
  stores **user-typed chat messages only** — not system events.
- `PaymentRecord` (`backend/src/main/java/com/kammo/kammobackend/payment/PaymentRecord.java`)
  logs processor operations but isn't surfaced as part of a unified,
  human-readable dispute timeline.
- There is no `AuditLog`/`EventHistory` table capturing status transitions,
  delivery/courier updates, or who-did-what-when in a single queryable place.

When a dispute (`DealStatus.DISPUTED`) actually happens, there is currently no
single source of truth an admin or arbitrator could pull up to see the full
history of a deal.

## Relevant existing code

- `backend/src/main/java/com/kammo/kammobackend/deal/DealService.java` — all status transitions happen here; this is every point that needs an audit write.
- `backend/src/main/java/com/kammo/kammobackend/message/DealMessage.java` — existing chat message model; the new audit entity should be distinct from this but queryable alongside it for a unified timeline.
- `backend/src/main/java/com/kammo/kammobackend/payment/PaymentRecord.java` — already a decent append-only log for payment events; the audit trail can treat this as one of its source tables rather than duplicating it.
- `backend/src/main/java/com/kammo/kammobackend/delivery/` — check for `TrackingEvent`/`TrackingResult` classes mentioned in the prior audit; confirm whether courier status updates are persisted anywhere today or only held transiently.
- `backend/src/main/java/com/kammo/kammobackend/admin/` — likely where an admin-facing dispute view should be exposed.

## Required outcome

1. A `DealAuditEvent` entity: `dealId`, `eventType` (enum: STATUS_CHANGED,
   PAYMENT_ACTION, DELIVERY_UPDATE, MESSAGE_SENT, DISPUTE_OPENED,
   DISPUTE_RESOLVED), `actorUserId` (nullable for system-generated events),
   `description` (human-readable), `metadata` (JSON column for structured
   detail — old/new status, payment reference, tracking number, etc.),
   `createdAt`. Append-only — no updates or deletes.
2. Write an audit event at every point `DealService` changes `DealStatus`,
   every successful/failed `PaymentRecord` write in `PaymentService`, every
   courier tracking update ingested in `backend/src/main/java/com/kammo/kammobackend/delivery/`,
   and every `DealMessage` sent. Easiest implementation: a single
   `AuditEventListener` subscribed to the same `ApplicationEventPublisher`
   events introduced in Task 4 (`DealStatusChangedEvent`) plus direct calls
   from `PaymentService`/delivery code for things that don't already have an
   event.
3. Add an admin endpoint `GET /api/admin/deals/{dealId}/audit-trail` returning
   the full chronological event list for a deal — this is what compliance or
   an arbitrator would pull up during a dispute. Check
   `backend/src/main/java/com/kammo/kammobackend/admin/` for existing
   admin-auth patterns to reuse.
4. Ensure the audit trail captures enough detail to answer "did the seller
   get notified," "when exactly did the buyer's inspection window start/end,"
   and "what was the exact payment provider response" — cross-reference with
   Task 3/4's notification dispatch so notification attempts are also logged
   here (or are queryable from the same admin view even if stored in the
   Worker's own logs).

## Explicitly out of scope

- Don't build a generic blockchain-style immutable ledger — a regular
  append-only Postgres table with no update/delete permission at the
  application layer is sufficient.
- Don't expose this audit trail to regular buyer/seller users in this task —
  scope it to admin/dispute-resolution access only unless later told
  otherwise.

## Acceptance check

Run a full deal lifecycle in an integration test (create → accept → pay →
ship → deliver → confirm) and assert the audit trail query returns one
chronologically-ordered entry per meaningful action, with no gaps for any
`DealStatus` transition that occurred.
