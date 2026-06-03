package com.example.suhanova.network

import android.content.Context
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

const val SUHANOVA_BACKEND_BASE_URL = "https://suhanova.onrender.com/"

data class BackendHealthResponse(
    val ok: Boolean,
    val service: String,
)

data class SuhanovaSetupRequest(
    val name: String = "Suhana",
    val board: String,
    val studentClass: String,
    val targetExam: String,
    val examDate: String = "",
    val goal: String,
    val level: String,
    val weakAreas: String,
)

data class SuhanovaSetupResponse(
    val ok: Boolean,
    val service: String,
    val setup: SuhanovaSetupRequest,
)

data class SuhanovaAIRequest(
    val board: String,
    val studentClass: String,
    val targetExam: String,
    val topic: String? = null,
    val goal: String? = null,
    val level: String,
    val weakAreas: String,
    val maxTokens: Int = 900,
)

data class LibrarySearchRequest(
    val board: String,
    val studentClass: String,
    val subject: String,
    val chapter: String,
)

data class LibrarySearchResponse(
    val ok: Boolean,
    val notesQuery: String,
    val questionsQuery: String,
    val videosQuery: String,
)

private interface SuhanovaBackendService {
    @GET("health")
    suspend fun health(): BackendHealthResponse

    @POST("suhanova/setup")
    suspend fun setup(@Body request: SuhanovaSetupRequest): SuhanovaSetupResponse

    @POST("suhanova/study-plan")
    suspend fun studyPlan(@Body request: SuhanovaAIRequest): GroqResponse

    @POST("suhanova/roadmap")
    suspend fun roadmap(@Body request: SuhanovaAIRequest): GroqResponse

    @POST("suhanova/library/search")
    suspend fun librarySearch(@Body request: LibrarySearchRequest): LibrarySearchResponse
}

class BackendClient(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val service: SuhanovaBackendService by lazy {
        Retrofit.Builder()
            .baseUrl(SUHANOVA_BACKEND_BASE_URL)
            .client(buildHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SuhanovaBackendService::class.java)
    }

    suspend fun health(): Result<BackendHealthResponse> = apiCall {
        service.health()
    }

    suspend fun setup(request: SuhanovaSetupRequest): Result<SuhanovaSetupResponse> = apiCall {
        service.setup(request)
    }

    suspend fun studyPlan(request: SuhanovaAIRequest): Result<String> = apiCall {
        service.studyPlan(request).choices.firstOrNull()?.message?.content
            ?: "Nova returned an empty study plan. Try again."
    }

    suspend fun roadmap(request: SuhanovaAIRequest): Result<String> = apiCall {
        service.roadmap(request).choices.firstOrNull()?.message?.content
            ?: "Nova returned an empty roadmap. Try again."
    }

    suspend fun librarySearch(request: LibrarySearchRequest): Result<LibrarySearchResponse> = apiCall {
        service.librarySearch(request)
    }

    private fun buildHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")

                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(Exception(cleanBackendError(e), e))
        }
    }

    private fun cleanBackendError(error: Exception): String {
        val raw = error.message.orEmpty()
        return when {
            raw.contains("Unable to resolve host", ignoreCase = true) ->
                "Backend is unavailable. Please check your internet connection."
            raw.contains("timeout", ignoreCase = true) ->
                "Backend took too long to respond. Please try again."
            raw.isBlank() ->
                "Backend request failed. Please try again."
            else ->
                raw
        }
    }

    companion object {
        private const val PREFS_NAME = "suhanova_backend_session"
    }
}
