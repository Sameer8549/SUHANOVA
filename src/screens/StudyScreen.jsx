// Suhanova — Study Screen (Subject Rooms + Flashcards + Planner)
import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

// ─── FLASHCARD MODE ───────────────────────────────────────────
const FLASHCARDS = [
  { id: 1, term: 'MITOSIS',    subject: 'Biology', def: 'Cell division producing 2 identical daughter cells with same chromosome number as parent. Process: PMAT — Prophase, Metaphase, Anaphase, Telophase.', emoji: '🧬' },
  { id: 2, term: 'OSMOSIS',    subject: 'Biology', def: 'Movement of water molecules through a semi-permeable membrane from high water concentration (dilute) to low water concentration (concentrated).', emoji: '💧' },
  { id: 3, term: 'ATP',        subject: 'Biology', def: 'Adenosine Triphosphate — the energy currency of the cell. Produced in mitochondria during cellular respiration. 36-38 ATP per glucose molecule.', emoji: '⚡' },
  { id: 4, term: 'NEWTON\'S 1ST', subject: 'Physics', def: 'Law of Inertia: An object at rest stays at rest, and an object in motion stays in motion, unless acted upon by an external force.', emoji: '🔵' },
  { id: 5, term: 'PLASMOLYSIS', subject: 'Biology', def: 'Shrinkage of protoplasm when a plant cell is placed in hypertonic solution — water moves OUT of the cell due to osmosis.', emoji: '🌱' },
];

function FlashcardMode() {
  const [cardIndex, setCardIndex] = useState(0);
  const [flipped, setFlipped] = useState(false);
  const [ratings, setRatings] = useState({ easy: 0, okay: 0, hard: 0 });
  const [done, setDone] = useState([]);
  const card = FLASHCARDS[cardIndex];

  const rate = (rating) => {
    setRatings(r => ({ ...r, [rating]: r[rating] + 1 }));
    setDone(d => [...d, card.id]);
    setFlipped(false);
    setTimeout(() => {
      setCardIndex(i => (i + 1) % FLASHCARDS.length);
    }, 300);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span className="chip chip-green">{card.subject}</span>
        <span style={{ fontFamily: 'var(--font-label)', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
          {cardIndex + 1} / {FLASHCARDS.length}
        </span>
      </div>

      {/* Card */}
      <div className="flashcard-scene" onClick={() => setFlipped(f => !f)}>
        <div className={`flashcard-inner ${flipped ? 'flipped' : ''}`}>
          <div className="flashcard-face" style={{ borderColor: 'var(--nova-gold)', boxShadow: '0 0 30px rgba(255,215,0,0.15)' }}>
            <span style={{ fontSize: '2rem', marginBottom: 12 }}>{card.emoji}</span>
            <div className="flashcard-term">{card.term}</div>
            <div className="flashcard-hint">TAP TO FLIP</div>
          </div>
          <div className="flashcard-face flashcard-back" style={{ borderColor: 'var(--stellar-pink)', boxShadow: '0 0 30px rgba(255,110,180,0.15)' }}>
            <div style={{ fontFamily: 'var(--font-body)', fontSize: '0.92rem', color: 'var(--text-primary)', lineHeight: 1.6, textAlign: 'center' }}>
              {card.def}
            </div>
          </div>
        </div>
      </div>

      {/* Ratings */}
      <div className="rating-row">
        {[
          { key: 'hard', label: 'Hard',  emoji: '😵', cls: 'hard' },
          { key: 'okay', label: 'Okay',  emoji: '🤔', cls: 'okay' },
          { key: 'easy', label: 'Easy!', emoji: '😄', cls: 'easy' },
        ].map(r => (
          <button key={r.key} className={`rating-btn ${r.cls}`} onClick={() => rate(r.key)}>
            <span style={{ fontSize: '1.2rem' }}>{r.emoji}</span>
            {r.label}
          </button>
        ))}
      </div>

      {/* Progress */}
      <div className="glass-card" style={{ padding: '14px 18px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
          <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.78rem', fontWeight: 700, color: 'var(--text-primary)' }}>
            🔥 Daily Goal: 20 cards
          </div>
          <span className="chip chip-gold" style={{ fontSize: '0.65rem' }}>{done.length}/20</span>
        </div>
        <div className="progress-bar">
          <div className="progress-fill" style={{ width: `${(done.length / 20) * 100}%` }} />
        </div>
        <div style={{ display: 'flex', gap: 8, marginTop: 10, flexWrap: 'wrap' }}>
          <span className="chip chip-green" style={{ fontSize: '0.65rem' }}>✓ Easy: {ratings.easy}</span>
          <span className="chip chip-gold" style={{ fontSize: '0.65rem' }}>∼ Okay: {ratings.okay}</span>
          <span className="chip chip-red"  style={{ fontSize: '0.65rem' }}>✗ Hard: {ratings.hard}</span>
        </div>
      </div>
    </div>
  );
}

// ─── SUBJECT ROOMS ────────────────────────────────────────────
const SUBJECTS_DATA = [
  {
    id: 'biology', name: 'Biology', emoji: '🧬', color: 'var(--bio-green)', glow: 'var(--bio-green-glow)',
    pct: 42, chapters: 45,
    units: [
      { name: 'Cell Biology', chapters: [
        { name: 'Cell Theory', status: 'done', notes: 24, cards: 15 },
        { name: 'Cell Structure', status: 'progress', notes: 18, cards: 12 },
        { name: 'Cell Division', status: 'locked', notes: 22, cards: 0 },
      ]},
      { name: 'Genetics', chapters: [
        { name: 'Mendelian Genetics', status: 'pending', notes: 30, cards: 20 },
        { name: 'DNA Structure', status: 'pending', notes: 28, cards: 18 },
      ]},
    ],
  },
  {
    id: 'physics', name: 'Physics', emoji: '⚡', color: 'var(--phys-blue)', glow: 'var(--phys-blue-glow)',
    pct: 38, chapters: 38,
    units: [
      { name: 'Mechanics', chapters: [
        { name: 'Motion in a Straight Line', status: 'done', notes: 20, cards: 15 },
        { name: 'Laws of Motion', status: 'done', notes: 22, cards: 18 },
        { name: 'Work, Energy & Power', status: 'progress', notes: 19, cards: 12 },
      ]},
    ],
  },
  {
    id: 'chemistry', name: 'Chemistry', emoji: '🧪', color: 'var(--chem-red)', glow: 'var(--chem-red-glow)',
    pct: 55, chapters: 30,
    units: [
      { name: 'Physical Chemistry', chapters: [
        { name: 'Basic Concepts', status: 'done', notes: 18, cards: 12 },
        { name: 'Atomic Structure', status: 'done', notes: 20, cards: 16 },
        { name: 'Chemical Bonding', status: 'progress', notes: 22, cards: 10 },
      ]},
    ],
  },
  {
    id: 'maths', name: 'Maths', emoji: '📐', color: 'var(--math-gold)', glow: 'var(--nova-gold-glow)',
    pct: 71, chapters: 16,
    units: [
      { name: 'Calculus', chapters: [
        { name: 'Limits & Continuity', status: 'done', notes: 24, cards: 20 },
        { name: 'Differentiation', status: 'done', notes: 28, cards: 22 },
        { name: 'Integration', status: 'progress', notes: 20, cards: 15 },
      ]},
    ],
  },
];

const STATUS_MAP = {
  done:     { icon: '✅', label: 'Completed', color: 'var(--bio-green)' },
  progress: { icon: '🔵', label: 'In Progress', color: 'var(--phys-blue)' },
  pending:  { icon: '📚', label: 'Not Started', color: 'var(--text-muted)' },
  locked:   { icon: '🔒', label: 'Locked', color: 'var(--text-muted)' },
};

function SubjectRoom({ subject, onBack }) {
  return (
    <div>
      <button className="btn-ghost" onClick={onBack} style={{ marginBottom: 12, display: 'flex', alignItems: 'center', gap: 6 }}>
        ← Back
      </button>

      {/* Hero Banner */}
      <div
        className="glass-card"
        style={{
          padding: '20px', marginBottom: 16,
          border: `1px solid ${subject.color}50`,
          boxShadow: `0 0 30px ${subject.glow}, 0 0 60px ${subject.glow}`,
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <h3 style={{ color: subject.color, fontSize: '1.6rem', marginBottom: 4 }}>
              {subject.emoji} {subject.name}
            </h3>
            <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
              NEET 2025 · {subject.chapters} chapters
            </div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.6rem', color: subject.color }}>
              {subject.pct}%
            </div>
            <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.62rem', color: 'var(--text-muted)' }}>complete</div>
          </div>
        </div>
        <div className="progress-bar" style={{ marginTop: 12 }}>
          <div className="progress-fill" style={{ width: `${subject.pct}%`, background: subject.color }} />
        </div>
      </div>

      {/* Units */}
      {subject.units.map(unit => (
        <div key={unit.name} style={{ marginBottom: 16 }}>
          <div style={{
            fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.75rem',
            color: subject.color, letterSpacing: '0.1em', textTransform: 'uppercase',
            marginBottom: 10, display: 'flex', alignItems: 'center', gap: 8,
          }}>
            <div style={{ flex: 1, height: 1, background: `${subject.color}30` }} />
            {unit.name}
            <div style={{ flex: 1, height: 1, background: `${subject.color}30` }} />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {unit.chapters.map(ch => {
              const st = STATUS_MAP[ch.status];
              return (
                <motion.div
                  key={ch.name}
                  className="task-card"
                  style={{ cursor: ch.status !== 'locked' ? 'pointer' : 'default' }}
                  whileTap={ch.status !== 'locked' ? { scale: 0.98 } : {}}
                >
                  <span style={{ fontSize: '1.1rem' }}>{st.icon}</span>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontFamily: 'var(--font-body)', fontWeight: 500, fontSize: '0.88rem', color: ch.status === 'locked' ? 'var(--text-muted)' : 'var(--text-primary)' }}>
                      {ch.name}
                    </div>
                    {ch.status !== 'locked' && (
                      <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--text-muted)', marginTop: 2 }}>
                        📚 {ch.notes} notes · ✨ {ch.cards} flashcards
                      </div>
                    )}
                  </div>
                  {ch.status !== 'locked' && (
                    <span style={{ color: subject.color, fontSize: '0.9rem' }}>›</span>
                  )}
                </motion.div>
              );
            })}
          </div>
        </div>
      ))}

      {/* AI Quiz FAB */}
      <div style={{ position: 'fixed', bottom: 100, right: 24 }}>
        <motion.button
          className="btn-primary"
          style={{ padding: '14px 20px', borderRadius: 20, boxShadow: '0 0 30px rgba(255,215,0,0.5)' }}
          whileTap={{ scale: 0.93 }}
          animate={{ y: [0, -4, 0] }}
          transition={{ duration: 2.5, repeat: Infinity }}
        >
          ✨ AI Quiz
        </motion.button>
      </div>
    </div>
  );
}

// ─── STUDY SCREEN MAIN ────────────────────────────────────────
const STUDY_TABS = ['Subjects', 'Flashcards', 'Planner'];

const SCHEDULE = [
  { emoji: '🧬', subject: 'Biology', topic: 'Cell Division', time: '9:00 – 10:30', status: 'done', color: 'var(--bio-green)' },
  { emoji: '⚡', subject: 'Physics', topic: 'Optics', time: '11:00 – 12:30', status: 'active', color: 'var(--phys-blue)' },
  { emoji: '🧪', subject: 'Chemistry', topic: 'Organic Reactions', time: '2:00 – 3:30', status: 'pending', color: 'var(--chem-red)' },
  { emoji: '📐', subject: 'Maths', topic: 'Calculus Revision', time: '4:00 – 5:00', status: 'pending', color: 'var(--math-gold)' },
];

const WEEK = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const TODAY_IDX = 1; // Tuesday

export default function StudyScreen() {
  const [activeTab, setActiveTab] = useState('Subjects');
  const [activeSubject, setActiveSubject] = useState(null);
  const [pomodoroTime, setPomodoroTime] = useState(25 * 60);
  const [pomodoroRunning, setPomodoroRunning] = useState(false);

  const formatPomodoro = (s) => `${String(Math.floor(s / 60)).padStart(2,'0')}:${String(s % 60).padStart(2,'0')}`;
  const pomoPct = pomodoroTime / (25 * 60);

  return (
    <div className="page-wrapper">
      <div className="page-content">
        <div style={{ paddingTop: 8 }}>
          <h2 style={{ fontSize: '1.4rem', marginBottom: 14 }}>Study 📚</h2>
        </div>

        {/* Tab Bar */}
        {!activeSubject && (
          <div style={{ display: 'flex', gap: 8, background: 'rgba(255,255,255,0.04)', padding: 4, borderRadius: 12 }}>
            {STUDY_TABS.map(tab => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                style={{
                  flex: 1, padding: '10px 4px', borderRadius: 10, border: 'none', cursor: 'pointer',
                  fontFamily: 'var(--font-label)', fontWeight: 600, fontSize: '0.8rem',
                  background: activeTab === tab ? 'var(--nova-gold)' : 'transparent',
                  color: activeTab === tab ? 'var(--bg-space)' : 'var(--text-muted)',
                  transition: 'all 0.2s',
                }}
              >
                {tab}
              </button>
            ))}
          </div>
        )}

        {/* Content */}
        <AnimatePresence mode="wait">
          {activeTab === 'Subjects' && !activeSubject && (
            <motion.div key="subjects" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {SUBJECTS_DATA.map(subj => (
                  <motion.div
                    key={subj.id}
                    className="glass-card"
                    style={{ padding: '16px 18px', cursor: 'pointer', borderColor: `${subj.color}40`, boxShadow: `0 0 20px ${subj.glow}` }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => setActiveSubject(subj)}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                        <span style={{ fontSize: '1.6rem' }}>{subj.emoji}</span>
                        <div>
                          <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1rem', color: subj.color }}>{subj.name}</div>
                          <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--text-muted)' }}>{subj.chapters} chapters</div>
                        </div>
                      </div>
                      <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.3rem', color: subj.color }}>{subj.pct}%</div>
                    </div>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{ width: `${subj.pct}%`, background: subj.color }} />
                    </div>
                  </motion.div>
                ))}
              </div>
            </motion.div>
          )}

          {activeTab === 'Subjects' && activeSubject && (
            <motion.div key="subject-room" initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -20 }}>
              <SubjectRoom subject={activeSubject} onBack={() => setActiveSubject(null)} />
            </motion.div>
          )}

          {activeTab === 'Flashcards' && (
            <motion.div key="flashcards" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
              <FlashcardMode />
            </motion.div>
          )}

          {activeTab === 'Planner' && (
            <motion.div key="planner" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
              {/* NEET Countdown */}
              <div className="nova-border" style={{ padding: 2 }}>
                <div style={{ padding: '16px 20px', background: 'rgba(10,8,0,0.8)', borderRadius: 18, textAlign: 'center' }}>
                  <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.68rem', color: 'var(--nova-gold)', letterSpacing: '0.12em', marginBottom: 4 }}>🩺 NEET 2025</div>
                  <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '3.5rem', color: 'var(--nova-gold)', lineHeight: 1 }}>83</div>
                  <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: 8 }}>days remaining</div>
                  <div style={{ fontFamily: 'var(--font-body)', fontStyle: 'italic', fontSize: '0.82rem', color: 'var(--stellar-pink)' }}>
                    "Suhana, the stethoscope is already yours."
                  </div>
                </div>
              </div>

              {/* Week Calendar */}
              <div>
                <div className="section-title">This Week</div>
                <div style={{ display: 'flex', gap: 6, overflowX: 'auto', paddingBottom: 4, scrollbarWidth: 'none' }}>
                  {WEEK.map((day, i) => (
                    <div key={day} className={`day-pill ${i === TODAY_IDX ? 'today' : ''}`} style={{ minWidth: 52 }}>
                      <span style={{ fontSize: '0.75rem', fontWeight: i === TODAY_IDX ? 700 : 500 }}>{day}</span>
                      {i === TODAY_IDX && <span style={{ fontSize: '0.55rem' }}>today</span>}
                    </div>
                  ))}
                </div>
              </div>

              {/* Today's Schedule */}
              <div>
                <div className="section-title">Tuesday Schedule</div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {SCHEDULE.map(task => (
                    <div key={task.topic} className={`task-card ${task.status === 'done' ? 'done' : ''}`}>
                      <div className="task-dot" style={{ background: task.color }} />
                      <span style={{ fontSize: '1.1rem' }}>{task.emoji}</span>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontFamily: 'var(--font-body)', fontSize: '0.88rem', fontWeight: 500, color: task.status === 'done' ? 'var(--text-muted)' : 'var(--text-primary)' }}>
                          {task.subject} · {task.topic}
                          {task.status === 'done' && ' ✅'}
                          {task.status === 'active' && ' 🔵'}
                        </div>
                        <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--text-muted)' }}>{task.time}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Pomodoro Timer */}
              <div className="glass-card" style={{ padding: '20px', textAlign: 'center' }}>
                <div style={{ fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.78rem', color: 'var(--nova-gold)', letterSpacing: '0.1em', marginBottom: 16 }}>
                  FOCUS SESSION 🍅
                </div>
                <div className="circular-timer" style={{ width: 100, height: 100, margin: '0 auto 16px' }}>
                  <svg viewBox="0 0 100 100" width="100" height="100">
                    <circle cx="50" cy="50" r="42" stroke="rgba(255,255,255,0.06)" strokeWidth="7" fill="none" />
                    <circle
                      cx="50" cy="50" r="42"
                      stroke="var(--nova-gold)" strokeWidth="7" fill="none"
                      strokeLinecap="round"
                      strokeDasharray={2 * Math.PI * 42}
                      strokeDashoffset={2 * Math.PI * 42 * (1 - pomoPct)}
                      filter="drop-shadow(0 0 6px var(--nova-gold))"
                      style={{ transform: 'rotate(-90deg)', transformOrigin: '50px 50px' }}
                    />
                  </svg>
                  <div className="timer-text">
                    <span style={{ fontSize: '1.3rem', fontFamily: 'var(--font-display)', fontWeight: 800 }}>
                      {formatPomodoro(pomodoroTime)}
                    </span>
                  </div>
                </div>
                <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.72rem', color: 'var(--text-muted)', marginBottom: 12 }}>
                  Physics · Optics Chapter
                </div>
                <div style={{ display: 'flex', gap: 12, justifyContent: 'center', marginBottom: 14 }}>
                  {['🌧️', '🎵', '📚'].map(icon => (
                    <button key={icon} className="btn-ghost" style={{ fontSize: '1.2rem', padding: 8 }}>{icon}</button>
                  ))}
                </div>
                <div style={{ display: 'flex', gap: 10 }}>
                  <button className="btn-primary" style={{ flex: 1, padding: '12px' }} onClick={() => setPomodoroRunning(r => !r)}>
                    {pomodoroRunning ? '⏸ Pause' : '▶ Start'}
                  </button>
                  <button className="btn-secondary" style={{ padding: '12px 16px' }} onClick={() => setPomodoroTime(25 * 60)}>↺</button>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
