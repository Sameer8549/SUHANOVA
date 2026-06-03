// Suhanova — Smart Quiz Engine
import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const SAMPLE_QUESTIONS = [
  {
    id: 1,
    question: "Which organelle is known as the powerhouse of the cell?",
    subject: "Biology 🧬",
    chapter: "Chapter 4: Cell Division",
    difficulty: "Medium",
    options: ["Nucleus", "Mitochondria", "Ribosome", "Golgi Apparatus"],
    correct: 1,
    explanation: "Think of mitochondria like tiny power plants inside your cell! They convert glucose into ATP energy — just like a generator converts fuel into electricity. For NEET, remember: Mitochondria = ATP production = powerhouse. This comes up every year, Suhana! 💡",
  },
  {
    id: 2,
    question: "Osmosis is the movement of water molecules through a semi-permeable membrane from a region of:",
    subject: "Biology 🧬",
    chapter: "Chapter 6: Transport",
    difficulty: "Easy",
    options: [
      "High solute concentration to low",
      "Low water concentration to high",
      "High water concentration to low",
      "Equal concentrations"
    ],
    correct: 2,
    explanation: "Water always moves from where there's MORE of it (dilute/hypotonic solution) to where there's LESS of it (concentrated/hypertonic). Remember: water chases salt! 🌊 Dilute → Concentrated.",
  },
  {
    id: 3,
    question: "Newton's First Law of Motion is also known as the Law of:",
    subject: "Physics ⚡",
    chapter: "Chapter 2: Laws of Motion",
    difficulty: "Easy",
    options: ["Acceleration", "Inertia", "Action-Reaction", "Conservation"],
    correct: 1,
    explanation: "Newton's 1st Law = Law of Inertia. Objects at rest stay at rest, objects in motion stay in motion — UNLESS an external force acts on them. Think of a ball on a frictionless surface — it would roll forever! ⚡",
  },
  {
    id: 4,
    question: "Which of the following is an example of a Lewis acid?",
    subject: "Chemistry 🧪",
    chapter: "Chapter 3: Chemical Bonding",
    difficulty: "Hard",
    options: ["NH₃", "H₂O", "BF₃", "NaOH"],
    correct: 2,
    explanation: "BF₃ (Boron trifluoride) is a classic Lewis acid — it can ACCEPT an electron pair. Boron only has 6 electrons in its outer shell, making it electron-deficient. NH₃ and H₂O are Lewis BASES — they donate electron pairs. 🧪",
  },
  {
    id: 5,
    question: "The value of g (acceleration due to gravity) on the surface of the Earth is approximately:",
    subject: "Physics ⚡",
    chapter: "Chapter 5: Gravitation",
    difficulty: "Easy",
    options: ["6.67 × 10⁻¹¹ m/s²", "9.8 m/s²", "10.8 m/s²", "8.9 m/s²"],
    correct: 1,
    explanation: "g ≈ 9.8 m/s² on Earth's surface. In NEET, we often use g = 10 m/s² for easy calculation. Remember: G (gravitational constant) = 6.67 × 10⁻¹¹, but g (acceleration) = 9.8 m/s². Don't confuse the two! ⚡",
  },
];

const DIFFICULTY_COLORS = {
  Easy: 'var(--bio-green)',
  Medium: 'var(--nova-gold)',
  Hard: 'var(--chem-red)',
};

export default function QuizScreen() {
  const [questionIndex, setQuestionIndex] = useState(0);
  const [selected, setSelected] = useState(null);
  const [answered, setAnswered] = useState(false);
  const [showExplanation, setShowExplanation] = useState(false);
  const [score, setScore] = useState({ correct: 0, wrong: 0, skipped: 0 });
  const [timeLeft, setTimeLeft] = useState(60);
  const [timerActive, setTimerActive] = useState(true);
  const [finished, setFinished] = useState(false);

  const current = SAMPLE_QUESTIONS[questionIndex];
  const totalQuestions = SAMPLE_QUESTIONS.length;
  const circumference = 2 * Math.PI * 34;
  const timerPct = timeLeft / 60;

  const handleAnswer = useCallback((optionIndex) => {
    if (answered) return;
    setSelected(optionIndex);
    setAnswered(true);
    setTimerActive(false);
    setTimeout(() => setShowExplanation(true), 600);
    if (optionIndex === current.correct) {
      setScore(s => ({ ...s, correct: s.correct + 1 }));
    } else {
      setScore(s => ({ ...s, wrong: s.wrong + 1 }));
    }
  }, [answered, current]);

  const handleNext = () => {
    if (questionIndex + 1 >= totalQuestions) {
      setFinished(true);
      return;
    }
    setQuestionIndex(i => i + 1);
    setSelected(null);
    setAnswered(false);
    setShowExplanation(false);
    setTimeLeft(60);
    setTimerActive(true);
  };

  useEffect(() => {
    if (!timerActive || answered) return;
    if (timeLeft <= 0) { handleAnswer(-1); return; }
    const t = setTimeout(() => setTimeLeft(t => t - 1), 1000);
    return () => clearTimeout(t);
  }, [timeLeft, timerActive, answered, handleAnswer]);

  if (finished) return <QuizResults score={score} total={totalQuestions} />;

  return (
    <div className="page-wrapper">
      <div className="page-content">
        {/* Header */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: 8 }}>
          <div>
            <h2 style={{ fontSize: '1.4rem', marginBottom: 2 }}>NEET Quiz <span>✨</span></h2>
            <div className="chip chip-green" style={{ fontSize: '0.68rem' }}>{current.subject} · {current.chapter}</div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.1rem', color: 'var(--nova-gold)' }}>
              {questionIndex + 1}/{totalQuestions}
            </div>
            <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--text-muted)' }}>questions</div>
          </div>
        </div>

        {/* Progress bar */}
        <div className="progress-bar">
          <motion.div
            className="progress-fill"
            animate={{ width: `${((questionIndex + 1) / totalQuestions) * 100}%` }}
            transition={{ duration: 0.5 }}
          />
        </div>

        {/* Timer + Score */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <div className="circular-timer">
            <svg viewBox="0 0 80 80" width="80" height="80">
              <circle cx="40" cy="40" r="34" stroke="rgba(255,255,255,0.08)" strokeWidth="5" fill="none" />
              <circle
                cx="40" cy="40" r="34"
                stroke={timeLeft < 15 ? 'var(--chem-red)' : 'var(--nova-gold)'}
                strokeWidth="5" fill="none"
                strokeLinecap="round"
                strokeDasharray={circumference}
                strokeDashoffset={circumference * (1 - timerPct)}
                style={{ transition: 'stroke-dashoffset 1s linear, stroke 0.3s' }}
                filter={`drop-shadow(0 0 6px ${timeLeft < 15 ? 'var(--chem-red)' : 'var(--nova-gold)'})`}
              />
            </svg>
            <div className="timer-text" style={{ color: timeLeft < 15 ? 'var(--chem-red)' : 'var(--nova-gold)' }}>
              <span style={{ fontSize: '1rem' }}>{String(Math.floor(timeLeft / 60)).padStart(2,'0')}:{String(timeLeft % 60).padStart(2,'0')}</span>
            </div>
          </div>

          <div style={{ flex: 1, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <span className="chip chip-green">✓ {score.correct}</span>
            <span className="chip chip-red">✗ {score.wrong}</span>
            <span className="chip" style={{ color: 'var(--text-muted)', borderColor: 'rgba(255,255,255,0.1)', background: 'transparent' }}>
              — {score.skipped}
            </span>
          </div>
        </div>

        {/* Question Card */}
        <AnimatePresence mode="wait">
          <motion.div
            key={questionIndex}
            className="glass-card"
            style={{ padding: '20px 20px' }}
            initial={{ opacity: 0, x: 30 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -30 }}
            transition={{ duration: 0.3 }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
              <div style={{
                width: 28, height: 28, borderRadius: '50%',
                background: 'rgba(255,215,0,0.15)', border: '1px solid rgba(255,215,0,0.4)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '0.8rem', color: 'var(--nova-gold)',
              }}>
                {questionIndex + 1}
              </div>
              <span className="chip" style={{
                color: DIFFICULTY_COLORS[current.difficulty],
                borderColor: DIFFICULTY_COLORS[current.difficulty] + '60',
                background: DIFFICULTY_COLORS[current.difficulty] + '15',
              }}>
                {current.difficulty}
              </span>
            </div>
            <p style={{
              fontFamily: 'var(--font-body)', fontWeight: 500, fontSize: '1rem',
              color: 'var(--text-primary)', lineHeight: 1.5,
            }}>
              {current.question}
            </p>
          </motion.div>
        </AnimatePresence>

        {/* Options */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {current.options.map((option, i) => {
            const isCorrect = i === current.correct;
            const isSelected = i === selected;
            const optionClass = answered
              ? isCorrect ? 'correct' : (isSelected ? 'wrong' : '')
              : '';

            return (
              <motion.button
                key={i}
                className={`option-card ${optionClass}`}
                onClick={() => handleAnswer(i)}
                whileTap={!answered ? { scale: 0.98 } : {}}
                style={{ cursor: answered ? 'default' : 'pointer', textAlign: 'left', width: '100%' }}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.06 }}
              >
                <div className="option-letter" style={{
                  background: answered && isCorrect ? 'rgba(0,255,150,0.2)' : answered && isSelected && !isCorrect ? 'rgba(255,107,107,0.2)' : '',
                  borderColor: answered && isCorrect ? 'var(--bio-green)' : answered && isSelected && !isCorrect ? 'var(--chem-red)' : '',
                  color: answered && isCorrect ? 'var(--bio-green)' : answered && isSelected && !isCorrect ? 'var(--chem-red)' : '',
                }}>
                  {answered && isCorrect ? '✓' : answered && isSelected && !isCorrect ? '✗' : String.fromCharCode(65 + i)}
                </div>
                <span>{option}</span>
              </motion.button>
            );
          })}
        </div>

        {/* AI Explanation */}
        <AnimatePresence>
          {showExplanation && (
            <motion.div
              className="glass-card"
              style={{
                padding: '18px 20px',
                borderLeft: '3px solid var(--nova-gold)',
                background: 'rgba(255,215,0,0.04)',
              }}
              initial={{ opacity: 0, y: 20, scale: 0.97 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{ type: 'spring', damping: 20 }}
            >
              <div style={{
                fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.75rem',
                color: 'var(--nova-gold)', marginBottom: 8, display: 'flex', alignItems: 'center', gap: 6,
              }}>
                💡 Nova says:
              </div>
              <p style={{ fontSize: '0.87rem', lineHeight: 1.55, color: 'var(--text-primary)', marginBottom: 16 }}>
                {current.explanation}
              </p>
              <motion.button
                className="btn-primary"
                style={{ width: '100%' }}
                onClick={handleNext}
                whileTap={{ scale: 0.97 }}
              >
                {questionIndex + 1 >= totalQuestions ? 'See Results 🎉' : 'Next Question →'}
              </motion.button>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

function QuizResults({ score, total }) {
  const pct = Math.round((score.correct / total) * 100);
  const circumference = 2 * Math.PI * 54;

  const NOVA_MESSAGES = [
    "Suhana, that's not just a score — that's proof. 🌟",
    "Every question you cracked today is a step closer to the hospital halls. 🩺",
    `${pct}% on your first try? That's preparation meeting confidence, Suhana.`,
  ];
  const msg = NOVA_MESSAGES[Math.floor(Math.random() * NOVA_MESSAGES.length)];

  return (
    <div className="page-wrapper">
      <div className="page-content" style={{ alignItems: 'center', paddingTop: 24 }}>
        <motion.div
          initial={{ scale: 0.5, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ type: 'spring', damping: 14 }}
        >
          <h1 className="text-center" style={{ fontSize: '2rem', background: 'linear-gradient(135deg, var(--nova-gold), var(--stellar-pink))', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            QUIZ COMPLETE! ✨
          </h1>
        </motion.div>

        {/* Score Ring */}
        <motion.div
          className="readiness-ring"
          style={{ width: 140, height: 140 }}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
        >
          <svg viewBox="0 0 140 140" width="140" height="140">
            <defs>
              <linearGradient id="scoreGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stopColor="#FFD700" />
                <stop offset="100%" stopColor="#FF6EB4" />
              </linearGradient>
            </defs>
            <circle cx="70" cy="70" r="54" stroke="rgba(255,255,255,0.08)" strokeWidth="8" fill="none" />
            <motion.circle
              cx="70" cy="70" r="54"
              stroke="url(#scoreGrad)" strokeWidth="8" fill="none"
              strokeLinecap="round"
              strokeDasharray={circumference}
              initial={{ strokeDashoffset: circumference }}
              animate={{ strokeDashoffset: circumference * (1 - pct / 100) }}
              transition={{ delay: 0.4, duration: 1.4, ease: [0.34, 1.56, 0.64, 1] }}
              filter="drop-shadow(0 0 8px #FFD700)"
            />
          </svg>
          <div className="readiness-ring" style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            <div className="readiness-pct" style={{ fontSize: '2rem' }}>{pct}%</div>
            <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--text-muted)' }}>SCORE</div>
          </div>
        </motion.div>

        {/* Stats Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, width: '100%' }}>
          {[
            { label: 'Correct', value: `✓ ${score.correct}`, color: 'var(--bio-green)' },
            { label: 'Wrong', value: `✗ ${score.wrong}`, color: 'var(--chem-red)' },
            { label: 'Avg Speed', value: '34s ⏱', color: 'var(--phys-blue)' },
            { label: 'XP Earned', value: `+${score.correct * 80} 💥`, color: 'var(--nova-gold)' },
          ].map(stat => (
            <motion.div
              key={stat.label}
              className="stat-card"
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.5 }}
            >
              <div className="stat-value" style={{ color: stat.color, fontSize: '1.3rem' }}>{stat.value}</div>
              <div className="stat-label">{stat.label}</div>
            </motion.div>
          ))}
        </div>

        {/* Nova Message */}
        <motion.div
          className="nova-border"
          style={{ padding: 2, width: '100%' }}
          initial={{ opacity: 0, scale: 0.97 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.7 }}
        >
          <div style={{ padding: '16px 20px', background: 'rgba(10,8,0,0.7)', borderRadius: 18 }}>
            <div style={{ fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.72rem', color: 'var(--nova-gold)', marginBottom: 8 }}>
              🩺 Nova says:
            </div>
            <p style={{ fontStyle: 'italic', fontSize: '0.88rem', color: 'var(--text-primary)', lineHeight: 1.5 }}>
              "{msg}"
            </p>
          </div>
        </motion.div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10, width: '100%' }}>
          <button className="btn-primary" style={{ width: '100%', fontSize: '0.9rem' }}>Review Wrong Answers 📚</button>
          <button className="btn-secondary" style={{ width: '100%', fontSize: '0.9rem' }}>New Quiz →</button>
        </div>

        <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.72rem', color: 'var(--stellar-pink)', textAlign: 'center' }}>
          Made specially for Suhana. My Doctor. 🩺
        </div>
      </div>
    </div>
  );
}
