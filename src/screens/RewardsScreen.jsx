// Suhanova — Rewards & Nova Ranks Screen
import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const BADGES = [
  { emoji: '🧠', name: 'Biology Beast',   color: 'var(--bio-green)',   locked: false, desc: '100% Biology accuracy' },
  { emoji: '⚡', name: 'Speed Solver',    color: 'var(--phys-blue)',   locked: false, desc: '10 questions under 5s' },
  { emoji: '🔥', name: '7-Day Nova',      color: '#FF8C00',            locked: false, desc: '7 day streak' },
  { emoji: '🌙', name: 'Midnight Scholar',color: '#9B59B6',            locked: false, desc: 'Open app 12AM-4AM' },
  { emoji: '🩺', name: 'Future Doctor',   color: 'var(--nova-gold)',   locked: true,  desc: 'Complete all NEET chapters' },
  { emoji: '💫', name: 'Nova Master',     color: '#FF6EB4',            locked: true,  desc: 'Reach Level 50' },
];

const RANKS = [
  { name: 'Nova Spark',     level: 1,   xpNeeded: 500   },
  { name: 'Nova Scholar',   level: 14,  xpNeeded: 3500  },
  { name: 'Nova Champion',  level: 20,  xpNeeded: 8000  },
  { name: 'Nova Master',    level: 35,  xpNeeded: 18000 },
  { name: 'Future Doctor',  level: 50,  xpNeeded: 50000 },
];

export default function RewardsScreen() {
  const [showLevelUp, setShowLevelUp] = useState(false);
  const currentXP = 2840;
  const nextXP = 3500;
  const xpPct = (currentXP / nextXP) * 100;

  return (
    <div className="page-wrapper">
      <div className="page-content">
        {/* Header */}
        <div style={{ paddingTop: 8 }}>
          <h2 style={{ fontSize: '1.5rem', marginBottom: 4 }}>Nova Ranks 🏆</h2>
          <p style={{ fontSize: '0.85rem' }}>Every session makes you stronger, Suhana.</p>
        </div>

        {/* Rank Card */}
        <motion.div
          className="nova-border pulse-glow"
          style={{ padding: 2 }}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ type: 'spring' }}
        >
          <div style={{
            padding: '24px 20px', background: 'rgba(10,8,0,0.8)', borderRadius: 18,
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12, textAlign: 'center',
          }}>
            <motion.div
              className="float-anim"
              style={{ fontSize: '3rem', filter: 'drop-shadow(0 0 20px var(--nova-gold))' }}
            >
              👑
            </motion.div>
            <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.68rem', fontWeight: 700, color: 'var(--text-muted)', letterSpacing: '0.14em', textTransform: 'uppercase' }}>
              CURRENT RANK
            </div>
            <div style={{
              fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '2rem',
              background: 'linear-gradient(135deg, var(--nova-gold), var(--stellar-pink))',
              WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
            }}>
              NOVA SCHOLAR
            </div>
            <span className="chip chip-pink" style={{ fontSize: '0.78rem' }}>Level 14</span>

            {/* XP Bar */}
            <div style={{ width: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                <span style={{ fontFamily: 'var(--font-label)', fontSize: '0.7rem', color: 'var(--nova-gold)', fontWeight: 700 }}>
                  {currentXP.toLocaleString()} XP
                </span>
                <span style={{ fontFamily: 'var(--font-label)', fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                  {nextXP.toLocaleString()} XP → Nova Champion
                </span>
              </div>
              <div className="xp-bar-track">
                <motion.div
                  className="xp-bar-fill"
                  initial={{ width: 0 }}
                  animate={{ width: `${xpPct}%` }}
                  transition={{ delay: 0.4, duration: 1.4, ease: [0.34, 1.56, 0.64, 1] }}
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: 8 }}>
              <span className="chip chip-green">↑ +320 XP today ✨</span>
            </div>
          </div>
        </motion.div>

        {/* Level Up Preview Button */}
        <motion.button
          className="btn-primary"
          style={{ width: '100%' }}
          whileTap={{ scale: 0.97 }}
          onClick={() => setShowLevelUp(true)}
        >
          Preview Level Up Celebration 🎉
        </motion.button>

        {/* Badges Grid */}
        <div>
          <div className="section-title">Earned Badges</div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10 }}>
            {BADGES.map((badge, i) => (
              <motion.div
                key={badge.name}
                className={`badge-card ${badge.locked ? 'locked' : ''}`}
                style={{ borderColor: badge.locked ? 'rgba(255,255,255,0.1)' : badge.color + '50', boxShadow: badge.locked ? 'none' : `0 0 16px ${badge.color}25` }}
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: badge.locked ? 0.4 : 1, scale: 1 }}
                transition={{ delay: 0.1 * i, type: 'spring' }}
                whileTap={!badge.locked ? { scale: 0.94 } : {}}
              >
                <div className="badge-emoji">{badge.locked ? '🔒' : badge.emoji}</div>
                <div className="badge-name" style={{ color: badge.locked ? 'var(--text-muted)' : badge.color }}>
                  {badge.name}
                </div>
                <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.58rem', color: 'var(--text-muted)', textAlign: 'center', lineHeight: 1.3 }}>
                  {badge.desc}
                </div>
              </motion.div>
            ))}
          </div>
        </div>

        {/* Rank Progression */}
        <motion.div
          className="glass-card"
          style={{ padding: '18px 20px' }}
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
        >
          <div style={{ fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.82rem', color: 'var(--text-primary)', marginBottom: 14 }}>
            Nova Rank Progression
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {RANKS.map((rank, i) => {
              const isCurrent = rank.name === 'Nova Scholar';
              const isPast = i < 1;
              return (
                <div key={rank.name} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <div style={{
                    width: 32, height: 32, borderRadius: '50%', flexShrink: 0,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    background: isPast ? 'rgba(0,255,150,0.15)' : isCurrent ? 'rgba(255,215,0,0.2)' : 'rgba(255,255,255,0.05)',
                    border: `2px solid ${isPast ? 'var(--bio-green)' : isCurrent ? 'var(--nova-gold)' : 'rgba(255,255,255,0.1)'}`,
                    fontSize: '0.75rem',
                  }}>
                    {isPast ? '✓' : isCurrent ? '★' : rank.level}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{
                      fontFamily: 'var(--font-label)', fontWeight: isCurrent ? 700 : 500, fontSize: '0.82rem',
                      color: isCurrent ? 'var(--nova-gold)' : isPast ? 'var(--bio-green)' : 'var(--text-secondary)',
                    }}>
                      {rank.name} {isCurrent && '← You are here'}
                    </div>
                    <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.62rem', color: 'var(--text-muted)' }}>
                      {rank.xpNeeded.toLocaleString()} XP · Level {rank.level}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </motion.div>
      </div>

      {/* Level Up Overlay */}
      <AnimatePresence>
        {showLevelUp && (
          <motion.div
            style={{
              position: 'fixed', inset: 0, zIndex: 200,
              background: 'rgba(0,0,0,0.92)',
              display: 'flex', flexDirection: 'column',
              alignItems: 'center', justifyContent: 'center',
              padding: 32, textAlign: 'center', gap: 20,
            }}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            {/* Confetti-like particles */}
            {Array.from({ length: 20 }, (_, i) => (
              <motion.div
                key={i}
                style={{
                  position: 'absolute',
                  width: 8, height: 8, borderRadius: '50%',
                  background: i % 2 === 0 ? 'var(--nova-gold)' : 'var(--stellar-pink)',
                  left: `${Math.random() * 100}%`,
                  top: `${Math.random() * 100}%`,
                }}
                initial={{ scale: 0, opacity: 0 }}
                animate={{ scale: [0, 1.5, 0], opacity: [0, 1, 0], y: [-20, 20] }}
                transition={{ delay: Math.random() * 0.5, duration: 2, repeat: Infinity }}
              />
            ))}

            <motion.div
              className="float-anim"
              style={{ fontSize: '4rem', filter: 'drop-shadow(0 0 30px var(--nova-gold))' }}
              initial={{ scale: 0 }}
              animate={{ scale: 1, rotate: [0, 10, -10, 0] }}
              transition={{ type: 'spring', damping: 10 }}
            >
              👑
            </motion.div>

            <motion.div
              initial={{ scale: 0.5, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.2, type: 'spring' }}
            >
              <div style={{
                fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '3rem', lineHeight: 1,
                background: 'linear-gradient(135deg, var(--nova-gold), var(--stellar-pink))',
                WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
              }}>
                LEVEL UP!
              </div>
              <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.3rem', color: 'var(--stellar-pink)', marginTop: 8 }}>
                NOVA CHAMPION
              </div>
              <div className="chip chip-gold" style={{ margin: '12px auto 0', display: 'inline-flex' }}>Level 15</div>
            </motion.div>

            <motion.div
              className="glass-card"
              style={{ padding: '16px 20px', width: '100%', maxWidth: 300 }}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
            >
              <div style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', marginBottom: 8 }}>
                You reached Level 15! 🎉
              </div>
              <div style={{ color: 'var(--bio-green)', fontFamily: 'var(--font-label)', fontWeight: 700, fontSize: '0.82rem' }}>
                🔓 UNLOCKED: Future Doctor Badge 🩺
              </div>
            </motion.div>

            <motion.div
              style={{ fontStyle: 'italic', color: 'var(--text-primary)', fontSize: '0.9rem', maxWidth: 280, lineHeight: 1.5 }}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.6 }}
            >
              "⭐ Suhana, Level 15. Not everyone makes it here. But then again, not everyone is you."
            </motion.div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 10, width: '100%', maxWidth: 300 }}>
              <button className="btn-primary" onClick={() => setShowLevelUp(false)}>Claim Reward ✨</button>
              <button className="btn-ghost" onClick={() => setShowLevelUp(false)}>Continue Studying</button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
