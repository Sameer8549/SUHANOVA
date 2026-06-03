// Suhanova — Progress & Analytics Screen
import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';

const SUBJECT_STATS = [
  { name: 'Biology', emoji: '🧬', color: 'var(--bio-green)',  pct: 78 },
  { name: 'Physics', emoji: '⚡', color: 'var(--phys-blue)', pct: 65 },
  { name: 'Chem',    emoji: '🧪', color: 'var(--chem-red)',  pct: 71 },
  { name: 'Maths',   emoji: '📐', color: 'var(--nova-gold)', pct: 85 },
];

const WEEKLY_DATA = [
  { day: 'Mon', pct: 55 },
  { day: 'Tue', pct: 62 },
  { day: 'Wed', pct: 58 },
  { day: 'Thu', pct: 71 },
  { day: 'Fri', pct: 69 },
  { day: 'Sat', pct: 78 },
  { day: 'Sun', pct: 82 },
];

const WEAK_TOPICS = ['Membrane Transport', 'Thermodynamics', 'Organic Rxns', 'Optics', 'Genetics'];

function generateHeatmap() {
  const days = 35;
  return Array.from({ length: days }, (_, i) => ({
    idx: i,
    intensity: Math.random() > 0.3 ? Math.floor(Math.random() * 4) + 1 : 0,
  }));
}

const heatmapData = generateHeatmap();
const heatColors = ['rgba(255,255,255,0.05)', 'rgba(255,215,0,0.2)', 'rgba(255,215,0,0.45)', 'rgba(255,215,0,0.7)', '#FFD700'];

export default function ProgressScreen() {
  const [mounted, setMounted] = useState(false);
  const readiness = 72;
  const circumference = 2 * Math.PI * 65;

  useEffect(() => { setTimeout(() => setMounted(true), 100); }, []);

  return (
    <div className="page-wrapper">
      <div className="page-content">
        {/* Header */}
        <div style={{ paddingTop: 8 }}>
          <h2 style={{ fontSize: '1.5rem', marginBottom: 4 }}>Your Progress 📈</h2>
          <p style={{ fontSize: '0.85rem' }}>Suhana, you're getting stronger every day ⭐</p>
        </div>

        {/* NEET Readiness Ring */}
        <motion.div
          className="nova-border"
          style={{ padding: 2 }}
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ type: 'spring' }}
        >
          <div style={{
            padding: '24px 20px', background: 'rgba(10,8,0,0.7)', borderRadius: 18,
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16,
          }}>
            <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.72rem', fontWeight: 700, color: 'var(--nova-gold)', letterSpacing: '0.12em', textTransform: 'uppercase' }}>
              NEET Readiness Score
            </div>

            <div className="readiness-ring" style={{ width: 160, height: 160 }}>
              <svg viewBox="0 0 160 160" width="160" height="160">
                <defs>
                  <linearGradient id="readinessGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stopColor="#FFD700" />
                    <stop offset="100%" stopColor="#FF6EB4" />
                  </linearGradient>
                </defs>
                <circle cx="80" cy="80" r="65" stroke="rgba(255,255,255,0.06)" strokeWidth="10" fill="none" />
                <motion.circle
                  cx="80" cy="80" r="65"
                  stroke="url(#readinessGrad)" strokeWidth="10" fill="none"
                  strokeLinecap="round"
                  strokeDasharray={circumference}
                  initial={{ strokeDashoffset: circumference }}
                  animate={{ strokeDashoffset: circumference * (1 - (mounted ? readiness / 100 : 0)) }}
                  transition={{ delay: 0.3, duration: 1.6, ease: [0.34, 1.56, 0.64, 1] }}
                  filter="drop-shadow(0 0 10px #FFD700)"
                  style={{ transform: 'rotate(-90deg)', transformOrigin: '80px 80px' }}
                />
              </svg>
              <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                <div className="readiness-pct">{readiness}%</div>
                <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.62rem', color: 'var(--text-muted)' }}>READINESS</div>
              </div>
            </div>

            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', justifyContent: 'center' }}>
              <span className="chip chip-green">↑ +5% this week</span>
              <span className="chip chip-pink">🩺 83 days remaining</span>
            </div>
          </div>
        </motion.div>

        {/* Weekly Accuracy Chart */}
        <motion.div
          className="glass-card"
          style={{ padding: '18px 20px' }}
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          <div style={{ fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.82rem', color: 'var(--text-primary)', marginBottom: 16 }}>
            Accuracy This Week 📊
          </div>
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 6, height: 80 }}>
            {WEEKLY_DATA.map((d, i) => (
              <div key={d.day} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
                <motion.div
                  style={{
                    width: '100%', borderRadius: '4px 4px 0 0',
                    background: i === WEEKLY_DATA.length - 1
                      ? 'linear-gradient(180deg, var(--nova-gold), var(--stellar-pink))'
                      : 'rgba(255,215,0,0.3)',
                    boxShadow: i === WEEKLY_DATA.length - 1 ? '0 0 12px var(--nova-gold-glow)' : 'none',
                  }}
                  initial={{ height: 0 }}
                  animate={{ height: `${(d.pct / 100) * 70}px` }}
                  transition={{ delay: 0.3 + i * 0.06, type: 'spring' }}
                />
                <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.6rem', color: 'var(--text-muted)' }}>{d.day}</div>
              </div>
            ))}
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 8 }}>
            <span style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--text-muted)' }}>Mon 55%</span>
            <span style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--nova-gold)', fontWeight: 700 }}>Today 82% 🔥</span>
          </div>
        </motion.div>

        {/* Subject Accuracy */}
        <motion.div
          className="glass-card"
          style={{ padding: '18px 20px' }}
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.35 }}
        >
          <div style={{ fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.82rem', color: 'var(--text-primary)', marginBottom: 14 }}>
            Subject Breakdown
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {SUBJECT_STATS.map((s, i) => (
              <div key={s.name}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 5 }}>
                  <span style={{ fontFamily: 'var(--font-label)', fontSize: '0.78rem', color: s.color }}>
                    {s.emoji} {s.name}
                  </span>
                  <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '0.82rem', color: s.color }}>{s.pct}%</span>
                </div>
                <div className="progress-bar">
                  <motion.div
                    style={{ height: '100%', borderRadius: '9999px', background: s.color, boxShadow: `0 0 8px ${s.color}50` }}
                    initial={{ width: 0 }}
                    animate={{ width: mounted ? `${s.pct}%` : 0 }}
                    transition={{ delay: 0.4 + i * 0.1, duration: 1, ease: [0.34, 1.56, 0.64, 1] }}
                  />
                </div>
              </div>
            ))}
          </div>
        </motion.div>

        {/* Heatmap */}
        <motion.div
          className="glass-card"
          style={{ padding: '18px 20px' }}
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.45 }}
        >
          <div style={{ fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.82rem', color: 'var(--text-primary)', marginBottom: 12 }}>
            Study Consistency 🔥
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 4 }}>
            {heatmapData.map(cell => (
              <motion.div
                key={cell.idx}
                style={{
                  aspectRatio: '1', borderRadius: 4,
                  background: heatColors[cell.intensity],
                  boxShadow: cell.intensity >= 3 ? '0 0 6px rgba(255,215,0,0.4)' : 'none',
                }}
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ delay: 0.5 + cell.idx * 0.01 }}
              />
            ))}
          </div>
          <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--text-muted)', marginTop: 8, textAlign: 'right' }}>
            Last 35 days
          </div>
        </motion.div>

        {/* Weak Topics */}
        <motion.div
          className="glass-card"
          style={{ padding: '16px 20px' }}
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.55 }}
        >
          <div style={{ fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.82rem', color: 'var(--stellar-pink)', marginBottom: 10 }}>
            🎯 Focus Areas — Nova suggests these first
          </div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {WEAK_TOPICS.map(t => (
              <span key={t} className="chip chip-pink" style={{ fontSize: '0.7rem' }}>{t}</span>
            ))}
          </div>
        </motion.div>
      </div>
    </div>
  );
}
