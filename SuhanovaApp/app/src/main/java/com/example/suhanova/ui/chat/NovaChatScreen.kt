package com.example.suhanova.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suhanova.network.*
import com.example.suhanova.theme.*
import com.example.suhanova.ui.components.*
import com.example.suhanova.ui.utils.hapticClick
import com.example.suhanova.ui.utils.hapticSuccess
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class UiMessage(
    val id: Long = System.currentTimeMillis() + (0..99999).random(),
    val role: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
)

val QUICK_ASKS = listOf(
    "I'm confused about osmosis 💧",
    "Explain Newton's 3rd Law ⚡",
    "I keep forgetting Le Chatelier 🧪",
    "What is mitosis vs meiosis? 🧬",
    "I'm stressed about NEET 😔",
    "Explain DNA replication 🧬",
    "Help me with organic reactions 🧪",
    "I don't understand electrostatics ⚡",
)

val WEAK_TOPIC_ASKS = listOf(
    "📌 My weak area: Membrane Transport",
    "📌 My weak area: Thermodynamics",
    "📌 My weak area: Organic Reactions",
)

fun formatTime(ts: Long): String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(ts))

@Composable
fun NovaChatScreen() {
    val ctx        = LocalContext.current
    val repository = remember { NovaBestFriendRepository() }
    val scope      = rememberCoroutineScope()
    val listState  = rememberLazyListState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    // Initial welcome from Nova as best friend
    val welcomeText = remember {
        when {
            hour in 0..3  -> "Hey Suhana! 🌙 Midnight study session? Honestly, that's iconic. I'm here. Ask me anything — I won't judge, I'll just explain. What are we tackling?"
            hour in 4..11 -> "Good morning, Dr. Suhana! ☀️ You opened me first thing — that's the energy of a topper. What topic are we cracking today? I'm ready when you are! 🌟"
            hour in 12..16 -> "Hey! 👋 Afternoon session — this is when most people slack off, but not you. What's confusing you? Let's sort it out together!"
            else           -> "Evening, Suhana! 🌆 Prime study time. Give me your toughest question and I'll break it down like your smartest friend would. Let's go!"
        }
    }

    var messages by remember {
        mutableStateOf(listOf(UiMessage(role = "nova", text = welcomeText)))
    }
    var conversationHistory by remember { mutableStateOf(listOf<GroqMessage>()) }
    var inputText  by remember { mutableStateOf("") }
    var isTyping   by remember { mutableStateOf(false) }
    var chatMode   by remember { mutableStateOf("bestfriend") } // "bestfriend" | "tutor"

    fun sendMessage(text: String) {
        if (text.isBlank() || isTyping) return
        hapticClick(ctx)
        val userMsg = UiMessage(role = "user", text = text)
        messages = messages + userMsg
        conversationHistory = conversationHistory + GroqMessage("user", text)
        isTyping = true

        scope.launch {
            listState.animateScrollToItem(messages.size)

            val result = repository.askBestFriend(text, conversationHistory)

            result.fold(
                onSuccess = { reply ->
                    hapticSuccess(ctx)
                    messages = messages + UiMessage(role = "nova", text = reply)
                    conversationHistory = conversationHistory + GroqMessage("assistant", reply)
                },
                onFailure = { err ->
                    messages = messages + UiMessage(role = "nova", text = "⚠️ ${err.message}", isError = true)
                }
            )
            isTyping = false
            listState.animateScrollToItem(messages.size)
        }
    }

    Box(Modifier.fillMaxSize().background(SpaceBlack)) {
        StarFieldCanvas(Modifier.fillMaxSize())

        Column(Modifier.fillMaxSize()) {
            // ── Header ──────────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().background(SpaceBlack.copy(alpha = 0.92f))
                    .border(BorderStroke(1.dp, GlassBorder))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                // Nova avatar with pulsing glow
                val infTrans = rememberInfiniteTransition(label = "novaAvatar")
                val avatarPulse by infTrans.animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse), "avatarPulse")
                Box(
                    Modifier.size(46.dp).scale(avatarPulse).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NovaGold, StellarPink))),
                    contentAlignment = Alignment.Center,
                ) { Text("✨", fontSize = 20.sp) }

                Column(Modifier.weight(1f)) {
                    Text("Nova — Your Best Friend",
                        style = MaterialTheme.typography.titleMedium.copy(color = NovaGold, fontWeight = FontWeight.ExtraBold))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(BioGreen))
                        Text("Groq llama3 · Ready for anything 💬", style = MaterialTheme.typography.bodySmall.copy(color = BioGreen))
                    }
                }

                // Mode toggle: Best Friend vs Tutor
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "modeScale")
                Box(
                    Modifier.scale(scale).clip(CircleShape)
                        .background(if (chatMode == "bestfriend") StellarPink.copy(alpha = 0.2f) else NovaGold.copy(alpha = 0.2f))
                        .border(1.dp, if (chatMode == "bestfriend") StellarPink else NovaGold, CircleShape)
                        .clickable(interactionSource, null) {
                            hapticClick(ctx)
                            chatMode = if (chatMode == "bestfriend") "tutor" else "bestfriend"
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(if (chatMode == "bestfriend") "👭 Friend" else "📚 Tutor",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (chatMode == "bestfriend") StellarPink else NovaGold,
                            fontWeight = FontWeight.Bold,
                        ))
                }

                TextButton(onClick = {
                    hapticClick(ctx)
                    messages = listOf(UiMessage(role = "nova", text = "Fresh start! 🌟 What are we conquering today, Suhana?"))
                    conversationHistory = emptyList()
                }) { Text("Clear", style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)) }
            }

            // ── Messages ────────────────────────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { if (msg.role == "nova") -20 else 20 },
                    ) {
                        ChatBubble(msg)
                    }
                }
                if (isTyping) {
                    item(key = "typing") {
                        AnimatedVisibility(true, enter = fadeIn() + slideInVertically { 20 }) {
                            TypingIndicator()
                        }
                    }
                }
            }

            // ── Weak Topics Quick-Ask ────────────────────────────────────────────
            LazyRow(Modifier.padding(horizontal = 14.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(WEAK_TOPIC_ASKS) { q ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "wt_$q")
                    Box(
                        Modifier.scale(scale).clip(CircleShape)
                            .background(StellarPink.copy(alpha = 0.12f)).border(1.dp, StellarPink.copy(alpha = 0.45f), CircleShape)
                            .clickable(interactionSource, null) { sendMessage(q) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text(q, style = MaterialTheme.typography.labelSmall.copy(color = StellarPink, fontWeight = FontWeight.SemiBold)) }
                }
            }

            // ── Quick Asks ──────────────────────────────────────────────────────
            LazyRow(Modifier.padding(horizontal = 14.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(QUICK_ASKS) { q ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "qa_$q")
                    Box(
                        Modifier.scale(scale).clip(CircleShape)
                            .background(NovaGold.copy(alpha = 0.1f)).border(1.dp, NovaGold.copy(alpha = 0.35f), CircleShape)
                            .clickable(interactionSource, null) { sendMessage(q) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text(q, style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontWeight = FontWeight.SemiBold)) }
                }
            }

            // ── Input Bar ───────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().background(SpaceBlack.copy(alpha = 0.95f))
                    .border(BorderStroke(1.dp, GlassBorder)).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom,
            ) {
                OutlinedTextField(
                    value = inputText, onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask Nova anything... 💭", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor    = NovaGold,
                        unfocusedBorderColor  = GlassBorder,
                        cursorColor           = NovaGold,
                        focusedTextColor      = TextPrimary,
                        unfocusedTextColor    = TextPrimary,
                        focusedContainerColor = GlassBg,
                        unfocusedContainerColor = GlassBg,
                    ),
                    shape   = RoundedCornerShape(14.dp),
                    maxLines = 4,
                )
                val canSend = inputText.isNotBlank() && !isTyping
                val infTrans2 = rememberInfiniteTransition(label = "sendBtn")
                val sendGlow by infTrans2.animateFloat(0.4f, 0.9f, infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse), "sendGlow")

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed && canSend) 0.86f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "sendScale")

                Box(
                    Modifier.size(50.dp).scale(scale).clip(CircleShape)
                        .background(
                            if (canSend) Brush.linearGradient(listOf(NovaGold, StellarPink))
                            else Brush.linearGradient(listOf(TextMuted.copy(alpha = 0.3f), TextMuted.copy(alpha = 0.3f)))
                        )
                        
                        .clickable(interactionSource, null, enabled = canSend) {
                            val text = inputText.trim(); inputText = ""; sendMessage(text)
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("›", style = MaterialTheme.typography.headlineLarge.copy(color = if (canSend) SpaceBlack else TextMuted, fontWeight = FontWeight.ExtraBold)) }
            }
        }
    }
}

@Composable
fun ChatBubble(message: UiMessage) {
    val isNova = message.role == "nova"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isNova) Arrangement.Start else Arrangement.End) {
        Column(
            Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isNova) Alignment.Start else Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (isNova) {
                Text("✨ Nova · ${formatTime(message.timestamp)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp,
                        bottomStart = if (isNova) 4.dp else 20.dp,
                        bottomEnd   = if (isNova) 20.dp else 4.dp,
                    ))
                    .background(if (isNova) GlassBg else StellarPink.copy(alpha = 0.12f))
                    .border(
                        1.dp,
                        if (message.isError) ChemRed.copy(alpha = 0.5f)
                        else if (isNova) GlassBorder
                        else StellarPink.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp),
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isNova) {
                        Text("🌟", fontSize = 14.sp)
                    }
                    Text(message.text, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, lineHeight = 22.sp))
                }
            }
            if (!isNova) {
                Text("Suhana · ${formatTime(message.timestamp)}", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infTrans = rememberInfiniteTransition(label = "typing")
    Row(
        Modifier.clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
            .background(GlassBg).border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        Arrangement.spacedBy(5.dp), Alignment.CenterVertically,
    ) {
        repeat(3) { i ->
            val offsetY by infTrans.animateFloat(0f, -9f, infiniteRepeatable(tween(500, delayMillis = i * 150, easing = EaseInOutSine), RepeatMode.Reverse), "dot$i")
            Box(Modifier.size(8.dp).offset(y = offsetY.dp).clip(CircleShape).background(NovaGold))
        }
    }
}


