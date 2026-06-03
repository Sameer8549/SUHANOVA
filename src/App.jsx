// Suhanova — Main App Entry Point
// Made specially for Suhana. My Doctor. 🩺
import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import './index.css';

import StarField from './components/StarField';
import NavBar from './components/NavBar';
import HomeScreen from './screens/HomeScreen';
import StudyScreen from './screens/StudyScreen';
import QuizScreen from './screens/QuizScreen';
import ProgressScreen from './screens/ProgressScreen';
import RewardsScreen from './screens/RewardsScreen';
import NovaChatScreen from './screens/NovaChatScreen';

const SCREENS = {
  home:     HomeScreen,
  study:    StudyScreen,
  quiz:     QuizScreen,
  progress: ProgressScreen,
  rewards:  RewardsScreen,
};

const PAGE_VARIANTS = {
  enter: (direction) => ({ opacity: 0, x: direction > 0 ? 40 : -40 }),
  center: { opacity: 1, x: 0 },
  exit: (direction) => ({ opacity: 0, x: direction < 0 ? 40 : -40 }),
};

const TAB_ORDER = ['home', 'study', 'quiz', 'progress', 'rewards'];

export default function App() {
  const [activeTab, setActiveTab] = useState('home');
  const [prevTab, setPrevTab] = useState('home');
  const [showNovaChat, setShowNovaChat] = useState(false);

  const direction = TAB_ORDER.indexOf(activeTab) - TAB_ORDER.indexOf(prevTab);

  const handleTabChange = (tab) => {
    setPrevTab(activeTab);
    setActiveTab(tab);
    setShowNovaChat(false);
  };

  const ActiveScreen = SCREENS[activeTab] || HomeScreen;

  return (
    <div style={{ position: 'relative', minHeight: '100dvh', overflow: 'hidden' }}>
      {/* Background star field */}
      <StarField count={70} />

      {/* Global SVG gradients */}
      <svg width="0" height="0" style={{ position: 'absolute' }}>
        <defs>
          <linearGradient id="goldGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor="#FFD700" />
            <stop offset="100%" stopColor="#FF6EB4" />
          </linearGradient>
        </defs>
      </svg>

      {/* Screen content */}
      <AnimatePresence mode="wait" custom={direction}>
        {showNovaChat ? (
          <motion.div
            key="nova-chat"
            style={{ position: 'relative', zIndex: 10 }}
            initial={{ opacity: 0, x: 40 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -40 }}
            transition={{ duration: 0.28, ease: [0.4, 0, 0.2, 1] }}
          >
            <NovaChatScreen />
            <button
              onClick={() => setShowNovaChat(false)}
              style={{
                position: 'fixed', top: 20, left: 20, zIndex: 200,
                background: 'rgba(255,215,0,0.1)', border: '1px solid rgba(255,215,0,0.3)',
                borderRadius: 10, padding: '8px 14px',
                color: 'var(--nova-gold)', fontFamily: 'var(--font-label)', fontSize: '0.8rem',
                cursor: 'pointer',
              }}
            >
              ← Back
            </button>
          </motion.div>
        ) : (
          <motion.div
            key={activeTab}
            custom={direction}
            variants={PAGE_VARIANTS}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.28, ease: [0.4, 0, 0.2, 1] }}
            style={{ position: 'relative', zIndex: 10 }}
          >
            <ActiveScreen onNavigate={handleTabChange} />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Nova Chat FAB (floating) */}
      {!showNovaChat && (
        <motion.button
          style={{
            position: 'fixed', bottom: 90, right: 20, zIndex: 150,
            width: 52, height: 52, borderRadius: '50%',
            background: 'linear-gradient(135deg, var(--nova-gold), var(--stellar-pink))',
            border: 'none', cursor: 'pointer', fontSize: '1.4rem',
            boxShadow: '0 0 24px rgba(255,215,0,0.5), 0 4px 20px rgba(0,0,0,0.4)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}
          onClick={() => setShowNovaChat(true)}
          whileTap={{ scale: 0.9 }}
          animate={{ scale: [1, 1.05, 1], boxShadow: ['0 0 24px rgba(255,215,0,0.5)', '0 0 40px rgba(255,215,0,0.8)', '0 0 24px rgba(255,215,0,0.5)'] }}
          transition={{ duration: 2.5, repeat: Infinity }}
          title="Ask Nova"
        >
          ✨
        </motion.button>
      )}

      {/* Bottom Nav */}
      {!showNovaChat && (
        <NavBar active={activeTab} onChange={handleTabChange} />
      )}
    </div>
  );
}
