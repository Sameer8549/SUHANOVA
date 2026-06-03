package com.example.suhanova.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ─── NOVA CHAT REPOSITORY ─────────────────────────────────────────────────────

class NovaChatRepository {

    private val gson = Gson()

    /**
     * Send a message to Nova (Groq llama3-70b-8192).
     * Maintains conversation history for context.
     * Returns the AI's response text.
     */
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<GroqMessage>,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = buildList {
                add(GroqMessage(role = "system", content = NOVA_SYSTEM_PROMPT))
                addAll(conversationHistory.takeLast(10)) // Keep last 10 messages for context
                add(GroqMessage(role = "user", content = userMessage))
            }

            val response = GroqClient.service.chat(
                request = GroqRequest(messages = messages),
            )

            val reply = response.choices.firstOrNull()?.message?.content
                ?: "Nova is thinking... 🌟 Please try again."

            Result.success(reply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ─── MCQ GENERATION REPOSITORY ────────────────────────────────────────────────

data class GeneratedQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
)

data class GeneratedQuestionsWrapper(val questions: List<GeneratedQuestion>)

class QuizRepository {

    private val gson = Gson()

    /**
     * Generate real NEET MCQ questions using Mistral AI.
     * Returns a list of freshly generated questions.
     */
    suspend fun generateQuestions(
        subject: String,
        topic: String,
        difficulty: String = "Mixed",
        count: Int = 5,
    ): Result<List<GeneratedQuestion>> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildMcqPrompt(subject, topic, difficulty, count)

            val response = MistralClient.service.generate(
                request = MistralRequest(
                    messages = listOf(MistralMessage(role = "user", content = prompt))
                ),
            )

            val rawJson = response.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("Empty response from Mistral"))

            // Parse the JSON response
            val cleanJson = rawJson.trim()
                .removePrefix("```json").removeSuffix("```")
                .removePrefix("```").removeSuffix("```")
                .trim()

            val wrapper = gson.fromJson(cleanJson, GeneratedQuestionsWrapper::class.java)
            Result.success(wrapper.questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ─── REAL NEET QUESTION BANK (static fallback when no internet) ───────────────

val NEET_QUESTION_BANK = listOf(
    GeneratedQuestion(
        question = "Which of the following is NOT a function of the cell membrane?",
        options  = listOf(
            "Regulating movement of substances into and out of the cell",
            "Cell recognition and signaling",
            "ATP synthesis",
            "Maintaining cell shape"
        ),
        correctIndex = 2,
        explanation  = "ATP synthesis occurs in the mitochondria (oxidative phosphorylation) and chloroplasts (photophosphorylation), not the cell membrane. The cell membrane controls transport, participates in cell signaling, and helps maintain shape.",
        subject      = "Biology",
        topic        = "Cell Biology",
        difficulty   = "Easy",
    ),
    GeneratedQuestion(
        question = "The process of photosynthesis occurs in the:",
        options  = listOf("Mitochondria", "Ribosome", "Chloroplast", "Nucleus"),
        correctIndex = 2,
        explanation  = "Photosynthesis occurs in the chloroplast. The light-dependent reactions occur in the thylakoid membranes, while the Calvin cycle (light-independent) occurs in the stroma. Mitochondria perform cellular respiration.",
        subject      = "Biology",
        topic        = "Photosynthesis",
        difficulty   = "Easy",
    ),
    GeneratedQuestion(
        question = "Which law states that 'the total momentum of a system remains constant if no external force acts on it'?",
        options  = listOf("Newton's First Law", "Newton's Second Law", "Newton's Third Law", "Law of Conservation of Momentum"),
        correctIndex = 3,
        explanation  = "The Law of Conservation of Momentum states that total momentum of an isolated system is conserved. This is derived from Newton's 3rd Law but is a separate fundamental principle. Crucial for collision problems in NEET Physics.",
        subject      = "Physics",
        topic        = "Laws of Motion",
        difficulty   = "Medium",
    ),
    GeneratedQuestion(
        question = "In Mendel's law of segregation, which of the following is correct?",
        options  = listOf(
            "Both alleles of a gene are expressed equally",
            "The two alleles of a gene separate during gamete formation",
            "Alleles of different genes always separate together",
            "Dominant alleles eliminate recessive alleles"
        ),
        correctIndex = 1,
        explanation  = "Mendel's Law of Segregation states that the two alleles for any trait separate (segregate) during gamete formation, so each gamete carries only one allele. This is the basis of Mendelian genetics and appears every year in NEET Biology.",
        subject      = "Biology",
        topic        = "Genetics",
        difficulty   = "Medium",
    ),
    GeneratedQuestion(
        question = "Which of the following correctly represents the relation between Kp and Kc?",
        options  = listOf("Kp = Kc(RT)^Δn", "Kp = Kc + RT", "Kp = Kc / (RT)^Δn", "Kp = Kc × Δn"),
        correctIndex = 0,
        explanation  = "Kp = Kc(RT)^Δn where Δn = moles of gaseous products - moles of gaseous reactants, R = 0.0821 L·atm/mol·K, T = temperature in Kelvin. When Δn = 0, Kp = Kc. This formula is directly tested in NEET Chemistry.",
        subject      = "Chemistry",
        topic        = "Chemical Equilibrium",
        difficulty   = "Hard",
    ),
    GeneratedQuestion(
        question = "The resting membrane potential of a neuron is approximately:",
        options  = listOf("-30 mV", "-70 mV", "+40 mV", "0 mV"),
        correctIndex = 1,
        explanation  = "The resting membrane potential of a typical neuron is approximately -70 mV (inside negative relative to outside). This is maintained by the Na⁺/K⁺ ATPase pump and selective permeability of the membrane to K⁺ ions. NEET frequently tests the action potential values.",
        subject      = "Biology",
        topic        = "Neural Communication",
        difficulty   = "Hard",
    ),
    GeneratedQuestion(
        question = "A body moves with uniform velocity. What is its acceleration?",
        options  = listOf("Positive", "Negative", "Zero", "Cannot be determined"),
        correctIndex = 2,
        explanation  = "Uniform velocity means constant speed AND constant direction. Since acceleration = rate of change of velocity, and velocity is not changing, acceleration = 0. This is Newton's First Law in action — a body continues in uniform motion unless acted upon by a net external force.",
        subject      = "Physics",
        topic        = "Kinematics",
        difficulty   = "Easy",
    ),
    GeneratedQuestion(
        question = "Which of the following is a lysosomal enzyme?",
        options  = listOf("DNA Polymerase", "ATP Synthase", "Acid Phosphatase", "RNA Polymerase"),
        correctIndex = 2,
        explanation  = "Acid Phosphatase is a characteristic lysosomal enzyme, active at the acidic pH (4.5–5.0) maintained inside lysosomes. Lysosomes contain ~50 different hydrolytic enzymes for intracellular digestion. Remember: lysosomes = suicide bags, discovered by de Duve.",
        subject      = "Biology",
        topic        = "Cell Organelles",
        difficulty   = "Hard",
    ),
    GeneratedQuestion(
        question = "The IUPAC name of CH₃-CH₂-CHO is:",
        options  = listOf("Propan-1-ol", "Propanal", "Propanone", "Propanoic acid"),
        correctIndex = 1,
        explanation  = "CHO at the end indicates an aldehyde functional group. The compound has 3 carbons (prop-) and an aldehyde (-al). So: Propanal. Remember: Aldehyde suffix = -al, Ketone = -one, Carboxylic acid = -oic acid. Nomenclature is always tested in NEET Chemistry Organic section.",
        subject      = "Chemistry",
        topic        = "Organic Chemistry",
        difficulty   = "Medium",
    ),
    GeneratedQuestion(
        question = "Which type of RNA carries amino acids to the ribosome during translation?",
        options  = listOf("mRNA", "rRNA", "tRNA", "hnRNA"),
        correctIndex = 2,
        explanation  = "tRNA (Transfer RNA) is the adapter molecule that carries specific amino acids to the ribosome and recognizes the mRNA codons via its anticodon loop. mRNA carries genetic information, rRNA forms the ribosome structure, hnRNA is the primary transcript. Suhana — this is a GUARANTEED NEET question every year! 🩺",
        subject      = "Biology",
        topic        = "Molecular Biology",
        difficulty   = "Easy",
    ),
)
