# NepTools Emergency Contacts Cloudflare Edge API

Production-ready, zero-maintenance serverless API deployed to Cloudflare's global edge network.

## Features
- 100% Free (100,000 requests/day, zero cold starts, never pauses).
- Serves 61 verified emergency contacts for Nepal.
- Built-in conditional GET with ETag support for bandwidth savings.
- Query parameter filtering by province (`?province=Bagmati`) and category (`?category=medical`).
- Health check route: `/health`.

## Data sources and drift control

`src/emergency_contacts.json` is the canonical dataset. Two other copies exist and MUST be
updated together whenever contacts change, or the API will serve stale metadata:

| Location | Role |
| --- | --- |
| `src/emergency_contacts.json` | Canonical dataset, imported by `src/index.js`. |
| `standalone-worker.js` | Single-file deployable that embeds a copy of the dataset. Regenerate both the `DATA` block and the `totalContacts` count from the canonical JSON. |
| `../data/emergency_contacts.json` | Reference copy for humans and tooling. Not shipped in the APK. |
| `../app/.../EmergencyRepo.kt` | The compiled-in baseline the app treats as authoritative in an emergency. |

The app merges remote data instead of replacing it: a remote entry can add contacts, but it can
never overwrite or remove a compiled-in hotline. Keep `totalContacts` in sync with the number of
entries - `ETag` is derived from it.

## Deployment Options

### Option 1: One-Click Web Dashboard (Easiest - 30 seconds)
1. Log in to [Cloudflare Dashboard](https://dash.cloudflare.com/).
2. Navigate to **Compute (Workers) > Workers & Pages > Create application > Create Worker**.
3. Name it: `neptools-emergency-api` and click **Deploy**.
4. Click **Edit Code**, paste the contents of `src/index.js` (and upload `src/emergency_contacts.json` or inline it), then click **Save and Deploy**.

### Option 2: Command Line (Wrangler CLI)
Run inside this directory:
```bash
cd worker
npx wrangler login
npx wrangler deploy
```

Once deployed, your live URL will be:
`https://neptools-emergency-api.<your-subdomain>.workers.dev`
