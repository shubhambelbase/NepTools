import contactsData from "./emergency_contacts.json";

const ETAG = `"v${contactsData.version || 1}-${contactsData.totalContacts || 61}"`;

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    // Handle CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, HEAD, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, If-None-Match, If-Modified-Since",
          "Access-Control-Max-Age": "86400",
        },
      });
    }

    if (request.method !== "GET" && request.method !== "HEAD") {
      return new Response(JSON.stringify({ error: "Method not allowed" }), {
        status: 405,
        headers: {
          "Content-Type": "application/json",
          "Allow": "GET, HEAD, OPTIONS",
        },
      });
    }

    // Health check endpoint
    if (url.pathname === "/health") {
      return new Response(
        JSON.stringify({
          status: "healthy",
          service: "NepTools Emergency Contacts Edge API",
          provider: "Cloudflare Workers",
          version: contactsData.version,
          totalContacts: contactsData.totalContacts,
          timestamp: new Date().toISOString(),
        }),
        {
          status: 200,
          headers: {
            "Content-Type": "application/json",
            "Cache-Control": "no-cache",
          },
        }
      );
    }

    // Conditional GET: check ETag
    const clientEtag = request.headers.get("If-None-Match");
    if (clientEtag && clientEtag === ETAG) {
      return new Response(null, {
        status: 304,
        headers: {
          "ETag": ETAG,
          "Cache-Control": "public, max-age=300, s-maxage=3600",
          "Access-Control-Allow-Origin": "*",
        },
      });
    }

    // Query parameter filtering support (optional)
    const provinceFilter = url.searchParams.get("province");
    const categoryFilter = url.searchParams.get("category");

    let payload = contactsData;

    if (provinceFilter || categoryFilter) {
      const filtered = contactsData.contacts.filter((c) => {
        let match = true;
        if (provinceFilter && c.province !== "National" && c.province.toLowerCase() !== provinceFilter.toLowerCase()) {
          match = false;
        }
        if (categoryFilter && categoryFilter.toLowerCase() !== "all" && c.category.toLowerCase() !== categoryFilter.toLowerCase()) {
          match = false;
        }
        return match;
      });

      payload = {
        ...contactsData,
        totalContacts: filtered.length,
        contacts: filtered,
      };
    }

    const jsonBody = JSON.stringify(payload, null, 2);

    return new Response(jsonBody, {
      status: 200,
      headers: {
        "Content-Type": "application/json; charset=utf-8",
        "Cache-Control": "public, max-age=300, s-maxage=3600",
        "ETag": ETAG,
        "Access-Control-Allow-Origin": "*",
        "X-Powered-By": "NepTools Cloudflare Worker",
      },
    });
  },
};
