# @kammo/core

Shared **API client**, **utils**, and **deal business logic** for KAMMO frontends.

Used primarily by the **mobile app** (`mobile/`). Web can adopt the same package later.

## What's in here (not UI)

| Module | Purpose |
|--------|---------|
| `api.js` | `createKammoApi(baseUrl)` — all Spring Boot REST endpoints |
| `utils.js` | Phone normalize, ZAR format, status labels, create draft defaults |
| `dealActions.js` | Role detection, available deal actions, validation, progress |

## Mobile usage

```javascript
// mobile/src/api.js — only mobile-specific part is the base URL
import { createKammoApi } from "@kammo/core";
export const kammoApi = createKammoApi(apiBaseUrl);
```

```javascript
// Screens import via mobile shims (unchanged paths)
import { formatRand } from "../src/utils";
import { getDealActions } from "../src/dealActions";
import { useKammo } from "../src/KammoContext";
```

**UI stays in `mobile/app/`** — React Native components only.

## Backend

Single contract with `kammobackend` on port **8080**. No backend changes required.
