// Suhanova AI Gateway — Cloudflare Worker
// Proxies Groq (Nova Chat) and Mistral (Quiz Generation) APIs
// API keys stored as Cloudflare Secrets, never exposed in APK

export default {
  async fetch(request, env, ctx) {
    // CORS headers for Android app
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, X-App-Key",
    };

    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    if (request.method !== "POST") {
      return new Response("Method not allowed", { status: 405, headers: corsHeaders });
    }

    // Simple app-level auth to prevent abuse
    const appKey = request.headers.get("X-App-Key");
    if (appKey !== env.APP_SECRET) {
      return new Response("Unauthorized", { status: 401, headers: corsHeaders });
    }

    const url = new URL(request.url);
    const body = await request.json();

    try {
      // ── GROQ endpoint: Nova Chat (llama3-70b-8192) ────────────────────────────
      if (url.pathname === "/api/groq") {
        const groqRes = await fetch("https://api.groq.com/openai/v1/chat/completions", {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${env.GROQ_API_KEY}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            model: "llama3-70b-8192",
            messages: body.messages,
            max_tokens: body.max_tokens ?? 512,
            temperature: body.temperature ?? 0.7,
            stream: false,
          }),
        });

        const data = await groqRes.json();
        return new Response(JSON.stringify(data), {
          headers: { ...corsHeaders, "Content-Type": "application/json" },
          status: groqRes.status,
        });
      }

      // ── MISTRAL endpoint: Quiz MCQ Generation ─────────────────────────────────
      if (url.pathname === "/api/mistral") {
        const mistralRes = await fetch("https://api.mistral.ai/v1/chat/completions", {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${env.MISTRAL_API_KEY}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            model: "mistral-small-latest",
            messages: body.messages,
            max_tokens: body.max_tokens ?? 1024,
            temperature: body.temperature ?? 0.3,
          }),
        });

        const data = await mistralRes.json();
        return new Response(JSON.stringify(data), {
          headers: { ...corsHeaders, "Content-Type": "application/json" },
          status: mistralRes.status,
        });
      }

      // ── Health check ──────────────────────────────────────────────────────────
      if (url.pathname === "/health") {
        return new Response(JSON.stringify({ status: "ok", service: "Suhanova AI Gateway" }), {
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }

      return new Response("Not found", { status: 404, headers: corsHeaders });

    } catch (err) {
      return new Response(JSON.stringify({ error: err.message }), {
        status: 500,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }
  },
};
