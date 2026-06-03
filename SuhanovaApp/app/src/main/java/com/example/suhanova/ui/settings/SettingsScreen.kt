package com.example.suhanova.ui.settings

import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.suhanova.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.suhanova.theme.*
import com.example.suhanova.ui.components.GlassCard
import com.example.suhanova.ui.components.StarFieldCanvas

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("suhanova_security", android.content.Context.MODE_PRIVATE) }

    var biometricEnabled by remember { mutableStateOf(prefs.getBoolean("biometric_enabled", false)) }
    var biometricStatus  by remember { mutableStateOf("") }
    var isTesting        by remember { mutableStateOf(false) }

    // Check availability using AndroidX BiometricManager (just for querying, no FragmentActivity needed)
    val bioManager   = BiometricManager.from(ctx)
    val canUseBio    = bioManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS

    // Uses android.hardware.biometrics.BiometricPrompt (API 28+) — works with any Context
    fun testBiometric() {
        isTesting = true
        val executor = ContextCompat.getMainExecutor(ctx)
        val cancellation = CancellationSignal()

        val prompt = BiometricPrompt.Builder(ctx)
            .setTitle("Verify Your Identity")
            .setSubtitle("Confirm to enable biometric lock")
            .setNegativeButton("Cancel", executor) { _, _ ->
                isTesting = false
                biometricStatus = "Cancelled"
            }
            .build()

        prompt.authenticate(cancellation, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    biometricEnabled = true
                    prefs.edit().putBoolean("biometric_enabled", true).apply()
                    biometricStatus  = "Biometric verified successfully!"
                    isTesting = false
                }
                override fun onAuthenticationFailed() {
                    biometricStatus = "Authentication failed — try again"
                    isTesting = false
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    biometricStatus = errString.toString()
                    isTesting = false
                }
            }
        )
    }

    Box(Modifier.fillMaxSize().background(SpaceBlack)) {
        StarFieldCanvas(Modifier.fillMaxSize())

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(28.dp))

            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextPrimary, fontWeight = FontWeight.ExtraBold,
                )
            )
            TextButton(onClick = onBack) {
                Text("‹ Back", style = MaterialTheme.typography.labelMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
            }
            Text(
                "Personalize your Suhanova experience",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            )

            // ── SECURITY ─────────────────────────────────────────────
            Text(
                "SECURITY",
                style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, letterSpacing = 2.sp),
            )

            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Biometric Lock",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary, fontWeight = FontWeight.SemiBold,
                                )
                            )
                            Text(
                                when {
                                    !canUseBio       -> "Not available on this device"
                                    biometricEnabled -> "Fingerprint / Face ID active"
                                    else             -> "Enable fingerprint or face lock"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            )
                        }
                        Switch(
                            checked  = biometricEnabled,
                            onCheckedChange = {
                                if (it && canUseBio) {
                                    testBiometric()
                                } else {
                                    biometricEnabled = false
                                    prefs.edit().putBoolean("biometric_enabled", false).apply()
                                    biometricStatus = ""
                                }
                            },
                            enabled = canUseBio,
                            colors  = SwitchDefaults.colors(
                                checkedThumbColor   = SpaceBlack,
                                checkedTrackColor   = NovaGold,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = GlassBorder,
                            ),
                        )
                    }

                    AnimatedVisibility(biometricStatus.isNotEmpty()) {
                        Text(
                            biometricStatus,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (biometricEnabled) BioGreen else ChemRed,
                            ),
                        )
                    }

                    AnimatedVisibility(biometricEnabled) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(NovaGold.copy(alpha = 0.15f), StellarPink.copy(alpha = 0.15f))))
                                .border(1.dp, NovaGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { testBiometric() }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                if (isTesting) "Verifying..." else "Test Biometric",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = NovaGold, fontWeight = FontWeight.Bold,
                                )
                            )
                        }
                    }
                }
            }

            // ── LOGO ──────────────────────────────────────────────────
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.logo_suhanova),
                    contentDescription = "Suhanova Logo",
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                    contentScale = ContentScale.Fit,
                )
            }

            // ── ABOUT ─────────────────────────────────────────────────
            Text(
                "ABOUT",
                style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, letterSpacing = 2.sp),
            )

            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsRow("App Version", "1.0.0")
                    HorizontalDivider(color = GlassBorder)
                    SettingsRow("Built for", "Suhana 💖")
                    HorizontalDivider(color = GlassBorder)
                    SettingsRow("AI Engine", "Groq llama3 + Mistral")
                    HorizontalDivider(color = GlassBorder)
                    SettingsRow("Target Exam", "Set during first launch")
                }
            }

            // ── QUOTE ─────────────────────────────────────────────────
            GlassCard {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "\"This app is specially made for you, Su. I built it because I believe in you and I know you're going to crush NEET. Go become the doctor you were meant to be. 🩺\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = StellarPink,
                            fontStyle   = FontStyle.Italic,
                            textAlign   = TextAlign.Center,
                            lineHeight  = 22.sp,
                        ),
                    )
                    Text(
                        "— Sameer, your best friend 💫",
                        style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontWeight = FontWeight.Bold),
                    )
                }
            }

            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        Text(value,  style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
    }
}
