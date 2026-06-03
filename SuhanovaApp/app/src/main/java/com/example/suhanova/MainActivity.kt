package com.example.suhanova

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.suhanova.notifications.createNotificationChannels
import com.example.suhanova.notifications.scheduleAllNotifications
import com.example.suhanova.notifications.sendNovaNotification
import com.example.suhanova.notifications.CHANNEL_NOVA
import com.example.suhanova.theme.SuhanovaTheme

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
    SuhanovaNavigation()
}
