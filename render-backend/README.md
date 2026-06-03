# Suhanova Render Backend

Deploy this folder on Render as a Node web service.

## Render settings

- Root directory: `render-backend`
- Build command: `npm install`
- Start command: `npm start`
- Health check path: `/health`

## Environment variables

- `SESSION_SECRET`: any long random string
- `GROQ_API_KEY`: optional, required for `/api/groq`
- `MISTRAL_API_KEY`: optional, required for `/api/mistral`

## Main endpoints

- `GET /health`
- `POST /auth/signup`
- `POST /auth/login`
- `GET /profile`
- `POST /profile`
- `GET /skills`
- `POST /skills`
- `POST /jobs/match`
- `POST /api/groq`
- `POST /api/mistral`
