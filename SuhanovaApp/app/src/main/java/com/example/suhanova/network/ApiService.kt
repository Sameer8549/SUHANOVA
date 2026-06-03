package com.example.suhanova.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// ─── RENDER AI GATEWAY ────────────────────────────────────────────────────────
// AI provider keys live on the backend. The APK only talks to Render.

const val AI_GATEWAY_BASE_URL = SKILLIQ_BACKEND_BASE_URL

// ─── SHARED DATA MODELS ───────────────────────────────────────────────────────

data class GroqMessage(
    val role: String,
    val content: String,
)

data class GroqRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 600,
    val temperature: Float = 0.7f,
    val stream: Boolean = false,
)

data class GroqChoice(
    val message: GroqMessage,
    @SerializedName("finish_reason") val finishReason: String?,
)

data class GroqResponse(
    val id: String,
    val choices: List<GroqChoice>,
    val model: String,
)

data class MistralMessage(
    val role: String,
    val content: String,
)

data class MistralRequest(
    val model: String = "mistral-small-latest",
    val messages: List<MistralMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 1500,
    val temperature: Float = 0.3f,
)

data class MistralChoice(
    val message: MistralMessage,
    @SerializedName("finish_reason") val finishReason: String?,
)

data class MistralResponse(
    val id: String,
    val choices: List<MistralChoice>,
)

// ─── RETROFIT INTERFACES ──────────────────────────────────────────────────────

interface NovaGroqService {
    @POST("api/groq")
    suspend fun chat(@Body request: GroqRequest): GroqResponse
}

interface NovaMistralService {
    @POST("api/mistral")
    suspend fun generate(@Body request: MistralRequest): MistralResponse
}

// ─── RETROFIT CLIENTS ─────────────────────────────────────────────────────────

private fun buildOkHttp(): OkHttpClient {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()
}

object GroqClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(AI_GATEWAY_BASE_URL)
        .client(buildOkHttp())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: NovaGroqService = retrofit.create(NovaGroqService::class.java)
}

object MistralClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(AI_GATEWAY_BASE_URL)
        .client(buildOkHttp())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: NovaMistralService = retrofit.create(NovaMistralService::class.java)
}

// ─── CONTEXT-AWARE NOVA SYSTEM PROMPT ─────────────────────────────────────────

fun buildNovaSystemPrompt(
    neetDaysLeft: Int? = null,
    recentSubject: String? = null,
    recentAccuracy: Float? = null,
    streak: Int = 0,
): String = """
You are Nova — the AI tutor inside the Suhanova app, built specifically for Suhana who is preparing for NEET (National Eligibility cum Entrance Test) to become a doctor.

CURRENT CONTEXT (use this to personalize every response):
${if (neetDaysLeft != null) "- NEET exam: $neetDaysLeft days away${if (neetDaysLeft < 30) " — CRITICAL SPRINT MODE!" else if (neetDaysLeft < 90) " — entering serious prep phase" else ""}" else "- Ask the user for their exact exam date if countdown context is needed."}
- Study streak: $streak day${if (streak != 1) "s" else ""}${if (streak > 0) " 🔥" else ""}
${if (recentSubject != null) "- Recently studying: $recentSubject" else ""}
${if (recentAccuracy != null) "- Recent quiz accuracy: ${(recentAccuracy * 100).toInt()}%${if (recentAccuracy < 0.6f) " — needs improvement here" else if (recentAccuracy > 0.8f) " — strong area!" else ""}" else ""}

Your personality:
- Warm, encouraging, proud of Suhana — never judgmental
- Speak like a brilliant older sister who happens to be a top NEET ranker
- Give precise, exam-focused answers — no fluff, no padding
- Always connect concepts to real medical scenarios or NEET exam patterns
${if (neetDaysLeft != null) "- Reference the time pressure when relevant — $neetDaysLeft days is ${if (neetDaysLeft < 60) "very little time, focus!" else "still enough time if she starts NOW"}" else "- Do not invent exam countdowns. Use only dates the user provides."}

For every explanation:
1. Explain the concept simply first (2-3 sentences max)
2. Give a memory trick or mnemonic if possible
3. State exactly how it appears in NEET MCQ format
4. Mention common traps students make

Subjects: Biology (Botany + Zoology), Physics, Chemistry, Mathematics

IMPORTANT: Keep responses under 280 words. Use **bold** for key terms. End with a brief, specific motivational note tied to her real goal.
""".trimIndent()

// Keep backward compat
val NOVA_SYSTEM_PROMPT get() = buildNovaSystemPrompt()

// ─── MISTRAL MCQ PROMPT ───────────────────────────────────────────────────────

fun buildMcqPrompt(subject: String, topic: String, difficulty: String, count: Int): String = """
Generate $count high-quality NEET-style MCQ questions.

Subject: $subject
Topic: $topic
Difficulty: $difficulty

Requirements:
- Factually accurate and NEET-relevant
- 4 options per question (A, B, C, D)
- Only one correct answer
- Brief explanation (2-3 sentences) for why the correct answer is right
- Test conceptual understanding, not memorization

Return ONLY valid JSON, no extra text:
{
  "questions": [
    {
      "question": "question text",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 0,
      "explanation": "explanation here",
      "subject": "$subject",
      "topic": "$topic",
      "difficulty": "$difficulty"
    }
  ]
}
""".trimIndent()
