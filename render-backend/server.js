import crypto from "node:crypto";
import cors from "cors";
import express from "express";

const PORT = process.env.PORT || 10000;
const SESSION_SECRET = process.env.SESSION_SECRET || "dev-session-secret-change-me";
const GROQ_API_KEY = process.env.GROQ_API_KEY;
const MISTRAL_API_KEY = process.env.MISTRAL_API_KEY;
const GROQ_DEFAULT_MODEL = "llama-3.3-70b-versatile";
const MISTRAL_DEFAULT_MODEL = "mistral-small-latest";

const app = express();

app.use(cors());
app.use(express.json({ limit: "2mb" }));

const usersByEmail = new Map();
const sessions = new Map();

function hashPassword(password, salt = crypto.randomBytes(16).toString("hex")) {
  const hash = crypto.pbkdf2Sync(password, salt, 100_000, 64, "sha512").toString("hex");
  return `${salt}:${hash}`;
}

function verifyPassword(password, storedHash) {
  const [salt] = storedHash.split(":");
  return hashPassword(password, salt) === storedHash;
}

function issueToken(email) {
  const nonce = crypto.randomBytes(24).toString("hex");
  const signature = crypto
    .createHmac("sha256", SESSION_SECRET)
    .update(`${email}:${nonce}`)
    .digest("hex");
  const token = Buffer.from(`${email}:${nonce}:${signature}`).toString("base64url");
  sessions.set(token, email);
  return token;
}

function publicUser(user) {
  return {
    name: user.name,
    email: user.email,
  };
}

function requireAuth(req, res, next) {
  const auth = req.get("Authorization") || "";
  const token = auth.startsWith("Bearer ") ? auth.slice("Bearer ".length) : null;
  const email = token ? sessions.get(token) : null;
  const user = email ? usersByEmail.get(email) : null;

  if (!user) {
    return res.status(401).json({ error: "Unauthorized" });
  }

  req.user = user;
  next();
}

function requireFields(res, body, fields) {
  const missing = fields.filter((field) => !body?.[field]);
  if (missing.length > 0) {
    res.status(400).json({ error: `Missing required field(s): ${missing.join(", ")}` });
    return false;
  }
  return true;
}

app.get("/health", (_req, res) => {
  res.json({ ok: true, service: "Suhanova backend" });
});

app.post("/suhanova/setup", (req, res) => {
  const setup = {
    name: req.body.name || "Suhana",
    board: req.body.board || "",
    studentClass: req.body.studentClass || req.body.student_class || "",
    targetExam: req.body.targetExam || req.body.target_exam || "",
    examDate: req.body.examDate || req.body.exam_date || "",
    goal: req.body.goal || "",
    level: req.body.level || "",
    weakAreas: req.body.weakAreas || req.body.weak_areas || "",
  };

  res.json({
    ok: true,
    service: "Suhanova backend",
    setup,
  });
});

app.post("/suhanova/study-plan", async (req, res) => {
  const messages = normalizeMessages([
    {
      role: "user",
      content: `Create a study plan for:
Board: ${req.body.board || ""}
Class: ${req.body.studentClass || req.body.student_class || ""}
Target exam: ${req.body.targetExam || req.body.target_exam || ""}
Topic: ${req.body.topic || req.body.goal || ""}
Level: ${req.body.level || ""}
Weak areas: ${req.body.weakAreas || req.body.weak_areas || ""}

Return diagnostic questions, a focused plan, flashcards, and a practice task.`,
    },
  ]);

  return proxyGroq(res, messages, normalizeNumber(req.body.max_tokens ?? req.body.maxTokens, 900), 0.65);
});

app.post("/suhanova/roadmap", async (req, res) => {
  const messages = normalizeMessages([
    {
      role: "user",
      content: `Create a personalized learning roadmap for:
Board: ${req.body.board || ""}
Class: ${req.body.studentClass || req.body.student_class || ""}
Target exam: ${req.body.targetExam || req.body.target_exam || ""}
Goal: ${req.body.goal || ""}
Current level: ${req.body.level || ""}
Weak areas: ${req.body.weakAreas || req.body.weak_areas || ""}

Return diagnostic questions, a 7-day roadmap, a 30-day roadmap, today's task, and next quiz topic.`,
    },
  ]);

  return proxyGroq(res, messages, normalizeNumber(req.body.max_tokens ?? req.body.maxTokens, 1000), 0.7);
});

app.post("/suhanova/library/search", (req, res) => {
  const board = req.body.board || "";
  const studentClass = req.body.studentClass || req.body.student_class || "";
  const subject = req.body.subject || "";
  const chapter = req.body.chapter || req.body.topic || "";
  const baseQuery = [board, studentClass, subject, chapter].filter(Boolean).join(" ");

  res.json({
    ok: true,
    notesQuery: `${baseQuery} notes`,
    questionsQuery: `${baseQuery} important questions`,
    videosQuery: `${baseQuery} video lecture`,
  });
});

app.post("/auth/signup", (req, res) => {
  if (!requireFields(res, req.body, ["name", "email", "password"])) return;

  const email = String(req.body.email).trim().toLowerCase();
  if (usersByEmail.has(email)) {
    return res.status(409).json({ error: "User already exists" });
  }

  const user = {
    name: String(req.body.name).trim(),
    email,
    passwordHash: hashPassword(String(req.body.password)),
    profile: {
      name: String(req.body.name).trim(),
      college: "",
      department: "",
      skillIQScore: 0,
    },
    skills: [],
  };

  usersByEmail.set(email, user);
  res.status(201).json({ token: issueToken(email), user: publicUser(user) });
});

app.post("/auth/login", (req, res) => {
  if (!requireFields(res, req.body, ["email", "password"])) return;

  const email = String(req.body.email).trim().toLowerCase();
  const user = usersByEmail.get(email);

  if (!user || !verifyPassword(String(req.body.password), user.passwordHash)) {
    return res.status(401).json({ error: "Invalid email or password" });
  }

  res.json({ token: issueToken(email), user: publicUser(user) });
});

app.get("/profile", requireAuth, (req, res) => {
  res.json({
    ...publicUser(req.user),
    ...req.user.profile,
  });
});

app.post("/profile", requireAuth, (req, res) => {
  req.user.profile = {
    name: req.body.name ?? req.user.profile.name ?? req.user.name,
    college: req.body.college ?? req.user.profile.college ?? "",
    department: req.body.department ?? req.user.profile.department ?? "",
    skillIQScore: Number(req.body.skillIQScore ?? req.user.profile.skillIQScore ?? 0),
  };
  req.user.name = req.user.profile.name;

  res.json({
    ...publicUser(req.user),
    ...req.user.profile,
  });
});

app.get("/skills", requireAuth, (req, res) => {
  res.json({ skills: req.user.skills });
});

app.post("/skills", requireAuth, (req, res) => {
  const skills = Array.isArray(req.body.skills) ? req.body.skills : [];
  req.user.skills = skills.map((skill) => ({
    name: String(skill.name ?? "").trim(),
    category: String(skill.category ?? "General").trim(),
    proficiency: Number(skill.proficiency ?? 0),
    experienceYears: Number(skill.experienceYears ?? 0),
    notes: skill.notes ? String(skill.notes) : "",
  })).filter((skill) => skill.name);

  res.json({ skills: req.user.skills });
});

app.post("/jobs/match", requireAuth, (req, res) => {
  const location = String(req.body.location || "Bengaluru").trim();
  const skills = Array.isArray(req.body.skills) ? req.body.skills : req.user.skills;
  const skillNames = skills.map((skill) => skill.name).filter(Boolean);
  const query = `${skillNames.join(" ")} fresher jobs ${location}`.trim();

  res.json({
    query,
    results: [
      {
        title: `${skillNames[0] || "Software"} Intern`,
        company: "Search recommended openings",
        location,
        score: Math.min(95, 60 + skillNames.length * 7),
        query,
        reason: "Generated from resume text, preferred location, and saved skills.",
      },
    ],
  });
});

app.post("/api/groq", async (req, res) => {
  if (!GROQ_API_KEY) {
    return res.status(503).json({ error: "GROQ_API_KEY is not configured on the backend" });
  }

  const messages = normalizeMessages(req.body.messages);
  if (messages.length === 0) {
    return res.status(400).json({ error: "messages must contain at least one chat message" });
  }

  return proxyGroq(
    res,
    messages,
    normalizeNumber(req.body.max_tokens ?? req.body.maxTokens, 600),
    normalizeNumber(req.body.temperature, 0.7),
  );
});

async function proxyGroq(res, messages, maxTokens, temperature) {
  if (!GROQ_API_KEY) {
    return res.status(503).json({ error: "GROQ_API_KEY is not configured on the backend" });
  }

  const upstream = await fetch("https://api.groq.com/openai/v1/chat/completions", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${GROQ_API_KEY}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      model: GROQ_DEFAULT_MODEL,
      messages,
      max_tokens: maxTokens,
      temperature,
      stream: false,
    }),
  });

  return res.status(upstream.status).json(await upstream.json());
}

app.post("/api/mistral", async (req, res) => {
  if (!MISTRAL_API_KEY) {
    return res.status(503).json({ error: "MISTRAL_API_KEY is not configured on the backend" });
  }

  const messages = normalizeMessages(req.body.messages);
  if (messages.length === 0) {
    return res.status(400).json({ error: "messages must contain at least one chat message" });
  }

  const upstream = await fetch("https://api.mistral.ai/v1/chat/completions", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${MISTRAL_API_KEY}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      model: MISTRAL_DEFAULT_MODEL,
      messages,
      max_tokens: normalizeNumber(req.body.max_tokens ?? req.body.maxTokens, 1500),
      temperature: normalizeNumber(req.body.temperature, 0.3),
    }),
  });

  res.status(upstream.status).json(await upstream.json());
});

function normalizeMessages(messages) {
  if (!Array.isArray(messages)) return [];
  return messages
    .map((message) => ({
      role: String(message.role || "user").trim() || "user",
      content: String(message.content || "").trim(),
    }))
    .filter((message) => message.content);
}

function normalizeNumber(value, fallback) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

app.use((err, _req, res, _next) => {
  console.error(err);
  res.status(500).json({ error: "Backend request failed" });
});

app.listen(PORT, () => {
  console.log(`Suhanova backend running on port ${PORT}`);
});
