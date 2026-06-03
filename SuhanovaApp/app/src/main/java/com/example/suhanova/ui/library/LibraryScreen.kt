package com.example.suhanova.ui.library

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.suhanova.data.EducationCatalogItem
import com.example.suhanova.data.SuhanovaDatabase
import com.example.suhanova.theme.BioGreen
import com.example.suhanova.theme.ChemRed
import com.example.suhanova.theme.GlassBg
import com.example.suhanova.theme.GlassBorder
import com.example.suhanova.theme.MathGold
import com.example.suhanova.theme.NovaGold
import com.example.suhanova.theme.PhysBlue
import com.example.suhanova.theme.SpaceBlack
import com.example.suhanova.theme.StellarPink
import com.example.suhanova.theme.TextMuted
import com.example.suhanova.theme.TextPrimary
import com.example.suhanova.theme.TextSecondary
import com.example.suhanova.ui.components.Chip
import com.example.suhanova.ui.components.GlassCard
import com.example.suhanova.ui.components.PageBackButton
import com.example.suhanova.ui.components.StarFieldCanvas
import java.net.URLEncoder

@Composable
fun LibraryScreen(onBack: () -> Unit = {}) {
    val ctx = LocalContext.current
    val db = remember { SuhanovaDatabase.getDatabase(ctx) }
    val setupPrefs = remember { ctx.getSharedPreferences("suhanova_first_run", android.content.Context.MODE_PRIVATE) }
    val board = remember { setupPrefs.getString("board", "").orEmpty() }
    val studentClass = remember { setupPrefs.getString("student_class", "").orEmpty() }
    val targetExam = remember { setupPrefs.getString("target_exam", "Exam").orEmpty() }

    val subjects by db.educationCatalogDao().getSubjects(board, studentClass).collectAsStateWithLifecycle(emptyList())
    var selectedSubject by remember(subjects) { mutableStateOf(subjects.firstOrNull().orEmpty()) }
    val catalog by db.educationCatalogDao().getAll().collectAsStateWithLifecycle(emptyList())

    val visibleItems = remember(catalog, board, studentClass, selectedSubject) {
        catalog.filter {
            (board.isBlank() || it.board.equals(board, ignoreCase = true)) &&
                (studentClass.isBlank() || it.studentClass.equals(studentClass, ignoreCase = true)) &&
                (selectedSubject.isBlank() || it.subject == selectedSubject)
        }
    }

    fun openSearch(query: String) {
        val encoded = URLEncoder.encode(query, "UTF-8")
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")))
    }

    LazyColumn(
        Modifier.fillMaxSize().background(SpaceBlack).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
    ) {
        item {
            Text(
                "Library",
                style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary, fontWeight = FontWeight.ExtraBold),
            )
            PageBackButton(onClick = onBack)
            Text(
                listOf(studentClass, board, targetExam).filter { it.isNotBlank() }.joinToString(" · ")
                    .ifBlank { "Resources adapt after setup" },
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
            )
        }

        if (subjects.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SUBJECTS", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, letterSpacing = 2.sp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        subjects.take(4).forEach { subject ->
                            Chip(
                                text = subject,
                                color = subjectColor(subject),
                                onClick = { selectedSubject = subject },
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard(glowColor = NovaGold) {
                Text("SMART SEARCH", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                Text(
                    "This library uses the education database for your board and class, then opens fresh resources for each chapter.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                )
            }
        }

        if (visibleItems.isEmpty()) {
            item {
                GlassCard {
                    Text("No catalog found yet", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Try setting your board/class again, or use Study and Quiz with any topic manually.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    )
                }
            }
        } else {
            items(visibleItems, key = { it.id }) { item ->
                CatalogCard(
                    item = item,
                    onOpenNotes = { openSearch("${item.resourceQuery} notes") },
                    onOpenQuestions = { openSearch("${item.resourceQuery} important questions") },
                    onOpenVideos = { openSearch("${item.resourceQuery} video lecture") },
                )
            }
        }
    }
}

@Composable
private fun CatalogCard(
    item: EducationCatalogItem,
    onOpenNotes: () -> Unit,
    onOpenQuestions: () -> Unit,
    onOpenVideos: () -> Unit,
) {
    val color = subjectColor(item.subject)
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.chapter, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                Text("${item.subject} · ${item.board} ${item.studentClass}", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
            }
            Text(subjectEmoji(item.subject), style = MaterialTheme.typography.headlineSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResourceButton("Notes", color, onOpenNotes)
            ResourceButton("Questions", NovaGold, onOpenQuestions)
            ResourceButton("Videos", StellarPink, onOpenVideos)
        }
    }
}

@Composable
private fun ResourceButton(label: String, color: Color, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold),
    )
}

private fun subjectEmoji(subject: String): String = when {
    subject.contains("bio", true) -> "🧬"
    subject.contains("physics", true) -> "⚡"
    subject.contains("chem", true) -> "🧪"
    subject.contains("math", true) -> "📐"
    subject.contains("english", true) -> "📘"
    subject.contains("social", true) -> "🌍"
    subject.contains("computer", true) -> "💻"
    subject.contains("science", true) -> "🔬"
    else -> "✨"
}

private fun subjectColor(subject: String): Color = when {
    subject.contains("bio", true) || subject.contains("science", true) -> BioGreen
    subject.contains("physics", true) || subject.contains("english", true) -> PhysBlue
    subject.contains("chem", true) -> ChemRed
    subject.contains("math", true) -> MathGold
    subject.contains("social", true) -> NovaGold
    else -> StellarPink
}
