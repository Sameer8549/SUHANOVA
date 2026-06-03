package com.example.suhanova.network

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ─── PERSONALIZED QUESTION ENGINE ─────────────────────────────────────────────
// Generates questions based on what Suhana finds hard

data class WeakArea(
    val subject: String,
    val topic: String,
    val accuracy: Float,
    val lastAttempted: Long = System.currentTimeMillis(),
)

class PersonalizedQuizRepository {

    private val gson = Gson()

    /**
     * Generate questions targeting Suhana's weakest topics specifically.
     * Uses Mistral to create contextually aware, pedagogically sound questions.
     */
    suspend fun generatePersonalizedQuiz(
        weakAreas: List<WeakArea>,
        count: Int = 5,
    ): Result<List<GeneratedQuestion>> = withContext(Dispatchers.IO) {
        try {
            // Pick the 3 weakest areas to focus on
            val targets = weakAreas.sortedBy { it.accuracy }.take(3)
            val targetDesc = targets.joinToString(", ") { "${it.topic} (${it.subject})" }

            val prompt = """
You are creating a PERSONALIZED quiz for a student preparing for NEET.

IMPORTANT: Suhana specifically struggles with these topics (ordered by weakness):
${targets.mapIndexed { i, wa -> "${i+1}. ${wa.topic} (${wa.subject}) — she scores ${(wa.accuracy * 100).toInt()}% on average" }.joinToString("\n")}

Generate $count MCQ questions that will help Suhana specifically improve in these weak areas.

Requirements:
- Focus MOST questions on her weakest areas
- Use clear, simple language in explanations — like a caring tutor explaining to a friend
- Start explanations with "Suhana," for the first question to make it personal
- Include memory tricks or mnemonics where possible
- Each explanation should specifically say WHY the wrong options are wrong
- Questions should be NEET-difficulty level

Return ONLY valid JSON (no extra text):
{
  "questions": [
    {
      "question": "question text",
      "options": ["A", "B", "C", "D"],
      "correctIndex": 0,
      "explanation": "explanation with memory trick",
      "subject": "subject name",
      "topic": "topic name",
      "difficulty": "Easy/Medium/Hard"
    }
  ]
}
""".trimIndent()

            val response = MistralClient.service.generate(
                request = MistralRequest(messages = listOf(MistralMessage("user", prompt)), maxTokens = 2048),
            )

            val rawJson = response.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("Empty response"))

            val cleanJson = rawJson.trim().removePrefix("```json").removeSuffix("```").removePrefix("```").removeSuffix("```").trim()
            val wrapper   = gson.fromJson(cleanJson, GeneratedQuestionsWrapper::class.java)
            Result.success(wrapper.questions)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ─── NOVA BEST FRIEND REPOSITORY ──────────────────────────────────────────────
// Nova as a warm, caring best friend — not just a tutor

class NovaBestFriendRepository {

    private val gson = Gson()

    /**
     * Get a personalized daily message based on:
     * - Time of day
     * - Suhana's current streak
     * - Her weak areas
     * - Upcoming NEET date
     */
    suspend fun getDailyMessage(
        hour: Int,
        streak: Int,
        weakArea: String,
        daysToNEET: Int,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timeContext = when (hour) {
                in 0..3  -> "It is currently past midnight"
                in 4..11 -> "It is currently morning"
                in 12..16 -> "It is currently afternoon"
                in 17..20 -> "It is currently evening"
                else     -> "It is currently night"
            }

            val prompt = """
You are Nova — the user's AI best friend and study companion. They are preparing for NEET.

Context:
- $timeContext
- She has a $streak-day study streak
- Her current weak area is: $weakArea
- Days until NEET exam: $daysToNEET days

Write ONE short, warm, personal message to Suhana (2-3 sentences max).
- Sound like a caring, excited best friend who genuinely believes in her
- Reference her specific context (time, streak, weak area, or NEET countdown)
- Never be generic — make it feel written just for her
- End with specific actionable advice for today
- Be warm, never preachy
- Tone: proud older sister who is also a top NEET ranker

Just the message text — no quotes, no labels.
""".trimIndent()

            val response = GroqClient.service.chat(
                request = GroqRequest(
                    messages    = listOf(GroqMessage("user", prompt)),
                    maxTokens   = 150,
                    temperature = 0.9f,
                ),
            )
            Result.success(response.choices.firstOrNull()?.message?.content ?: getFallbackMessage(hour, streak, daysToNEET))
        } catch (e: Exception) {
            Result.success(getFallbackMessage(hour, streak, daysToNEET))
        }
    }

    private fun getFallbackMessage(hour: Int, streak: Int, daysToNEET: Int): String {
        return when {
            hour in 0..3  -> "Midnight again, future Doctor? 🌙 $streak days straight — that's not a habit, that's character. $daysToNEET days to NEET. Every night like this one is why you'll make it."
            streak >= 7   -> "Seven days straight, Suhana! 🔥 Do you realize how rare that is? $daysToNEET days to NEET — you are exactly on track. Today: hit your weakest topic first. Get it out of the way."
            daysToNEET < 30 -> "Under 30 days, Suhana. 🩺 This is the moment everything you've studied pays off. Trust the process. Trust yourself. You've earned this."
            else          -> "Hey Suhana! 🌟 $streak days in — every session is a brick in the hospital where you'll work one day. Today counts. Make it count."
        }
    }

    /**
     * Get Nova's response as a best friend for ANY question Suhana asks.
     * More personal, warmer than the regular tutor mode.
     */
    suspend fun askBestFriend(
        question: String,
        conversationHistory: List<GroqMessage>,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bestFriendPrompt = """
You are Nova — the user's AI best friend, study partner, and personal NEET coach.

Your personality:
- You're like her brilliant best friend who happens to have topped NEET
- You use "Suhana" by name occasionally (not every sentence)
- You're warm, funny sometimes, always encouraging
- You celebrate even small wins enthusiastically
- When she asks something, you explain it simply then give the NEET angle
- You use emojis naturally, not excessively
- You never say "Great question!" — just answer naturally like a friend

If she shares that she's struggling, stressed, or tired:
- First acknowledge her feelings
- Then gently motivate
- Then get back to studying

If she asks something off-topic:
- Be friendly but redirect to studies naturally

Always end with a micro-tip specific to NEET.
""".trimIndent()

            val messages = buildList {
                add(GroqMessage("system", bestFriendPrompt))
                addAll(conversationHistory.takeLast(12))
                add(GroqMessage("user", question))
            }

            val response = GroqClient.service.chat(
                request = GroqRequest(messages = messages, maxTokens = 600, temperature = 0.8f),
            )
            Result.success(response.choices.firstOrNull()?.message?.content ?: "Let me think about that! Try again?")
        } catch (e: Exception) {
            val errMsg = when {
                e.message?.contains("401") == true ->
                    "AI service is unavailable. Check the backend configuration and try again."
                e.message?.contains("connect") == true ->
                    "📶 You're offline — but I'm still here! Check your connection and ask again."
                else -> "⚠️ Nova had a moment. Try again! (${e.message?.take(50)})"
            }
            Result.success(errMsg)
        }
    }
}
