package com.example.suhanova.ui.library

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suhanova.theme.*
import com.example.suhanova.ui.components.*
import com.example.suhanova.ui.utils.hapticClick

// ─── DATA MODELS ──────────────────────────────────────────────────────────────

data class VideoLesson(
    val id: String,
    val title: String,
    val channel: String,
    val subject: String,
    val topic: String,
    val duration: String,
    val emoji: String,
    val color: Color,
    val youtubeUrl: String,
    val difficulty: String,
)

data class StudyBook(
    val id: String,
    val title: String,
    val author: String,
    val subject: String,
    val emoji: String,
    val color: Color,
    val pdfUrl: String,
    val pages: String,
    val edition: String,
)

data class SolvedPaper(
    val id: String,
    val title: String,
    val year: String,
    val subject: String,
    val emoji: String,
    val color: Color,
    val url: String,
    val questions: String,
)

// ─── CURATED VIDEO LIBRARY ────────────────────────────────────────────────────

val FEATURED_VIDEOS = listOf(
    VideoLesson("v1", "Cell Division — Mitosis & Meiosis COMPLETE", "Vedantu NEET Made Ejaz", "Biology", "Cell Division", "1:24:13", "🧬", BioGreen,
        "https://www.youtube.com/results?search_query=mitosis+meiosis+NEET+Vedantu", "High"),
    VideoLesson("v2", "Laws of Motion — Full Chapter in 1 Hour", "Physics Wallah - Alakh Pandey", "Physics", "Laws of Motion", "58:42", "⚡", PhysBlue,
        "https://www.youtube.com/results?search_query=laws+of+motion+NEET+physics+wallah", "Medium"),
    VideoLesson("v3", "Organic Chemistry — NEET Special", "Unacademy NEET", "Chemistry", "Organic Chemistry", "2:10:05", "🧪", ChemRed,
        "https://www.youtube.com/results?search_query=organic+chemistry+NEET+unacademy", "High"),
    VideoLesson("v4", "Genetics — Mendelian Inheritance Made Easy", "NEET Biology Dr. Murad", "Biology", "Genetics", "47:18", "🧬", BioGreen,
        "https://www.youtube.com/results?search_query=genetics+mendelian+NEET+biology", "Medium"),
    VideoLesson("v5", "Optics — Ray Optics & Wave Optics Full", "Physics Wallah", "Physics", "Optics", "1:35:20", "🔦", PhysBlue,
        "https://www.youtube.com/results?search_query=optics+ray+optics+NEET+physics", "High"),
    VideoLesson("v6", "Chemical Equilibrium — Le Chatelier's", "Vedantu Chemistry", "Chemistry", "Equilibrium", "52:30", "⚖️", ChemRed,
        "https://www.youtube.com/results?search_query=chemical+equilibrium+le+chatelier+NEET", "Medium"),
    VideoLesson("v7", "Human Digestive System — Complete", "Khan Academy Medicine", "Biology", "Human Physiology", "38:15", "🫁", BioGreen,
        "https://www.youtube.com/results?search_query=human+digestive+system+NEET+biology", "Easy"),
    VideoLesson("v8", "Electrochemistry — NEET Revision", "JEE Wallah", "Chemistry", "Electrochemistry", "1:05:44", "⚡", ChemRed,
        "https://www.youtube.com/results?search_query=electrochemistry+NEET+revision", "Hard"),
)

// ─── BOOK LIBRARY ─────────────────────────────────────────────────────────────

val STUDY_BOOKS = listOf(
    StudyBook("b1", "NCERT Biology Class 11", "NCERT", "Biology", "🧬", BioGreen,
        "https://ncert.nic.in/textbook.php?kebo1=0-22", "387", "Latest"),
    StudyBook("b2", "NCERT Biology Class 12", "NCERT", "Biology", "🧬", BioGreen,
        "https://ncert.nic.in/textbook.php?lebo1=0-16", "321", "Latest"),
    StudyBook("b3", "NCERT Physics Class 11", "NCERT", "Physics", "⚡", PhysBlue,
        "https://ncert.nic.in/textbook.php?keph1=0-9", "402", "Latest"),
    StudyBook("b4", "NCERT Physics Class 12", "NCERT", "Physics", "⚡", PhysBlue,
        "https://ncert.nic.in/textbook.php?leph1=0-9", "360", "Latest"),
    StudyBook("b5", "NCERT Chemistry Class 11", "NCERT", "Chemistry", "🧪", ChemRed,
        "https://ncert.nic.in/textbook.php?kech1=0-14", "352", "Latest"),
    StudyBook("b6", "NCERT Chemistry Class 12", "NCERT", "Chemistry", "🧪", ChemRed,
        "https://ncert.nic.in/textbook.php?lech1=0-10", "388", "Latest"),
    StudyBook("b7", "DC Pandey — Optics & Modern Physics", "DC Pandey", "Physics", "🔦", PhysBlue,
        "https://www.google.com/search?q=DC+Pandey+optics+modern+physics+pdf", "544", "2024"),
    StudyBook("b8", "MS Chauhan — Organic Chemistry", "MS Chauhan", "Chemistry", "🧪", ChemRed,
        "https://www.google.com/search?q=MS+Chauhan+organic+chemistry+NEET+pdf", "620", "2024"),
)

// ─── PREVIOUS YEAR PAPERS ─────────────────────────────────────────────────────

val SOLVED_PAPERS = listOf(
    SolvedPaper("p1", "NEET 2024 — Full Paper + Solutions", "2024", "All", "📋", NovaGold,
        "https://www.google.com/search?q=NEET+2024+question+paper+with+solutions+pdf", "200 Q"),
    SolvedPaper("p2", "NEET 2023 — Full Paper + Solutions", "2023", "All", "📋", NovaGold,
        "https://www.google.com/search?q=NEET+2023+question+paper+with+solutions+pdf", "200 Q"),
    SolvedPaper("p3", "NEET 2022 — Full Paper + Solutions", "2022", "All", "📋", NovaGold,
        "https://www.google.com/search?q=NEET+2022+question+paper+with+solutions+pdf", "200 Q"),
    SolvedPaper("p4", "NEET 2021 — Full Paper + Solutions", "2021", "All", "📋", NovaGold,
        "https://www.google.com/search?q=NEET+2021+question+paper+with+solutions+pdf", "200 Q"),
    SolvedPaper("p5", "NEET Biology 2024 — Chapter Wise", "2024", "Biology", "🧬", BioGreen,
        "https://www.google.com/search?q=NEET+2024+biology+chapter+wise+questions+solutions", "90 Q"),
    SolvedPaper("p6", "NEET Physics 2024 — Chapter Wise", "2024", "Physics", "⚡", PhysBlue,
        "https://www.google.com/search?q=NEET+2024+physics+chapter+wise+questions+solutions", "45 Q"),
    SolvedPaper("p7", "NEET Chemistry 2024 — Chapter Wise", "2024", "Chemistry", "🧪", ChemRed,
        "https://www.google.com/search?q=NEET+2024+chemistry+chapter+wise+questions+solutions", "45 Q"),
    SolvedPaper("p8", "AIIMS Previous Papers 2019–2023", "2019–23", "All", "🏥", StellarPink,
        "https://www.google.com/search?q=AIIMS+MBBS+previous+year+papers+with+solutions+pdf", "120 Q"),
)

// ─── LIBRARY SCREEN ───────────────────────────────────────────────────────────

val LIBRARY_TABS = listOf("Videos 🎥", "Books 📖", "Papers 📋")

@Composable
fun LibraryScreen() {
    val ctx       = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var activeTab  by remember { mutableStateOf("Videos 🎥") }
    var searchQuery by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(SpaceBlack)) {
        // Header
        Column(Modifier.padding(horizontal = 20.dp).padding(top = 24.dp)) {
            Text("Library 📚",
                style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary, fontWeight = FontWeight.ExtraBold))
            Text("NEET resources curated just for you, Suhana 🩺",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        }

        Spacer(Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery, onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            placeholder = { Text("Search videos, books, chapters... 🔍", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = NovaGold, unfocusedBorderColor = GlassBorder,
                cursorColor           = NovaGold, focusedTextColor = TextPrimary,
                unfocusedTextColor    = TextPrimary, focusedContainerColor = GlassBg,
                unfocusedContainerColor = GlassBg,
            ),
            shape    = RoundedCornerShape(14.dp),
            maxLines = 1,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    TextButton(onClick = { searchQuery = "" }) {
                        Text("✕", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                    }
                }
            }
        )

        Spacer(Modifier.height(10.dp))

        // Tabs
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.04f)).padding(4.dp)
        ) {
            LIBRARY_TABS.forEach { tab ->
                val selected = activeTab == tab
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "libTab$tab")
                val bgAlpha by animateFloatAsState(if (selected) 1f else 0f, tween(200), label = "libTabBg$tab")

                Box(
                    Modifier.weight(1f).scale(scale).clip(RoundedCornerShape(10.dp))
                        .background(NovaGold.copy(alpha = bgAlpha))
                        .clickable(interactionSource, null) { hapticClick(ctx); activeTab = tab }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(tab, style = MaterialTheme.typography.labelSmall.copy(
                        color     = if (selected) SpaceBlack else TextMuted,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    ))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Tab content with animation
        AnimatedContent(activeTab, label = "libTab",
            transitionSpec = { (fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 6 }) togetherWith (fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { -it / 6 }) }
        ) { tab ->
            when (tab) {
                "Videos 🎥" -> VideosTab(searchQuery, onOpen = { uriHandler.openUri(it) })
                "Books 📖"  -> BooksTab(searchQuery, onOpen = { uriHandler.openUri(it) })
                "Papers 📋" -> PapersTab(searchQuery, onOpen = { uriHandler.openUri(it) })
            }
        }
    }
}

// ─── VIDEOS TAB ───────────────────────────────────────────────────────────────

@Composable
fun VideosTab(searchQuery: String, onOpen: (String) -> Unit) {
    val ctx = LocalContext.current
    val filtered = FEATURED_VIDEOS.filter {
        searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) ||
        it.subject.contains(searchQuery, ignoreCase = true) || it.topic.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 110.dp)) {
        item {
            Text("CURATED FOR YOUR WEAK AREAS",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 2.sp))
        }
        items(filtered, key = { it.id }) { video ->
            VideoCard(video, onOpen = { hapticClick(ctx); onOpen(video.youtubeUrl) })
        }
    }
}

@Composable
fun VideoCard(video: VideoLesson, onOpen: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "vid${video.id}")
    val infTrans = rememberInfiniteTransition(label = "vid${video.id}")
    val glow by infTrans.animateFloat(0.06f, 0.18f, infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse), "vGlow${video.id}")

    Row(
        Modifier.fillMaxWidth().scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg)
            .border(1.dp, video.color.copy(alpha = glow + 0.15f), RoundedCornerShape(16.dp))
            .clickable(interactionSource, null) { onOpen() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail placeholder
        Box(
            Modifier.size(62.dp).clip(RoundedCornerShape(10.dp))
                .background(video.color.copy(alpha = 0.12f))
                .border(1.dp, video.color.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(video.emoji, fontSize = 22.sp)
                Text("▶", style = MaterialTheme.typography.labelSmall.copy(color = video.color, fontSize = 10.sp))
            }
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(video.title, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp),
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(video.channel, style = MaterialTheme.typography.labelSmall.copy(color = video.color))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip(video.duration, TextMuted)
                Chip(video.difficulty, when (video.difficulty) { "Easy" -> BioGreen; "Hard" -> ChemRed; else -> NovaGold })
            }
        }
        Text("›", style = MaterialTheme.typography.headlineMedium.copy(color = NovaGold))
    }
}

// ─── BOOKS TAB ────────────────────────────────────────────────────────────────

@Composable
fun BooksTab(searchQuery: String, onOpen: (String) -> Unit) {
    val ctx = LocalContext.current
    val filtered = STUDY_BOOKS.filter {
        searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.subject.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 110.dp)) {
        item {
            GlassCard {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📌", fontSize = 20.sp)
                    Column {
                        Text("NCERT First — Always!", style = MaterialTheme.typography.labelMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                        Text("70% of NEET questions come directly from NCERT. Read every line.", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp))
                    }
                }
            }
        }
        items(filtered, key = { it.id }) { book ->
            BookCard(book, onOpen = { hapticClick(ctx); onOpen(book.pdfUrl) })
        }
    }
}

@Composable
fun BookCard(book: StudyBook, onOpen: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "book${book.id}")

    Row(
        Modifier.fillMaxWidth().scale(scale)
            .clip(RoundedCornerShape(16.dp)).background(GlassBg)
            .border(1.dp, book.color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(interactionSource, null) { onOpen() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        // Book cover
        Box(
            Modifier.size(52.dp, 68.dp).clip(RoundedCornerShape(6.dp))
                .background(book.color.copy(alpha = 0.1f))
                .border(2.dp, book.color.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(book.emoji, fontSize = 22.sp) }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(book.title, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp))
            Text(book.author, style = MaterialTheme.typography.labelSmall.copy(color = book.color))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("${book.pages} pages", TextMuted)
                Chip(book.edition, book.color)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📥", fontSize = 18.sp)
            Text("Open", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontSize = 9.sp))
        }
    }
}

// ─── PAPERS TAB ───────────────────────────────────────────────────────────────

@Composable
fun PapersTab(searchQuery: String, onOpen: (String) -> Unit) {
    val ctx = LocalContext.current
    val filtered = SOLVED_PAPERS.filter {
        searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.year.contains(searchQuery)
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 110.dp)) {
        item {
            GlassCard {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 20.sp)
                    Column {
                        Text("Solve PYQs Daily!", style = MaterialTheme.typography.labelMedium.copy(color = StellarPink, fontWeight = FontWeight.Bold))
                        Text("Previous Year Questions are the closest prediction of what NEET will ask. Solve 2024, 2023, 2022 minimum.", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp))
                    }
                }
            }
        }
        items(filtered, key = { it.id }) { paper ->
            PaperCard(paper, onOpen = { hapticClick(ctx); onOpen(paper.url) })
        }
    }
}

@Composable
fun PaperCard(paper: SolvedPaper, onOpen: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "paper${paper.id}")

    Row(
        Modifier.fillMaxWidth().scale(scale)
            .clip(RoundedCornerShape(16.dp)).background(GlassBg)
            .border(1.dp, paper.color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(interactionSource, null) { onOpen() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(50.dp).clip(RoundedCornerShape(10.dp))
                .background(paper.color.copy(alpha = 0.12f))
                .border(1.dp, paper.color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(paper.emoji, fontSize = 20.sp) }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(paper.title, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("NEET ${paper.year}", paper.color)
                Chip(paper.questions, TextMuted)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📋", fontSize = 18.sp)
            Text("Solve", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontSize = 9.sp))
        }
    }
}
