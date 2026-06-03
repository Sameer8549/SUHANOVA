package com.example.suhanova.ui.roadmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.suhanova.network.GroqClient
import com.example.suhanova.network.GroqMessage
import com.example.suhanova.network.GroqRequest
import com.example.suhanova.theme.GlassBg
import com.example.suhanova.theme.GlassBorder
import com.example.suhanova.theme.NovaGold
import com.example.suhanova.theme.SpaceBlack
import com.example.suhanova.theme.StellarPink
import com.example.suhanova.theme.TextMuted
import com.example.suhanova.theme.TextPrimary
import com.example.suhanova.theme.TextSecondary
import com.example.suhanova.ui.components.GlassCard
import com.example.suhanova.ui.components.PageBackButton
import com.example.suhanova.ui.components.StarFieldCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoadmapAIRepository {
    suspend fun generatePersonalizedRoadmap(
        goal: String,
        currentLevel: String,
        weakAreas: String,
        board: String,
        studentClass: String,
        targetExam: String,
    ): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val prompt = """
Create a personalized real-time learning roadmap.

Board: $board
Class: $studentClass
Target exam: $targetExam
Goal/exam: $goal
Current level: $currentLevel
Weak areas or confusion: $weakAreas

Return:
1. Diagnostic questions Nova should ask first
2. A 7-day roadmap
3. A 30-day roadmap
4. What to study today
5. What to quiz next

Do not assume completed chapters, scores, streaks, or old progress. Use only the user-provided context.
""".trimIndent()

                val response = GroqClient.service.chat(
                    GroqRequest(
                        messages = listOf(GroqMessage("user", prompt)),
                        maxTokens = 1000,
                        temperature = 0.7f,
                    )
                )
                Result.success(response.choices.firstOrNull()?.message?.content.orEmpty())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

@Composable
fun RoadmapScreen(onBack: () -> Unit = {}) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val setupPrefs = remember { ctx.getSharedPreferences("suhanova_first_run", android.content.Context.MODE_PRIVATE) }
    val repository = remember { RoadmapAIRepository() }
    val scope = rememberCoroutineScope()

    var goal by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var weakAreas by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(SpaceBlack)) {
        StarFieldCanvas(Modifier.fillMaxSize())

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
        ) {
            item {
                AnimatedVisibility(visible, enter = fadeIn() + slideInVertically { -30 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Live Roadmap",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        )
                        PageBackButton(onClick = onBack)
                        Text(
                            "Nova builds the path from your answers. No pre-added progress.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        )
                    }
                }
            }

            item {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RoadmapInput(goal, { goal = it }, "Goal or exam", "Example: NEET 2026, semester exams, Biology basics")
                        RoadmapInput(level, { level = it }, "Current level", "Example: beginner, scoring 60%, strong in Bio")
                        RoadmapInput(weakAreas, { weakAreas = it }, "Weak areas", "Example: physics numericals, organic reactions")

                        Button(
                            onClick = {
                                loading = true
                                error = ""
                                plan = ""
                                scope.launch {
                                    repository.generatePersonalizedRoadmap(
                                        goal = goal,
                                        currentLevel = level,
                                        weakAreas = weakAreas,
                                        board = setupPrefs.getString("board", "").orEmpty(),
                                        studentClass = setupPrefs.getString("student_class", "").orEmpty(),
                                        targetExam = setupPrefs.getString("target_exam", "").orEmpty(),
                                    ).fold(
                                        onSuccess = { plan = it.ifBlank { "Nova returned an empty roadmap. Add more detail and try again." } },
                                        onFailure = { error = it.message ?: "AI roadmap generation failed. Please try again." },
                                    )
                                    loading = false
                                }
                            },
                            enabled = !loading && goal.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = SpaceBlack),
                        ) {
                            if (loading) {
                                CircularProgressIndicator(color = SpaceBlack, strokeWidth = 2.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Building...")
                            } else {
                                Text("Generate My Roadmap", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (error.isNotBlank()) {
                item {
                    Box(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(StellarPink.copy(alpha = 0.08f))
                            .border(1.dp, StellarPink.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                    ) {
                        Text(error, style = MaterialTheme.typography.bodyMedium.copy(color = StellarPink))
                    }
                }
            }

            if (plan.isNotBlank()) {
                item {
                    GlassCard(glowColor = NovaGold) {
                        Text("NOVA'S LIVE ROADMAP", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(10.dp))
                        Text(plan, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadmapInput(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontWeight = FontWeight.Bold))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaGold,
                unfocusedBorderColor = GlassBorder,
                cursorColor = NovaGold,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = GlassBg,
                unfocusedContainerColor = GlassBg,
            ),
            shape = RoundedCornerShape(14.dp),
        )
    }
}
