import urllib.request, json
req = urllib.request.Request("https://suhanova-ai.sameer974021.workers.dev/api/groq",
    headers={"X-App-Key": "suhanova-app-2025-nova", "Content-Type": "application/json"},
    data=json.dumps({"messages": [{"role": "user", "content": "Say hello!"}], "max_tokens": 50}).encode('utf-8'))
try:
    with urllib.request.urlopen(req) as response:
        print("Status:", response.status)
        print(response.read().decode())
except urllib.error.HTTPError as e:
    print("Status:", e.code)
    print(e.read().decode())
