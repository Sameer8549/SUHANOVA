package com.example.suhanova.ui.roadmap

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suhanova.network.*
import com.example.suhanova.theme.*
import com.example.suhanova.ui.components.*
import com.example.suhanova.ui.utils.hapticClick
import com.example.suhanova.ui.utils.hapticSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── DATA MODELS ──────────────────────────────────────────────────────────────

enum class NodeStatus { LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED }

data class RoadmapNode(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val color: Color,
    val status: NodeStatus,
    val daysNeeded: Int,
    val accuracy: Int,           // Suhana's current accuracy (0 = not started)
    val xpReward: Int,
    val children: List<String>,  // IDs of nodes this unlocks
    val isWeakArea: Boolean = false,
    val tips: List<String> = emptyList(),
)

data class RoadmapPath(val from: String, val to: String)

// ─── FULL NEET ROADMAP DATA ────────────────────────────────────────────────────

val ROADMAP_NODES = listOf(

    // ── FOUNDATION (always available) ──
    RoadmapNode("foundation", "Foundation", "Start your NEET journey", "🌟", NovaGold,
        NodeStatus.COMPLETED, 0, 100, 0,
        listOf("bio_cell", "phy_motion", "chem_atomic"),
        tips = listOf("You've begun. That's the hardest step.")
    ),

    // ── BIOLOGY BRANCH ──
    RoadmapNode("bio_cell", "Cell Biology", "Cell structure, organelles, division", "🧬", BioGreen,
        NodeStatus.COMPLETED, 7, 78, 500,
        listOf("bio_biomol"),
        isWeakArea = false,
        tips = listOf("NCERT Ch 8-10", "Draw the cell — visualize it", "~15 Qs in NEET every year")
    ),
    RoadmapNode("bio_biomol", "Biomolecules", "Carbs, Proteins, Lipids, Nucleic Acids", "🔬", BioGreen,
        NodeStatus.IN_PROGRESS, 5, 65, 400,
        listOf("bio_celldiv"),
        tips = listOf("Enzyme kinetics — must know Km/Vmax", "DNA structure always comes in NEET")
    ),
    RoadmapNode("bio_celldiv", "Cell Division", "Mitosis, Meiosis, Cell Cycle", "🔄", BioGreen,
        NodeStatus.AVAILABLE, 4, 0, 450,
        listOf("bio_plant", "bio_genetics"),
        isWeakArea = false,
        tips = listOf("PMAT mnemonic", "Meiosis crossing over = variation", "~8 Qs per year")
    ),
    RoadmapNode("bio_plant", "Plant Physiology", "Photosynthesis, Respiration, Transport", "🌱", BioGreen,
        NodeStatus.LOCKED, 6, 0, 500,
        listOf("bio_human"),
        isWeakArea = false,
        tips = listOf("C3 vs C4 — very common in NEET", "Krebs cycle steps with ATP count")
    ),
    RoadmapNode("bio_genetics", "Genetics & Evolution", "Mendelian, Molecular, Evolution", "🧬", BioGreen,
        NodeStatus.LOCKED, 8, 52, 600,
        listOf("bio_human"),
        isWeakArea = true,
        tips = listOf("Mendel's laws — 5+ Qs", "DNA replication enzymes by name", "Hardy-Weinberg in MCQ form")
    ),
    RoadmapNode("bio_membrane", "Membrane Transport", "Osmosis, Active/Passive, Pumps", "💧", BioGreen,
        NodeStatus.AVAILABLE, 3, 35, 350,
        listOf("bio_plant"),
        isWeakArea = true,
        tips = listOf("Na+/K+ ATPase mechanism", "Osmosis direction — dilute to concentrated", "MOST MISSED topic — fix this first!")
    ),
    RoadmapNode("bio_human", "Human Physiology", "Digestion, Circulation, Excretion, Neural", "🫁", BioGreen,
        NodeStatus.LOCKED, 10, 0, 700,
        listOf("bio_reproduce"),
        tips = listOf("~25 Qs from this unit alone!", "Draw digestion diagram by memory", "ECG waveform = guaranteed question")
    ),
    RoadmapNode("bio_reproduce", "Reproduction", "Human, Plant, Reproductive Health", "🌸", BioGreen,
        NodeStatus.LOCKED, 7, 0, 550,
        listOf("bio_ecology"),
        tips = listOf("Gametogenesis differences", "Placenta hormones", "Contraception mechanisms")
    ),
    RoadmapNode("bio_ecology", "Ecology & Environment", "Ecosystems, Biodiversity, Pollution", "🌍", BioGreen,
        NodeStatus.LOCKED, 5, 0, 450,
        listOf("bio_neet_ready"),
        tips = listOf("~10 Qs per year", "Carbon cycle must memorize", "Pyramid of numbers vs biomass")
    ),
    RoadmapNode("bio_neet_ready", "Biology NEET Ready! 🩺", "All Biology chapters complete", "🏆", NovaGold,
        NodeStatus.LOCKED, 0, 0, 2000,
        listOf(),
        tips = listOf("You're a Biology champion, Suhana!")
    ),

    // ── PHYSICS BRANCH ──
    RoadmapNode("phy_motion", "Kinematics", "Motion, Velocity, Acceleration, Projectile", "🚀", PhysBlue,
        NodeStatus.COMPLETED, 5, 82, 400,
        listOf("phy_laws"),
        tips = listOf("Equations of motion — derive, don't memorize", "Projectile: max height = v²sin²θ/2g")
    ),
    RoadmapNode("phy_laws", "Laws of Motion", "Newton's 3 Laws, Friction, Circular", "⚡", PhysBlue,
        NodeStatus.COMPLETED, 5, 79, 400,
        listOf("phy_work"),
        tips = listOf("Free Body Diagram for every problem", "Pseudo force in non-inertial frames")
    ),
    RoadmapNode("phy_work", "Work, Energy & Power", "Work-Energy theorem, Conservation", "⚡", PhysBlue,
        NodeStatus.IN_PROGRESS, 4, 71, 400,
        listOf("phy_waves", "phy_thermal"),
        tips = listOf("Work-Energy theorem shortcut", "Elastic vs inelastic collision")
    ),
    RoadmapNode("phy_waves", "Waves & Sound", "SHM, Wave properties, Doppler", "〜", PhysBlue,
        NodeStatus.AVAILABLE, 5, 0, 450,
        listOf("phy_optics"),
        tips = listOf("SHM equations — angular frequency", "Beats formula: n₁-n₂")
    ),
    RoadmapNode("phy_optics", "Optics", "Ray Optics, Wave Optics, Lens", "🔦", PhysBlue,
        NodeStatus.AVAILABLE, 6, 48, 500,
        listOf("phy_modern"),
        isWeakArea = true,
        tips = listOf("Mirror formula: 1/f = 1/v + 1/u", "Young's double slit — fringe width", "~8 Qs every year")
    ),
    RoadmapNode("phy_thermal", "Thermodynamics", "Heat, Laws of Thermo, Kinetic Theory", "🌡️", PhysBlue,
        NodeStatus.AVAILABLE, 5, 40, 450,
        listOf("phy_electro"),
        isWeakArea = true,
        tips = listOf("First law: ΔU = Q - W", "Carnot efficiency formula", "Specific heat ratios γ")
    ),
    RoadmapNode("phy_electro", "Electrostatics & Current", "Coulomb, Capacitors, Circuits, EMF", "⚡", PhysBlue,
        NodeStatus.LOCKED, 8, 55, 600,
        listOf("phy_magnetics"),
        isWeakArea = true,
        tips = listOf("Capacitor energy = ½CV²", "Kirchhoff's laws — 2+ Qs", "Wheatstone bridge balance condition")
    ),
    RoadmapNode("phy_magnetics", "Magnetism & EMI", "Biot-Savart, Faraday, AC Circuits", "🧲", PhysBlue,
        NodeStatus.LOCKED, 7, 0, 550,
        listOf("phy_modern"),
        tips = listOf("Lenz's law — direction of induced current", "Transformer: Ns/Np = Vs/Vp")
    ),
    RoadmapNode("phy_modern", "Modern Physics", "Photoelectric, Atomic Models, Nuclear", "☢️", PhysBlue,
        NodeStatus.LOCKED, 6, 0, 600,
        listOf("phy_neet_ready"),
        tips = listOf("Bohr model energy levels", "Photoelectric: KE = hν - φ", "Radioactive decay law")
    ),
    RoadmapNode("phy_neet_ready", "Physics NEET Ready! ⚡", "All Physics chapters complete", "🏆", NovaGold,
        NodeStatus.LOCKED, 0, 0, 2000,
        listOf(),
        tips = listOf("Physics champion, Suhana! ⚡")
    ),

    // ── CHEMISTRY BRANCH ──
    RoadmapNode("chem_atomic", "Atomic Structure", "Quantum numbers, Orbitals, Aufbau", "⚛️", ChemRed,
        NodeStatus.COMPLETED, 4, 85, 350,
        listOf("chem_bonding"),
        tips = listOf("Quantum numbers ranges by heart", "Electronic configuration exceptions: Cr, Cu")
    ),
    RoadmapNode("chem_bonding", "Chemical Bonding", "VSEPR, Hybridization, MOT", "🔗", ChemRed,
        NodeStatus.COMPLETED, 5, 80, 400,
        listOf("chem_states"),
        tips = listOf("Hybridization: sp sp2 sp3 sp3d sp3d2", "Bond angles — draw the shape")
    ),
    RoadmapNode("chem_states", "States of Matter", "Gas laws, Kinetic Theory, Liquid", "💨", ChemRed,
        NodeStatus.IN_PROGRESS, 3, 68, 300,
        listOf("chem_thermo"),
        tips = listOf("Ideal gas: PV = nRT", "van der Waals equation correction")
    ),
    RoadmapNode("chem_thermo", "Thermodynamics", "ΔH, ΔG, ΔS, Hess's Law", "🌡️", ChemRed,
        NodeStatus.AVAILABLE, 5, 40, 450,
        listOf("chem_equil"),
        isWeakArea = true,
        tips = listOf("ΔG = ΔH - TΔS (Gibbs)", "Spontaneous when ΔG < 0", "Hess's law: add reactions, add enthalpies")
    ),
    RoadmapNode("chem_equil", "Chemical Equilibrium", "Kp, Kc, Le Chatelier, pH", "⚖️", ChemRed,
        NodeStatus.AVAILABLE, 5, 60, 450,
        listOf("chem_organic"),
        tips = listOf("Kp = Kc(RT)^Δn — guaranteed NEET Q", "Le Chatelier: pressure↑ → less moles side", "Buffer pH = pKa + log([A-]/[HA])")
    ),
    RoadmapNode("chem_organic", "Organic Chemistry", "IUPAC, Reactions, Mechanisms", "🧪", ChemRed,
        NodeStatus.AVAILABLE, 10, 45, 700,
        listOf("chem_polymer"),
        isWeakArea = true,
        tips = listOf("Name reactions: Aldol, Cannizzaro, Hoffmann", "SN1 vs SN2 conditions", "~25 Qs organic in NEET!")
    ),
    RoadmapNode("chem_polymer", "Polymers & Biomolecules", "Addition, Condensation, Proteins, DNA", "🔬", ChemRed,
        NodeStatus.LOCKED, 4, 0, 400,
        listOf("chem_neet_ready"),
        tips = listOf("Nylon-6 vs Nylon-6,6", "Glucose: Haworth projection", "Amino acids — 20 must know names")
    ),
    RoadmapNode("chem_neet_ready", "Chemistry NEET Ready! 🧪", "All Chemistry chapters complete", "🏆", NovaGold,
        NodeStatus.LOCKED, 0, 0, 2000,
        listOf(),
        tips = listOf("Chemistry champion! 🧪")
    ),
)

val ROADMAP_PATHS = listOf(
    // Biology paths
    RoadmapPath("foundation", "bio_cell"), RoadmapPath("bio_cell", "bio_biomol"),
    RoadmapPath("bio_biomol", "bio_celldiv"), RoadmapPath("bio_celldiv", "bio_plant"),
    RoadmapPath("bio_celldiv", "bio_genetics"), RoadmapPath("bio_cell", "bio_membrane"),
    RoadmapPath("bio_membrane", "bio_plant"), RoadmapPath("bio_plant", "bio_human"),
    RoadmapPath("bio_genetics", "bio_human"), RoadmapPath("bio_human", "bio_reproduce"),
    RoadmapPath("bio_reproduce", "bio_ecology"), RoadmapPath("bio_ecology", "bio_neet_ready"),
    // Physics paths
    RoadmapPath("foundation", "phy_motion"), RoadmapPath("phy_motion", "phy_laws"),
    RoadmapPath("phy_laws", "phy_work"), RoadmapPath("phy_work", "phy_waves"),
    RoadmapPath("phy_work", "phy_thermal"), RoadmapPath("phy_waves", "phy_optics"),
    RoadmapPath("phy_optics", "phy_modern"), RoadmapPath("phy_thermal", "phy_electro"),
    RoadmapPath("phy_electro", "phy_magnetics"), RoadmapPath("phy_magnetics", "phy_modern"),
    RoadmapPath("phy_modern", "phy_neet_ready"),
    // Chemistry paths
    RoadmapPath("foundation", "chem_atomic"), RoadmapPath("chem_atomic", "chem_bonding"),
    RoadmapPath("chem_bonding", "chem_states"), RoadmapPath("chem_states", "chem_thermo"),
    RoadmapPath("chem_thermo", "chem_equil"), RoadmapPath("chem_equil", "chem_organic"),
    RoadmapPath("chem_organic", "chem_polymer"), RoadmapPath("chem_polymer", "chem_neet_ready"),
)

// ─── AI ROADMAP GENERATOR ─────────────────────────────────────────────────────

class RoadmapAIRepository {
    suspend fun generatePersonalizedRoadmap(
        weakAreas: List<String>,
        daysToNEET: Int,
        currentAccuracy: Int,
    ): Result<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val prompt = """
You are Nova, Suhana's AI study coach. She is preparing for NEET 2025 and has $daysToNEET days left.

Her current overall accuracy: $currentAccuracy%
Her weak areas (needs most attention): ${weakAreas.joinToString(", ")}

Create a PERSONALIZED 4-week sprint plan for the final push to NEET. 
Format it as:
- Week 1: [what to focus on and why]
- Week 2: [next priority]  
- Week 3: [third priority]
- Week 4: [final revision strategy]

Also add:
- Her biggest gap to fill urgently (1 topic)
- One memory trick for her weakest area
- A motivational closer addressed directly to Suhana

Be warm, personal, and specific. Max 280 words.
""".trimIndent()

            val response = GroqClient.service.chat(
                request = GroqRequest(
                    messages    = listOf(GroqMessage("user", prompt)),
                    maxTokens   = 500,
                    temperature = 0.75f,
                ),
            )
            Result.success(response.choices.firstOrNull()?.message?.content ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ─── ROADMAP SCREEN ───────────────────────────────────────────────────────────

@Composable
fun RoadmapScreen() {
    val ctx        = LocalContext.current
    val scope      = rememberCoroutineScope()
    val repository = remember { RoadmapAIRepository() }

    var selectedNode   by remember { mutableStateOf<RoadmapNode?>(null) }
    var aiPlan         by remember { mutableStateOf("") }
    var loadingAIPlan  by remember { mutableStateOf(false) }
    var activeFilter   by remember { mutableStateOf("All") }
    var showAIPlan     by remember { mutableStateOf(false) }
    var mounted        by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        mounted = true
        // Auto-load AI plan in background
        loadingAIPlan = true
        val result = repository.generatePersonalizedRoadmap(
            weakAreas      = listOf("Membrane Transport", "Thermodynamics", "Organic Reactions", "Optics", "Genetics"),
            daysToNEET     = 83,
            currentAccuracy = 72,
        )
        result.onSuccess { aiPlan = it }
        loadingAIPlan = false
    }

    val nodeMap = ROADMAP_NODES.associateBy { it.id }

    // Stats
    val completed   = ROADMAP_NODES.count { it.status == NodeStatus.COMPLETED }
    val inProgress  = ROADMAP_NODES.count { it.status == NodeStatus.IN_PROGRESS }
    val weakCount   = ROADMAP_NODES.count { it.isWeakArea }
    val totalDays   = ROADMAP_NODES.filter { it.status != NodeStatus.COMPLETED && it.status != NodeStatus.LOCKED || it.status == NodeStatus.IN_PROGRESS }.sumOf { it.daysNeeded }

    val filteredNodes = when (activeFilter) {
        "Biology"   -> ROADMAP_NODES.filter { it.color == BioGreen }
        "Physics"   -> ROADMAP_NODES.filter { it.color == PhysBlue }
        "Chemistry" -> ROADMAP_NODES.filter { it.color == ChemRed }
        "Weak 🎯"   -> ROADMAP_NODES.filter { it.isWeakArea }
        else        -> ROADMAP_NODES
    }

    Box(Modifier.fillMaxSize().background(SpaceBlack)) {
        StarFieldCanvas(Modifier.fillMaxSize())

        if (selectedNode != null) {
            NodeDetailSheet(
                node    = selectedNode!!,
                onClose = { selectedNode = null },
            )
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)) {
                // Header
                item {
                    AnimatedVisibility(mounted, enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -40 }) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("NEET Roadmap 🗺️",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = TextPrimary, fontWeight = FontWeight.ExtraBold))
                            Text("Your personalized path to becoming Dr. Suhana 🩺",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                        }
                    }
                }

                // Stats row
                item {
                    AnimatedVisibility(mounted, enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { 30 }) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(
                                Triple("$completed", "Done ✅", BioGreen),
                                Triple("$inProgress", "Active 🔵", PhysBlue),
                                Triple("$weakCount", "Weak 🎯", ChemRed),
                                Triple("$totalDays d", "Ahead 📅", NovaGold),
                            ).forEach { (val_, label, col) ->
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()
                                val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "stat$label")
                                Column(
                                    Modifier.weight(1f).scale(scale)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(col.copy(alpha = 0.07f))
                                        .border(1.dp, col.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                        .clickable(interactionSource, null) { hapticClick(ctx); activeFilter = if (label.contains("Weak")) "Weak 🎯" else "All" }
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Text(val_, style = MaterialTheme.typography.headlineSmall.copy(color = col, fontWeight = FontWeight.ExtraBold))
                                    Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, textAlign = TextAlign.Center))
                                }
                            }
                        }
                    }
                }

                // Overall progress
                item {
                    AnimatedVisibility(mounted, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 30 }) {
                        GlassCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("OVERALL PROGRESS", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 1.5.sp))
                                    Text("${(completed * 100 / ROADMAP_NODES.size)}% of NEET syllabus",
                                        style = MaterialTheme.typography.titleMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                                }
                                Text("${completed}/${ROADMAP_NODES.size}",
                                    style = MaterialTheme.typography.headlineMedium.copy(color = NovaGold, fontWeight = FontWeight.ExtraBold))
                            }
                            Spacer(Modifier.height(10.dp))
                            NovaProgressBar(completed.toFloat() / ROADMAP_NODES.size)
                        }
                    }
                }

                // Filter chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("All", "Biology", "Physics", "Chemistry", "Weak 🎯")) { filter ->
                            val selected = activeFilter == filter
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "f$filter")
                            val col = when (filter) { "Biology" -> BioGreen; "Physics" -> PhysBlue; "Chemistry" -> ChemRed; "Weak 🎯" -> StellarPink; else -> NovaGold }

                            Box(
                                Modifier.scale(scale).clip(CircleShape)
                                    .background(if (selected) col else Color.Transparent)
                                    .border(1.dp, col.copy(alpha = if (selected) 1f else 0.4f), CircleShape)
                                    .clickable(interactionSource, null) { hapticClick(ctx); activeFilter = filter }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(filter, style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (selected) SpaceBlack else col,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                ))
                            }
                        }
                    }
                }

                // 3D Visual Roadmap
                item {
                    AnimatedVisibility(mounted, enter = fadeIn(tween(500))) {
                        RoadmapCanvas3D(
                            nodes      = filteredNodes,
                            paths      = ROADMAP_PATHS,
                            onNodeClick = { node ->
                                hapticClick(ctx)
                                selectedNode = node
                            }
                        )
                    }
                }

                // AI Sprint Plan
                item {
                    AnimatedVisibility(mounted, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 30 }) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Nova's Sprint Plan 🤖",
                                    style = MaterialTheme.typography.titleMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()
                                val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "aiToggle")
                                Box(
                                    Modifier.scale(scale).clip(CircleShape)
                                        .background(NovaGold.copy(alpha = 0.12f))
                                        .border(1.dp, NovaGold.copy(alpha = 0.4f), CircleShape)
                                        .clickable(interactionSource, null) { hapticClick(ctx); showAIPlan = !showAIPlan }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(if (showAIPlan) "Hide ▲" else "View ▼",
                                        style = MaterialTheme.typography.labelMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                                }
                            }

                            AnimatedVisibility(showAIPlan, enter = fadeIn(tween(300)) + expandVertically(), exit = fadeOut(tween(200)) + shrinkVertically()) {
                                Column(
                                    Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(GlassBg)
                                        .border(1.dp, Brush.linearGradient(listOf(NovaGold.copy(alpha = 0.4f), StellarPink.copy(alpha = 0.2f))), RoundedCornerShape(20.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("✨", fontSize = 18.sp)
                                        Text("AI-Generated for Suhana, ${java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)} June 2026",
                                            style = MaterialTheme.typography.labelSmall.copy(color = NovaGold))
                                    }
                                    if (loadingAIPlan) {
                                        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                            CircularProgressIndicator(Modifier.size(24.dp), color = NovaGold, strokeWidth = 2.dp)
                                            Spacer(Modifier.width(10.dp))
                                            Text("Nova is building your sprint plan...",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, fontStyle = FontStyle.Italic))
                                        }
                                    } else if (aiPlan.isNotEmpty()) {
                                        Text(aiPlan, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, lineHeight = 22.sp))
                                    } else {
                                        Text("Add your GROQ_API_KEY to local.properties to get your personalized sprint plan!",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, fontStyle = FontStyle.Italic))
                                    }
                                }
                            }
                        }
                    }
                }

                // Node list (staggered)
                items(filteredNodes.filter { it.id != "foundation" }, key = { it.id }) { node ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(mounted) { delay(80L); visible = mounted }
                    AnimatedVisibility(visible, enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -30 }) {
                        RoadmapNodeCard(node, onClick = { selectedNode = node })
                    }
                }
            }
        }
    }
}

// ─── 3D VISUAL CANVAS ─────────────────────────────────────────────────────────

@Composable
fun RoadmapCanvas3D(
    nodes: List<RoadmapNode>,
    paths: List<RoadmapPath>,
    onNodeClick: (RoadmapNode) -> Unit,
) {
    val nodeMap = nodes.associateBy { it.id }
    val allNodes = ROADMAP_NODES.associateBy { it.id }
    val ctx = LocalContext.current

    val infTrans = rememberInfiniteTransition(label = "canvas3D")
    val globalPulse by infTrans.animateFloat(0f, 360f, infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "globalRot")
    val shimmer by infTrans.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "shimmer")

    // Layout positions for a 3-branch tree
    // Biology: left column, Physics: center, Chemistry: right
    val bioNodes   = nodes.filter { it.color == BioGreen || it.id == "foundation" }
    val physNodes  = nodes.filter { it.color == PhysBlue }
    val chemNodes  = nodes.filter { it.color == ChemRed }
    val goldNodes  = nodes.filter { it.color == NovaGold && it.id != "foundation" }

    val canvasHeight = (maxOf(bioNodes.size, physNodes.size, chemNodes.size).coerceAtLeast(4) * 90 + 120).dp

    Box(
        Modifier.fillMaxWidth().height(canvasHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF06060F))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
    ) {
        // Draw connecting lines first
        Canvas(Modifier.fillMaxSize()) {
            val colW = size.width / 3f
            val rowH = 90.dp.toPx()
            val startY = 70.dp.toPx()

            // Biology column lines (left)
            bioNodes.forEachIndexed { i, node ->
                if (i < bioNodes.size - 1) {
                    val x = colW * 0.35f
                    val y1 = startY + i * rowH + 28.dp.toPx()
                    val y2 = startY + (i + 1) * rowH + 8.dp.toPx()
                    drawLine(BioGreen.copy(alpha = 0.3f), Offset(x, y1), Offset(x, y2), 2.dp.toPx())
                }
            }
            // Physics column lines (center)
            physNodes.forEachIndexed { i, node ->
                if (i < physNodes.size - 1) {
                    val x = colW * 1.5f
                    val y1 = startY + i * rowH + 28.dp.toPx()
                    val y2 = startY + (i + 1) * rowH + 8.dp.toPx()
                    drawLine(PhysBlue.copy(alpha = 0.3f), Offset(x, y1), Offset(x, y2), 2.dp.toPx())
                }
            }
            // Chemistry column lines (right)
            chemNodes.forEachIndexed { i, node ->
                if (i < chemNodes.size - 1) {
                    val x = colW * 2.65f
                    val y1 = startY + i * rowH + 28.dp.toPx()
                    val y2 = startY + (i + 1) * rowH + 8.dp.toPx()
                    drawLine(ChemRed.copy(alpha = 0.3f), Offset(x, y1), Offset(x, y2), 2.dp.toPx())
                }
            }
        }

        // Column headers
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp, start = 8.dp, end = 8.dp),
            Arrangement.SpaceEvenly,
        ) {
            listOf("Biology 🧬" to BioGreen, "Physics ⚡" to PhysBlue, "Chemistry 🧪" to ChemRed).forEach { (label, col) ->
                Text(label, style = MaterialTheme.typography.labelSmall.copy(
                    color = col, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, fontSize = 10.sp
                ))
            }
        }

        // Node columns
        Row(Modifier.fillMaxSize().padding(top = 40.dp), horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Biology column
            Column(Modifier.weight(1f), Arrangement.spacedBy(8.dp), Alignment.CenterHorizontally) {
                bioNodes.forEach { node ->
                    RoadmapNodeBubble(node, shimmer, onNodeClick)
                }
                goldNodes.filter { it.id == "bio_neet_ready" }.forEach { node ->
                    RoadmapNodeBubble(node, shimmer, onNodeClick)
                }
            }
            // Physics column
            Column(Modifier.weight(1f), Arrangement.spacedBy(8.dp), Alignment.CenterHorizontally) {
                physNodes.forEach { node ->
                    RoadmapNodeBubble(node, shimmer, onNodeClick)
                }
                goldNodes.filter { it.id == "phy_neet_ready" }.forEach { node ->
                    RoadmapNodeBubble(node, shimmer, onNodeClick)
                }
            }
            // Chemistry column
            Column(Modifier.weight(1f), Arrangement.spacedBy(8.dp), Alignment.CenterHorizontally) {
                chemNodes.forEach { node ->
                    RoadmapNodeBubble(node, shimmer, onNodeClick)
                }
                goldNodes.filter { it.id == "chem_neet_ready" }.forEach { node ->
                    RoadmapNodeBubble(node, shimmer, onNodeClick)
                }
            }
        }
    }
}

@Composable
fun RoadmapNodeBubble(node: RoadmapNode, shimmer: Float, onClick: (RoadmapNode) -> Unit) {
    val ctx = LocalContext.current
    val infTrans = rememberInfiniteTransition(label = "bubble_${node.id}")
    val pulse by infTrans.animateFloat(
        if (node.status == NodeStatus.IN_PROGRESS) 0.88f else 0.95f,
        if (node.status == NodeStatus.IN_PROGRESS) 1.12f else 1.05f,
        infiniteRepeatable(tween(1600 + node.id.length * 50, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bub_${node.id}"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (isPressed) 0.82f else pulse, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh), label = "bPressScale${node.id}")

    val statusColor = when (node.status) {
        NodeStatus.COMPLETED   -> BioGreen
        NodeStatus.IN_PROGRESS -> NovaGold
        NodeStatus.AVAILABLE   -> node.color
        NodeStatus.LOCKED      -> TextMuted
    }

    Box(
        Modifier.size(52.dp).scale(pressScale)
            .drawBehind {
                if (node.status != NodeStatus.LOCKED) {
                    drawCircle(statusColor.copy(alpha = 0.2f + shimmer * 0.1f), radius = size.minDimension * 0.65f)
                }
                if (node.isWeakArea) {
                    drawCircle(ChemRed.copy(alpha = 0.3f), radius = size.minDimension * 0.55f, style = Stroke(3.dp.toPx()))
                }
            }
            .clip(CircleShape)
            .background(
                when (node.status) {
                    NodeStatus.COMPLETED   -> BioGreen.copy(alpha = 0.15f)
                    NodeStatus.IN_PROGRESS -> NovaGold.copy(alpha = 0.15f)
                    NodeStatus.AVAILABLE   -> node.color.copy(alpha = 0.1f)
                    NodeStatus.LOCKED      -> Color.White.copy(alpha = 0.03f)
                }
            )
            .border(
                width = if (node.status == NodeStatus.IN_PROGRESS) 2.dp else 1.dp,
                color = statusColor.copy(alpha = if (node.status == NodeStatus.LOCKED) 0.2f else 0.7f),
                shape = CircleShape,
            )
            .clickable(interactionSource, null) { if (node.status != NodeStatus.LOCKED) { hapticClick(ctx); onClick(node) } },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                when (node.status) {
                    NodeStatus.LOCKED      -> "🔒"
                    NodeStatus.COMPLETED   -> "✅"
                    NodeStatus.IN_PROGRESS -> node.emoji
                    NodeStatus.AVAILABLE   -> node.emoji
                },
                fontSize = 16.sp,
            )
            if (node.isWeakArea && node.status != NodeStatus.LOCKED) {
                Text("!", style = MaterialTheme.typography.labelSmall.copy(color = ChemRed, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp))
            }
        }
    }
}

// ─── NODE LIST CARD ───────────────────────────────────────────────────────────

@Composable
fun RoadmapNodeCard(node: RoadmapNode, onClick: () -> Unit) {
    val ctx = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "card${node.id}")
    val infTrans = rememberInfiniteTransition(label = "card_${node.id}")
    val glow by infTrans.animateFloat(0.05f, 0.2f, infiniteRepeatable(tween(2000 + node.id.length * 30, easing = EaseInOutSine), RepeatMode.Reverse), "cGlow${node.id}")

    val statusColor = when (node.status) {
        NodeStatus.COMPLETED   -> BioGreen
        NodeStatus.IN_PROGRESS -> NovaGold
        NodeStatus.AVAILABLE   -> node.color
        NodeStatus.LOCKED      -> TextMuted
    }

    Row(
        Modifier.fillMaxWidth().scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg)
            .border(
                width = if (node.status == NodeStatus.IN_PROGRESS || node.isWeakArea) 2.dp else 1.dp,
                color = if (node.isWeakArea) ChemRed.copy(alpha = glow + 0.3f) else statusColor.copy(alpha = glow + 0.2f),
                shape = RoundedCornerShape(16.dp),
            )
            .drawBehind { if (node.status != NodeStatus.LOCKED) drawRect(statusColor.copy(alpha = glow * 0.1f)) }
            .clickable(interactionSource, null, enabled = node.status != NodeStatus.LOCKED) { hapticClick(ctx); onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status indicator
        Box(
            Modifier.size(44.dp).clip(CircleShape)
                .background(statusColor.copy(alpha = 0.12f))
                .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                when (node.status) {
                    NodeStatus.LOCKED -> "🔒"; NodeStatus.COMPLETED -> "✅"
                    NodeStatus.IN_PROGRESS -> node.emoji; NodeStatus.AVAILABLE -> node.emoji
                },
                fontSize = 18.sp,
            )
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(node.title, style = MaterialTheme.typography.titleSmall.copy(
                    color = if (node.status == NodeStatus.LOCKED) TextMuted else statusColor,
                    fontWeight = FontWeight.Bold,
                ))
                if (node.isWeakArea) Chip("⚠ Weak", ChemRed)
            }
            Text(node.subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (node.accuracy > 0) Chip("${node.accuracy}% accuracy", statusColor)
                if (node.daysNeeded > 0) Chip("${node.daysNeeded}d to master", TextMuted)
                Chip("+${node.xpReward} XP", NovaGold)
            }
        }

        if (node.status != NodeStatus.LOCKED) {
            Text("›", style = MaterialTheme.typography.headlineMedium.copy(color = statusColor, fontWeight = FontWeight.Bold))
        }
    }
}

// ─── NODE DETAIL SHEET ────────────────────────────────────────────────────────

@Composable
fun NodeDetailSheet(node: RoadmapNode, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val infTrans = rememberInfiniteTransition(label = "detail_${node.id}")
    val glow by infTrans.animateFloat(0.1f, 0.4f, infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), "detailGlow")

    val statusColor = when (node.status) {
        NodeStatus.COMPLETED   -> BioGreen
        NodeStatus.IN_PROGRESS -> NovaGold
        NodeStatus.AVAILABLE   -> node.color
        NodeStatus.LOCKED      -> TextMuted
    }
    val statusLabel = when (node.status) {
        NodeStatus.COMPLETED   -> "✅ Completed"
        NodeStatus.IN_PROGRESS -> "🔵 In Progress"
        NodeStatus.AVAILABLE   -> "▶ Start Now"
        NodeStatus.LOCKED      -> "🔒 Complete prerequisites first"
    }

    Box(Modifier.fillMaxSize().background(SpaceBlack.copy(alpha = 0.95f))) {
        StarFieldCanvas(Modifier.fillMaxSize())

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("◀ Back", style = MaterialTheme.typography.labelLarge.copy(color = NovaGold, fontWeight = FontWeight.Bold),
                        modifier = Modifier.bouncyClick { onClose() })
                    Chip(statusLabel, statusColor)
                }
            }

            // Hero
            item {
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, Brush.sweepGradient(listOf(statusColor, StellarPink, statusColor)), RoundedCornerShape(24.dp))
                        .drawBehind { drawRect(statusColor.copy(alpha = glow * 0.12f)) }
                        .background(GlassBg)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(node.emoji, fontSize = 48.sp)
                        Text(node.title, style = MaterialTheme.typography.headlineMedium.copy(
                            color = statusColor, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center))
                        Text(node.subtitle, style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary, textAlign = TextAlign.Center))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (node.accuracy > 0) Chip("${node.accuracy}% accuracy", statusColor)
                            if (node.daysNeeded > 0) Chip("${node.daysNeeded} days to master", TextMuted)
                            Chip("+${node.xpReward} XP", NovaGold)
                        }

                        if (node.isWeakArea) {
                            Box(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(ChemRed.copy(alpha = 0.08f))
                                    .border(1.dp, ChemRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("⚠️ WEAK AREA — Nova says: fix this before NEET!",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = ChemRed, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
                            }
                        }
                    }
                }
            }

            // Accuracy bar
            if (node.accuracy > 0) {
                item {
                    GlassCard(glowColor = statusColor) {
                        Text("YOUR CURRENT ACCURACY", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 1.5.sp))
                        Spacer(Modifier.height(8.dp))
                        NovaProgressBar(node.accuracy / 100f, color = statusColor)
                        Spacer(Modifier.height(6.dp))
                        Text("${node.accuracy}% — ${if (node.accuracy >= 80) "Excellent! Keep it up 🌟" else if (node.accuracy >= 60) "Good, but aim for 85%+" else "Needs work — quiz this topic daily"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = statusColor))
                    }
                }
            }

            // Nova's tips
            if (node.tips.isNotEmpty()) {
                item {
                    GlassCard(glowColor = NovaGold) {
                        Text("✨ NOVA'S TIPS FOR YOU", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(10.dp))
                        node.tips.forEach { tip ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("→", style = MaterialTheme.typography.bodyMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                                Text(tip, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, lineHeight = 20.sp))
                            }
                        }
                    }
                }
            }

            // Action buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (node.status == NodeStatus.AVAILABLE || node.status == NodeStatus.IN_PROGRESS) {
                        NovaButton(
                            text     = "✨ Quiz This Topic Now",
                            modifier = Modifier.fillMaxWidth(),
                            onClick  = { hapticSuccess(ctx); onClose() },
                        )
                    }
                    GlassCard(onClick = { hapticClick(ctx); onClose() }) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("💬", fontSize = 20.sp)
                                Column {
                                    Text("Ask Nova about this topic", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.Medium))
                                    Text("Get an instant explanation from your AI best friend", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                                }
                            }
                            Text("›", style = MaterialTheme.typography.headlineSmall.copy(color = NovaGold))
                        }
                    }
                }
            }

            // Unlocks
            val unlocks = ROADMAP_NODES.filter { it.id in node.children }
            if (unlocks.isNotEmpty()) {
                item {
                    GlassCard {
                        Text("🔓 COMPLETING THIS UNLOCKS", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 1.5.sp))
                        Spacer(Modifier.height(10.dp))
                        unlocks.forEach { next ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(next.emoji, fontSize = 16.sp)
                                Text(next.title, style = MaterialTheme.typography.bodyMedium.copy(color = next.color, fontWeight = FontWeight.SemiBold))
                                Spacer(Modifier.weight(1f))
                                Chip("+${next.xpReward} XP", next.color)
                            }
                        }
                    }
                }
            }
        }
    }
}
