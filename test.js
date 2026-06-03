const fetch = require('node-fetch');

async function test() {
    try {
        const res = await fetch("https://suhanova-ai.sameer974021.workers.dev/api/groq", {
            method: "POST",
            headers: {
                "X-App-Key": "suhanova-app-2025-nova",
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                messages: [{ role: "user", content: "Say hello!" }],
                max_tokens: 50
            })
        });
        
        const text = await res.text();
        console.log("Status:", res.status);
        console.log("Response:", text);
    } catch (e) {
        console.error(e);
    }
}
test();
