// Suhanova — Bottom Navigation Bar
import { motion } from 'framer-motion';

const TABS = [
  { id: 'home',     emoji: '🏠', label: 'Home'     },
  { id: 'study',    emoji: '📚', label: 'Study'    },
  { id: 'quiz',     emoji: '✨', label: 'Quiz'     },
  { id: 'progress', emoji: '📈', label: 'Progress' },
  { id: 'rewards',  emoji: '🏆', label: 'Rewards'  },
];

export default function NavBar({ active, onChange }) {
  return (
    <nav className="nav-bar">
      <svg width="0" height="0" style={{ position: 'absolute' }}>
        <defs>
          <linearGradient id="goldGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%"   stopColor="#FFD700" />
            <stop offset="100%" stopColor="#FF6EB4" />
          </linearGradient>
        </defs>
      </svg>

      {TABS.map(tab => (
        <button
          key={tab.id}
          className={`nav-item ${active === tab.id ? 'active' : ''}`}
          onClick={() => onChange(tab.id)}
        >
          <motion.span
            className="nav-icon"
            animate={active === tab.id ? { scale: [1, 1.25, 1] } : { scale: 1 }}
            transition={{ duration: 0.3, type: 'spring' }}
          >
            {tab.emoji}
          </motion.span>
          <span className="nav-label">{tab.label}</span>
          {active === tab.id && (
            <motion.div
              layoutId="nav-indicator"
              style={{
                position: 'absolute',
                bottom: -12,
                width: 4,
                height: 4,
                borderRadius: '50%',
                background: 'var(--nova-gold)',
                boxShadow: '0 0 8px var(--nova-gold)',
              }}
            />
          )}
        </button>
      ))}
    </nav>
  );
}
