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

const val SKILLIQ_BACKEND_BASE_URL = "https://see-attachments-greene-shoes.trycloudflare.com/"

data class BackendHealthResponse(
    val ok: Boolean,
    val service: String,
)

data class BackendUser(
    val name: String,
    val email: String,
)

data class AuthRequest(
    val name: String? = null,
    val email: String,
    val password: String,
)

data class AuthResponse(
    val token: String,
    val user: BackendUser,
)

data class ProfileRequest(
    val name: String,
    val college: String,
    val department: String,
    val skillIQScore: Int,
)

data class ProfileResponse(
    val name: String? = null,
    val email: String? = null,
    val college: String? = null,
    val department: String? = null,
    val skillIQScore: Int? = null,
)

data class SkillPayload(
    val name: String,
    val category: String,
    val proficiency: Int,
    val experienceYears: Int? = null,
    val notes: String? = null,
)

data class SkillsRequest(
    val skills: List<SkillPayload>,
)

data class SkillsResponse(
    val skills: List<SkillPayload> = emptyList(),
)

data class JobMatchRequest(
    val resumeText: String,
    val location: String,
    val skills: List<SkillPayload>,
)

data class JobMatchResult(
    val title: String? = null,
    val company: String? = null,
    val location: String? = null,
    val score: Int? = null,
    val query: String? = null,
    val reason: String? = null,
)

data class JobMatchResponse(
    val query: String? = null,
    val results: List<JobMatchResult> = emptyList(),
)

private interface SkillIQBackendService {
    @GET("health")
    suspend fun health(): BackendHealthResponse

    @POST("auth/signup")
    suspend fun signup(@Body request: AuthRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @POST("profile")
    suspend fun syncProfile(@Body request: ProfileRequest): ProfileResponse

    @GET("skills")
    suspend fun getSkills(): SkillsResponse

    @POST("skills")
    suspend fun syncSkills(@Body request: SkillsRequest): SkillsResponse

    @POST("jobs/match")
    suspend fun matchJobs(@Body request: JobMatchRequest): JobMatchResponse
}

class BackendClient(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val service: SkillIQBackendService by lazy {
        Retrofit.Builder()
            .baseUrl(SKILLIQ_BACKEND_BASE_URL)
            .client(buildHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SkillIQBackendService::class.java)
    }

    val isLoggedIn: Boolean
        get() = sessionToken != null

    private val sessionToken: String?
        get() = prefs.getString(KEY_SESSION_TOKEN, null)

    suspend fun health(): Result<BackendHealthResponse> = apiCall {
        service.health()
    }

    suspend fun signup(name: String, email: String, password: String): Result<AuthResponse> = apiCall {
        service.signup(AuthRequest(name = name, email = email, password = password)).also(::storeSession)
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> = apiCall {
        service.login(AuthRequest(email = email, password = password)).also(::storeSession)
    }

    suspend fun getProfile(): Result<ProfileResponse> = apiCall {
        service.getProfile()
    }

    suspend fun syncProfile(profile: ProfileRequest): Result<ProfileResponse> = apiCall {
        service.syncProfile(profile)
    }

    suspend fun getSkills(): Result<SkillsResponse> = apiCall {
        service.getSkills()
    }

    suspend fun syncSkills(skills: List<SkillPayload>): Result<SkillsResponse> = apiCall {
        service.syncSkills(SkillsRequest(skills))
    }

    suspend fun matchJobs(request: JobMatchRequest): Result<JobMatchResponse> = apiCall {
        service.matchJobs(request)
    }

    fun logout() {
        prefs.edit().remove(KEY_SESSION_TOKEN).apply()
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

                sessionToken?.let { token ->
                    builder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    private fun storeSession(response: AuthResponse) {
        prefs.edit()
            .putString(KEY_SESSION_TOKEN, response.token)
            .putString(KEY_USER_NAME, response.user.name)
            .putString(KEY_USER_EMAIL, response.user.email)
            .apply()
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
            raw.contains("401", ignoreCase = true) ->
                "Session expired. Please log in again."
            raw.isBlank() ->
                "Backend request failed. Please try again."
            else ->
                raw
        }
    }

    companion object {
        private const val PREFS_NAME = "suhanova_backend_session"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }
}
