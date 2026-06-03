package com.example.suhanova.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NovaChatRepository {
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<GroqMessage>,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = buildList {
                add(GroqMessage(role = "system", content = NOVA_SYSTEM_PROMPT))
                addAll(conversationHistory.takeLast(10))
                add(GroqMessage(role = "user", content = userMessage))
            }

            val response = GroqClient.service.chat(
                request = GroqRequest(messages = messages),
            )

            val reply = response.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("Empty response from Nova"))

            Result.success(reply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class GeneratedQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
)

data class GeneratedQuestionsWrapper(val questions: List<GeneratedQuestion>)

class QuizRepository {

    private val gson = Gson()

    suspend fun generateQuestions(
        subject: String,
        topic: String,
        difficulty: String = "Mixed",
        count: Int = 5,
    ): Result<List<GeneratedQuestion>> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildMcqPrompt(subject, topic, difficulty, count)

            val response = MistralClient.service.generate(
                request = MistralRequest(
                    messages = listOf(MistralMessage(role = "user", content = prompt))
                ),
            )

            val rawJson = response.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("Empty response from quiz AI"))

            val cleanJson = rawJson.trim()
                .removePrefix("```json").removeSuffix("```")
                .removePrefix("```").removeSuffix("```")
                .trim()

            val wrapper = gson.fromJson(cleanJson, GeneratedQuestionsWrapper::class.java)
            if (wrapper.questions.isEmpty()) {
                Result.failure(Exception("AI did not return quiz questions. Try a more specific topic."))
            } else {
                Result.success(wrapper.questions)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
