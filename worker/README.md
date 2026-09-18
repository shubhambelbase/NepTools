# NepTools Emergency Contacts Cloudflare Edge API

Production-ready, zero-maintenance serverless API deployed to Cloudflare's global edge network.

## Features
- 100% Free (100,000 requests/day, zero cold starts, never pauses).
- Serves 61 verified emergency contacts for Nepal.
- Built-in conditional GET with ETag support for bandwidth savings.
- Query parameter filtering by province (`?province=Bagmati`) and category (`?category=medical`).
- Health check route: `/health`.

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
