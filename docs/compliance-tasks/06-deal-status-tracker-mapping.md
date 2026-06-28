# Task 6 — Map Backend DealStatus to the Public 5-Step Payment Tracker

## Problem

The site's "5-Step Payment Tracker" shows exactly five states: **Initiated →
Paid → Transit → Inspect → Released**. The backend's actual
`DealStatus` enum
(`backend/src/main/java/com/kammo/kammobackend/deal/Deal.java`) has 12 values:
`CREATED, AWAITING_BUYER_PAYMENT, BUYER_ACCEPTED, SELLER_ACCEPTED,
PAYMENT_SECURED, AWAITING_COLLECTION, IN_TRANSIT, DELIVERED, COMPLETED,
DISPUTED, REFUNDED, CANCELLED`.

There is currently no documented or coded mapping between the two. Any
frontend (web marketing site, mobile app) that wants to render the 5-step
tracker has to invent its own mapping logic, which risks drifting out of sync
with backend reality, and risks misrepresenting deal state to users/compliance
if done inconsistently across clients.

## Relevant existing code

- `backend/src/main/java/com/kammo/kammobackend/deal/Deal.java` — `DealStatus` enum definition.
- `backend/src/main/java/com/kammo/kammobackend/deal/DealService.java` — all transition logic; the source of truth for valid transitions.
- Mobile app at `mobile/src/` and `mobile/app/` — check for any existing client-side status-display logic that already (informally) does this mapping, to avoid contradicting it.
- Public site is presumably a separate static/marketing project (not in this repo, per the kammo.co.za reference) — out of scope to edit here, but the mapping this task produces should be the canonical reference for whoever maintains that site too.

## Required outcome

1. Define and document the canonical mapping, e.g.:

   | Public tracker step | Backend `DealStatus` value(s) |
   |---|---|
   | Initiated | CREATED, AWAITING_BUYER_PAYMENT, BUYER_ACCEPTED, SELLER_ACCEPTED |
   | Paid | PAYMENT_SECURED |
   | Transit | AWAITING_COLLECTION, IN_TRANSIT |
   | Inspect | DELIVERED |
   | Released | COMPLETED |

   (DISPUTED, REFUNDED, CANCELLED are terminal/exception states outside the
   happy-path tracker — decide with the team how these should render: e.g.
   DISPUTED freezes the tracker at whatever step it was on plus a dispute
   banner; REFUNDED/CANCELLED show a distinct "deal ended" state rather than
   forcing them into one of the 5 steps.)

2. Implement this mapping as a single method, e.g.
   `DealStatus.toTrackerStep()` or a small `TrackerStep` enum +
   `TrackerStepMapper` class in the `deal` package, so it's computed once in
   the backend and exposed via the API rather than re-implemented per client.
3. Expose the tracker step (plus raw `DealStatus` for clients that want more
   granularity) in whatever DTO the deal-detail endpoint already returns —
   find the existing deal response DTO in
   `backend/src/main/java/com/kammo/kammobackend/deal/` and add a field
   rather than creating a parallel endpoint.
4. Update the mobile app (`mobile/src/`) to consume the new tracker-step field
   if it currently does its own ad-hoc mapping.

## Explicitly out of scope

- Don't change any `DealStatus` values or transition logic — this task is
  purely an additive read-side mapping, not a state-machine redesign.
- Don't touch the marketing site itself (not in this repo).

## Acceptance check

Unit test asserting every `DealStatus` value maps to exactly one defined
tracker step or exception category (no `DealStatus` falls through to a
default/null), and an API integration test confirming the deal-detail
response includes the correct tracker step for a deal at each status.
