package com.example.suhanova

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.suhanova.theme.SpaceBlack
import com.example.suhanova.ui.chat.NovaChatScreen
import com.example.suhanova.ui.components.NovaFAB
import com.example.suhanova.ui.components.SuhanovaBottomNav
import com.example.suhanova.ui.home.HomeScreen
import com.example.suhanova.ui.library.LibraryScreen
import com.example.suhanova.ui.progress.ProgressScreen
import com.example.suhanova.ui.quiz.QuizScreen
import com.example.suhanova.ui.rewards.RewardsScreen
import com.example.suhanova.ui.roadmap.RoadmapScreen
import com.example.suhanova.ui.study.StudyScreen
import com.example.suhanova.ui.settings.SettingsScreen

@Composable
fun SuhanovaNavigation() {
    val backStack = rememberNavBackStack(Home)

    val currentRoute by remember {
        derivedStateOf {
            when (backStack.lastOrNull()) {
                is Home     -> "home"
                is Study    -> "study"
                is Quiz     -> "quiz"
                is Progress -> "progress"
                is Rewards  -> "rewards"
                is Library  -> "library"
                is Roadmap  -> "roadmap"
                is NovaChat -> "nova"
                is Settings -> "settings"
                else        -> "home"
            }
        }
    }

    val showBottomBar = currentRoute != "nova"
    val showFab       = currentRoute != "nova"

    fun navigateTo(route: String) {
        if (route == "nova") {
            backStack.add(NovaChat)
            return
        }
        // Clear to root then navigate
        while (backStack.size > 1) backStack.removeLast()
        val currentKey = backStack.lastOrNull()
        val alreadyThere = when (route) {
            "home"     -> currentKey is Home
            "study"    -> currentKey is Study
            "quiz"     -> currentKey is Quiz
            "progress" -> currentKey is Progress
            "rewards"  -> currentKey is Rewards
            "library"  -> currentKey is Library
            "roadmap"  -> currentKey is Roadmap
            else       -> false
        }
        if (!alreadyThere) {
            while (backStack.isNotEmpty()) backStack.removeLast()
            when (route) {
                "home"     -> backStack.add(Home)
                "study"    -> backStack.add(Study)
                "quiz"     -> backStack.add(Quiz)
                "progress" -> backStack.add(Progress)
                "rewards"  -> backStack.add(Rewards)
                "library"  -> backStack.add(Library)
                "roadmap"  -> backStack.add(Roadmap)
                "settings" -> backStack.add(Settings)
                else       -> backStack.add(Home)
            }
        }
    }

    Scaffold(
        containerColor = SpaceBlack,
        bottomBar = {
            AnimatedVisibility(
                showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit  = slideOutVertically { it } + fadeOut()
            ) {
                SuhanovaBottomNav(currentRoute, ::navigateTo)
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            NavDisplay(
                backStack = backStack,
                onBack    = { if (backStack.size > 1) backStack.removeLast() },
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 8 }) togetherWith
                    (fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 8 })
                },
                popTransitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 8 }) togetherWith
                    (fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 8 })
                },
                entryProvider = { key ->
                    when (key) {
                        is Home     -> NavEntry(key) { HomeScreen(onNavigate = ::navigateTo) }
                        is Study    -> NavEntry(key) { StudyScreen(onNavigate = ::navigateTo) }
                        is Quiz     -> NavEntry(key) { QuizScreen(onNavigate  = ::navigateTo) }
                        is Progress -> NavEntry(key) { ProgressScreen() }
                        is Rewards  -> NavEntry(key) { RewardsScreen() }
                        is Library  -> NavEntry(key) { LibraryScreen() }
                        is Roadmap  -> NavEntry(key) { RoadmapScreen() }
                        is NovaChat -> NavEntry(key) { NovaChatScreen() }
                        is Settings -> NavEntry(key) { SettingsScreen() }
                        else        -> NavEntry(Home) { HomeScreen(onNavigate = ::navigateTo) }
                    }
                }
            )

            // Nova FAB
            AnimatedVisibility(
                visible  = showFab,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 88.dp),
                enter    = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit     = scaleOut() + fadeOut(),
            ) { NovaFAB(onClick = { navigateTo("nova") }) }
        }
    }
}
