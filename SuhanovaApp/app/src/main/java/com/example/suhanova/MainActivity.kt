package com.example.suhanova

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.suhanova.notifications.createNotificationChannels
import com.example.suhanova.notifications.scheduleAllNotifications
import com.example.suhanova.notifications.sendNovaNotification
import com.example.suhanova.notifications.CHANNEL_NOVA
import com.example.suhanova.theme.GlassBg
import com.example.suhanova.theme.GlassBorder
import com.example.suhanova.theme.NovaGold
import com.example.suhanova.theme.SpaceBlack
import com.example.suhanova.theme.SuhanovaTheme
import com.example.suhanova.theme.TextMuted
import com.example.suhanova.theme.TextPrimary
import com.example.suhanova.theme.TextSecondary
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            createNotificationChannels(this)
            scheduleAllNotifications(this)
            sendNovaNotification(
                context   = this,
                channelId = CHANNEL_NOVA,
                notifId   = 9999,
                title     = "Suhanova is ready for you, Suhana! 🩺",
                message   = "Your AI study companion is set up. Daily reminders at 7AM, 7PM and 9PM. Nova is watching your streak! 🔥",
                emoji     = "✨",
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Crash catcher — writes crash log to external storage so we can read it without ADB
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val trace = android.util.Log.getStackTraceString(throwable)
            try {
                val file = java.io.File(getExternalFilesDir(null), "crash.txt")
                file.writeText(trace)
            } catch (e: Exception) { /* ignore */ }
            kotlin.system.exitProcess(1)
        }

        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    createNotificationChannels(this)
                    scheduleAllNotifications(this)
                }
                else -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            createNotificationChannels(this)
            scheduleAllNotifications(this)
        }

        setContent {
            SuhanovaTheme {
                SuhanovaApp()
            }
        }
    }
}

@Composable
fun SuhanovaApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("suhanova_first_run", android.content.Context.MODE_PRIVATE) }
    val securityPrefs = remember { context.getSharedPreferences("suhanova_security", android.content.Context.MODE_PRIVATE) }
    var startupDone by remember { mutableStateOf(false) }
    var onboardingComplete by remember { mutableStateOf(prefs.getBoolean("onboarding_complete", false)) }
    var biometricUnlocked by remember { mutableStateOf(false) }
    val biometricEnabled = securityPrefs.getBoolean("biometric_enabled", false)

    if (!startupDone) {
        PersonalizedStartup(onDone = { startupDone = true })
    } else if (onboardingComplete && biometricEnabled && !biometricUnlocked) {
        BiometricGate(onUnlocked = { biometricUnlocked = true })
    } else if (onboardingComplete) {
        SuhanovaNavigation()
    } else {
        FirstRunQuestions {
            prefs.edit()
                .putString("goal", it.goal)
                .putString("board", it.board)
                .putString("student_class", it.studentClass)
                .putString("target_exam", it.targetExam)
                .putString("level", it.level)
                .putString("weak_areas", it.weakAreas)
                .putLong("exam_date_millis", it.examDateMillis)
                .putBoolean("onboarding_complete", true)
                .apply()
            onboardingComplete = true
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PersonalizedStartup(onDone: () -> Unit) {
    val photos = remember {
        listOf(
            R.drawable.startup_suhana_1,
            R.drawable.startup_suhana_2,
            R.drawable.startup_suhana_3,
            R.drawable.startup_suhana_4,
            R.drawable.startup_suhana_5,
        )
    }
    val lines = remember {
        listOf(
            "Suhana, you are the soft reason this app has a heartbeat.",
            "You matter quietly, deeply, and more than any screen can ever fully say.",
            "Every sunrise inside Suhanova is a small reminder: your dreams are worth protecting.",
            "Your smile changes the whole room; your becoming changes the whole story.",
            "Welcome back, Doctor Suhana. The world is lucky you are here."
        )
    }
    var index by remember { mutableIntStateOf(0) }
    val infiniteTransition = rememberInfiniteTransition(label = "startup3D")
    val floatDepth by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "startupFloatDepth",
    )
    val lightSweep by infiniteTransition.animateFloat(
        initialValue = -420f,
        targetValue = 420f,
        animationSpec = infiniteRepeatable(
            animation = tween(2300),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "startupLightSweep",
    )
    val greetingPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "startupGreetingPulse",
    )
    val imageScale by animateFloatAsState(
        targetValue = if (index % 2 == 0) 1.10f else 1.04f,
        animationSpec = tween(1800),
        label = "startupImageScale",
    )
    val imageRotation by animateFloatAsState(
        targetValue = if (index % 2 == 0) -3.4f else 3.1f,
        animationSpec = tween(1800),
        label = "startupImageRotation",
    )

    LaunchedEffect(Unit) {
        repeat(photos.size) { step ->
            index = step
            delay(1850)
        }
        delay(600)
        onDone()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF06050A),
                        Color(0xFF181022),
                        Color(0xFF050409),
                    )
                )
            )
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFFFF4F9A).copy(alpha = 0.28f),
                        Color(0xFFFFD76A).copy(alpha = 0.12f),
                        Color.Transparent,
                    )
                )
            )
        )

        Crossfade(targetState = photos[index], animationSpec = tween(700), label = "startupPhoto") { photo ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 76.dp)
                    .graphicsLayer {
                        rotationZ = imageRotation
                        rotationX = if (index % 2 == 0) 5f else -5f
                        rotationY = if (index % 2 == 0) -7f else 7f
                        cameraDistance = 18f * density
                        translationY = floatDepth
                        shadowElevation = 28f
                    }
            ) {
                Image(
                    painter = painterResource(photo),
                    contentDescription = "Suhanova startup memory",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                        },
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.06f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.40f),
                                )
                            )
                        )
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .graphicsLayer {
                            translationX = lightSweep
                            rotationZ = 18f
                            alpha = 0.42f
                        }
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.20f),
                                    Color.Transparent,
                                )
                            )
                        )
                )
            }
        }

        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(
                        NovaGold.copy(alpha = 0.24f),
                        Color(0xFFFF5E93).copy(alpha = 0.10f),
                        Color.Transparent,
                    )
                )
            )
        )

        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.44f),
                        Color.Black.copy(alpha = 0.03f),
                        Color.Black.copy(alpha = 0.82f),
                    )
                )
            )
        )

        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 34.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                photos.forEachIndexed { i, _ ->
                    Box(
                        Modifier.weight(1f).height(3.dp).clip(CircleShape)
                            .background(
                                if (i <= index) {
                                    Brush.horizontalGradient(listOf(Color(0xFFFFD76A), Color(0xFFFF4F9A)))
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.24f),
                                            Color.White.copy(alpha = 0.14f),
                                        )
                                    )
                                }
                            )
                    )
                }
            }

            Spacer(Modifier.height(1.dp))

            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        (fadeIn(tween(450)) + slideInVertically(tween(650)) { it / 2 }) togetherWith
                            (fadeOut(tween(250)) + slideOutVertically(tween(300)) { -it / 3 })
                    },
                    label = "startupText",
                ) { textIndex ->
                    Column(
                        Modifier.fillMaxWidth().graphicsLayer {
                            scaleX = greetingPulse
                            scaleY = greetingPulse
                            rotationX = 7f
                            cameraDistance = 14f * density
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            "FOR YOU",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFFFFD76A),
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer {
                                shadowElevation = 18f
                                translationY = -4f
                            },
                        )
                        Text(
                            lines[textIndex],
                            style = MaterialTheme.typography.headlineSmall.copy(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.White,
                                        Color(0xFFFFD76A),
                                        Color(0xFFFF5E93),
                                    )
                                ),
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp,
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer {
                                shadowElevation = 24f
                                translationY = -floatDepth / 4f
                            },
                        )
                        Text(
                            "This is not just an opening screen. It is a little celebration of you, every time.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.88f),
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp,
                            ),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BiometricGate(onUnlocked: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var status by remember { mutableStateOf("Unlock Suhanova to continue") }
    var promptNonce by remember { mutableIntStateOf(0) }

    fun showPrompt() {
        val executor = ContextCompat.getMainExecutor(context)
        val cancellation = android.os.CancellationSignal()
        val prompt = android.hardware.biometrics.BiometricPrompt.Builder(context)
            .setTitle("Unlock Suhanova")
            .setSubtitle("Use biometric or device credential")
            .setNegativeButton("Cancel", executor) { _, _ ->
                status = "Authentication cancelled"
            }
            .build()

        prompt.authenticate(
            cancellation,
            executor,
            object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult) {
                    status = "Unlocked"
                    onUnlocked()
                }

                override fun onAuthenticationFailed() {
                    status = "Authentication failed. Try again."
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    status = errString.toString()
                }
            }
        )
    }

    LaunchedEffect(promptNonce) {
        showPrompt()
    }

    Column(
        Modifier.fillMaxSize().background(SpaceBlack).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Suhanova Locked",
            style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary, fontWeight = FontWeight.ExtraBold),
        )
        Spacer(Modifier.height(8.dp))
        Text(status, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = { promptNonce++ },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = SpaceBlack),
        ) {
            Text("Unlock", fontWeight = FontWeight.Bold)
        }
    }
}

data class FirstRunAnswers(
    val goal: String,
    val board: String,
    val studentClass: String,
    val targetExam: String,
    val level: String,
    val weakAreas: String,
    val examDateMillis: Long,
)

@Composable
private fun FirstRunQuestions(onComplete: (FirstRunAnswers) -> Unit) {
    var goal by remember { mutableStateOf("") }
    var board by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("") }
    var targetExam by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var weakAreas by remember { mutableStateOf("") }
    var examDate by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().background(SpaceBlack).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Set Up Nova",
            style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary, fontWeight = FontWeight.ExtraBold),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Answer these once so the app adapts to your class, board, exam, and real needs.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
        )
        Spacer(Modifier.height(22.dp))

        FirstRunInput(goal, { goal = it }, "What is your goal?", "Example: board exams, NEET, improve Maths")
        Spacer(Modifier.height(12.dp))
        FirstRunInput(board, { board = it }, "Which board?", "Example: CBSE, ICSE, State Board, ISC")
        Spacer(Modifier.height(12.dp))
        FirstRunInput(studentClass, { studentClass = it }, "Which class?", "Example: 10th, 11th, 12th, dropper")
        Spacer(Modifier.height(12.dp))
        FirstRunInput(targetExam, { targetExam = it }, "Which target exam?", "Example: Class 10 boards, Class 12 boards, NEET, school tests")
        Spacer(Modifier.height(12.dp))
        FirstRunInput(examDate, { examDate = it; dateError = "" }, "When is your exam?", "YYYY-MM-DD, example: 2026-03-05")
        if (dateError.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(dateError, style = MaterialTheme.typography.bodySmall.copy(color = NovaGold))
        }
        Spacer(Modifier.height(12.dp))
        FirstRunInput(level, { level = it }, "What is your current level?", "Example: beginner, average, strong in Chemistry")
        Spacer(Modifier.height(12.dp))
        FirstRunInput(weakAreas, { weakAreas = it }, "What feels hard right now?", "Example: organic reactions, numericals, memorization")
        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                val millis = parseExamDateMillis(examDate)
                if (millis == null) {
                    dateError = "Enter date like 2026-05-03"
                } else {
                    onComplete(FirstRunAnswers(goal, board, studentClass, targetExam, level, weakAreas, millis))
                }
            },
            enabled = goal.isNotBlank() && board.isNotBlank() && studentClass.isNotBlank() && targetExam.isNotBlank() && examDate.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = SpaceBlack),
        ) {
            Text("Start Real-Time App", fontWeight = FontWeight.Bold)
        }
    }
}

private fun parseExamDateMillis(value: String): Long? = try {
    LocalDate.parse(value.trim())
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
} catch (_: Exception) {
    null
}

@Composable
private fun FirstRunInput(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
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
