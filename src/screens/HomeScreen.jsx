// Suhanova — Home Dashboard Screen
import { motion } from 'framer-motion';
import NovaMomentCard from '../components/NovaMomentCard';

const SUBJECTS = [
  { id: 'biology',   emoji: '🧬', name: 'Biology',  color: 'var(--bio-green)',   glow: 'var(--bio-green-glow)',  pct: 42 },
  { id: 'physics',   emoji: '⚡', name: 'Physics',  color: 'var(--phys-blue)',   glow: 'var(--phys-blue-glow)', pct: 38 },
  { id: 'chemistry', emoji: '🧪', name: 'Chem',     color: 'var(--chem-red)',    glow: 'var(--chem-red-glow)',  pct: 55 },
  { id: 'maths',     emoji: '📐', name: 'Maths',    color: 'var(--math-gold)',   glow: 'var(--nova-gold-glow)', pct: 71 },
];

const QUICK_ACTIONS = [
  { emoji: '✨', label: "Today's Quiz",  color: 'var(--nova-gold)'   },
  { emoji: '📊', label: 'Weak Areas',    color: 'var(--stellar-pink)' },
  { emoji: '🃏', label: 'Flashcards',   color: 'var(--bio-green)'   },
];

const container = { hidden: { opacity: 0 }, show: { opacity: 1, transition: { staggerChildren: 0.08 } } };
const item = { hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0 } };

export default function HomeScreen({ onNavigate }) {
  return (
    <div className="page-wrapper">
      <div className="page-content">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: 8 }}
        >
          <div>
            <div style={{
              fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.6rem',
              background: 'linear-gradient(135deg, var(--nova-gold), var(--stellar-pink))',
              WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
            }}>
              SUHANOVA
            </div>
            <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.7rem', color: 'var(--text-muted)', letterSpacing: '0.1em' }}>
              RISE LIKE A NOVA ✦
            </div>
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{
              width: 44, height: 44, borderRadius: '50%',
              background: 'linear-gradient(135deg, var(--nova-gold), var(--stellar-pink))',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.1rem',
              color: 'var(--bg-space)', boxShadow: '0 0 20px var(--nova-gold-glow)',
            }}>
              S
            </div>
            <div style={{
              position: 'absolute', top: -3, right: -3,
              width: 14, height: 14, borderRadius: '50%',
              background: 'var(--bio-green)', border: '2px solid var(--bg-space)',
              boxShadow: '0 0 8px var(--bio-green)',
            }} />
          </div>
        </motion.div>

        {/* Nova Moment Card */}
        <NovaMomentCard streak={12} />

        {/* Subject Planets */}
        <div>
          <div className="section-title">Subject Rooms</div>
          <motion.div
            className="subject-grid"
            variants={container}
            initial="hidden"
            animate="show"
          >
            {SUBJECTS.map(subj => (
              <motion.div
                key={subj.id}
                className="subject-card"
                variants={item}
                style={{
                  borderColor: subj.color,
                  boxShadow: `0 0 20px ${subj.glow}`,
                }}
                whileTap={{ scale: 0.92 }}
                onClick={() => onNavigate('study')}
              >
                <span className="emoji">{subj.emoji}</span>
                <span className="name" style={{ color: subj.color }}>{subj.name}</span>
                <div className="progress-bar" style={{ width: '100%' }}>
                  <div className="progress-fill" style={{ width: `${subj.pct}%`, background: subj.color, boxShadow: `0 0 6px ${subj.glow}` }} />
                </div>
                <span style={{ fontSize: '0.6rem', color: 'var(--text-muted)', fontFamily: 'var(--font-label)' }}>{subj.pct}%</span>
              </motion.div>
            ))}
          </motion.div>
        </div>

        {/* Today's Target */}
        <motion.div className="glass-card" style={{ padding: '16px 20px' }} variants={item} initial="hidden" animate="show">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
            <div>
              <div style={{ fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.85rem', color: 'var(--text-primary)' }}>Today's Target</div>
              <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.72rem', color: 'var(--text-muted)' }}>3 of 5 topics complete</div>
            </div>
            <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.4rem', color: 'var(--nova-gold)' }}>60%</div>
          </div>
          <div className="progress-bar" style={{ height: 8 }}>
            <motion.div
              className="progress-fill"
              initial={{ width: 0 }}
              animate={{ width: '60%' }}
              transition={{ delay: 0.5, duration: 1, ease: [0.34, 1.56, 0.64, 1] }}
            />
          </div>
        </motion.div>

        {/* Resume Last Topic */}
        <motion.div
          className="glass-card"
          style={{ padding: '16px 20px', cursor: 'pointer' }}
          whileTap={{ scale: 0.98 }}
          onClick={() => onNavigate('study')}
          initial={{ opacity: 0, x: -16 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3 }}
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
              <div style={{
                width: 44, height: 44, borderRadius: 10,
                background: 'rgba(0,255,150,0.12)', border: '1px solid rgba(0,255,150,0.3)',
                display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.3rem',
              }}>🧬</div>
              <div>
                <div style={{ fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.8rem', color: 'var(--text-muted)', letterSpacing: '0.06em', marginBottom: 2 }}>RESUME LAST TOPIC</div>
                <div style={{ fontFamily: 'var(--font-body)', fontWeight: 500, fontSize: '0.9rem', color: 'var(--text-primary)' }}>Cell Division — Chapter 3</div>
                <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.7rem', color: 'var(--bio-green)' }}>Biology · 65% done</div>
              </div>
            </div>
            <div style={{ fontSize: '1.2rem', color: 'var(--nova-gold)' }}>›</div>
          </div>
        </motion.div>

        {/* Quick Actions */}
        <div>
          <div className="section-title">Quick Actions</div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10 }}>
            {QUICK_ACTIONS.map(action => (
              <motion.button
                key={action.label}
                className="glass-card"
                style={{
                  padding: '14px 8px', cursor: 'pointer', border: 'none',
                  display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6,
                }}
                whileTap={{ scale: 0.93 }}
                onClick={() => action.label.includes('Quiz') && onNavigate('quiz')}
              >
                <span style={{ fontSize: '1.4rem' }}>{action.emoji}</span>
                <span style={{
                  fontFamily: 'var(--font-label)', fontSize: '0.68rem', fontWeight: 600,
                  color: action.color, textAlign: 'center', letterSpacing: '0.04em',
                }}>{action.label}</span>
              </motion.button>
            ))}
          </div>
        </div>

        {/* Streak Card */}
        <motion.div
          className="glass-card"
          style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: 16 }}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.6 }}
        >
          <div className="streak-ring" style={{ width: 60, height: 60, flexShrink: 0 }}>
            <svg viewBox="0 0 60 60" width="60" height="60">
              <defs>
                <linearGradient id="streakGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stopColor="#FFD700" />
                  <stop offset="100%" stopColor="#FF6EB4" />
                </linearGradient>
              </defs>
              <circle className="ring-bg" cx="30" cy="30" r="24" strokeWidth="5" />
              <circle
                className="ring-fill"
                cx="30" cy="30" r="24"
                strokeWidth="5"
                strokeDasharray={`${2 * Math.PI * 24 * 0.85} ${2 * Math.PI * 24}`}
                stroke="url(#streakGrad)"
              />
            </svg>
            <div style={{
              position: 'relative', top: -50, left: 0, width: 60, height: 50,
              display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
            }}>
              <span style={{ fontSize: '0.8rem' }}>🔥</span>
              <span style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '0.8rem', color: 'var(--nova-gold)', lineHeight: 1 }}>12</span>
            </div>
          </div>
          <div>
            <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1rem', color: 'var(--text-primary)' }}>12-Day Streak 🔥</div>
            <div style={{ fontFamily: 'var(--font-body)', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>Suhana, you are unstoppable.</div>
            <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.7rem', color: 'var(--nova-gold)', marginTop: 4 }}>3 more days for the 7-Day Nova badge!</div>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
