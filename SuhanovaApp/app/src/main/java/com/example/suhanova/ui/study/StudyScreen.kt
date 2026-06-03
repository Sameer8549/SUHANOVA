package com.example.suhanova.ui.study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
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
import com.example.suhanova.ui.components.StarFieldCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LiveStudyRepository {
    suspend fun generateStudySession(
        goal: String,
        level: String,
        board: String,
        studentClass: String,
        targetExam: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
Create a real-time study session for this learner.

Board: $board
Class: $studentClass
Target exam: $targetExam
Goal/topic: $goal
Current level or need: $level

Return:
1. Three diagnostic questions to ask before studying
2. A focused 45-minute study plan
3. Five flashcards generated for this topic only
4. One quick practice task

Do not use generic canned content. Keep it specific to the user's topic.
""".trimIndent()

            val response = GroqClient.service.chat(
                GroqRequest(
                    messages = listOf(GroqMessage("user", prompt)),
                    maxTokens = 900,
                    temperature = 0.65f,
                )
            )
            Result.success(response.choices.firstOrNull()?.message?.content.orEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Composable
fun StudyScreen(onNavigate: (String) -> Unit = {}) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val setupPrefs = remember { ctx.getSharedPreferences("suhanova_first_run", android.content.Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val repository = remember { LiveStudyRepository() }

    var topic by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
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
                            "Live Study",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        )
                        Text(
                            "No preloaded flashcards or schedules. Tell Nova what you need now.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        )
                    }
                }
            }

            item {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("WHAT SHOULD WE STUDY?", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                        LiveInput(
                            value = topic,
                            onValueChange = { topic = it },
                            placeholder = "Example: Human circulation, electrostatics, aldehydes",
                        )
                        Text("WHAT SHOULD NOVA KNOW FIRST?", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                        LiveInput(
                            value = level,
                            onValueChange = { level = it },
                            placeholder = "Example: I know basics, weak in numericals, exam tomorrow",
                        )
                        Button(
                            onClick = {
                                loading = true
                                error = ""
                                output = ""
                                scope.launch {
                                    repository.generateStudySession(
                                        goal = topic,
                                        level = level,
                                        board = setupPrefs.getString("board", "").orEmpty(),
                                        studentClass = setupPrefs.getString("student_class", "").orEmpty(),
                                        targetExam = setupPrefs.getString("target_exam", "").orEmpty(),
                                    ).fold(
                                        onSuccess = { output = it.ifBlank { "Nova returned an empty response. Try a more specific topic." } },
                                        onFailure = { error = it.message ?: "AI study generation failed. Please try again." },
                                    )
                                    loading = false
                                }
                            },
                            enabled = !loading && topic.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = SpaceBlack),
                        ) {
                            if (loading) {
                                CircularProgressIndicator(color = SpaceBlack, strokeWidth = 2.dp)
                                Spacer(Modifier.padding(horizontal = 6.dp))
                                Text("Generating...")
                            } else {
                                Text("Ask Nova To Build My Session", fontWeight = FontWeight.Bold)
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

            if (output.isNotBlank()) {
                item {
                    GlassCard(glowColor = NovaGold) {
                        Text("NOVA'S LIVE STUDY SESSION", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(10.dp))
                        Text(output, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, lineHeight = MaterialTheme.typography.bodyMedium.lineHeight))
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
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
        minLines = 1,
    )
}
