// Suhanova — The Nova Moment Hero Card
// "Made specially for Suhana. My Doctor. 🩺"
import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';

const MESSAGES = {
  morning: [
    "Good morning, Dr. Suhana ☀️ The wards are waiting.",
    "Rise, Suhana. Champions don't sleep through their destiny ✨",
    "Morning, future Doctor 🌅 Today's diagrams are tomorrow's lives saved.",
  ],
  afternoon: [
    "Still going, future Doctor? That's your answer. 💪",
    "Afternoon, Suhana. Half the day down, half to go. You've got this 🔥",
    "Look at you, studying in the afternoon like the champion you are 🌟",
  ],
  evening: [
    "Evening shift, Dr. Suhana? 🌆 Keep going — you're almost there.",
    "The stars are coming out, Suhana. So is your brilliance ⭐",
    "Even the evening knows — Suhana never stops. 🌙",
  ],
  night: [
    "Look how far you've come today, Suhana 🌙",
    "Night shift mode: ON. Dr. Suhana is studying. 🔬",
    "The night belongs to the dedicated. That's you, Suhana ✨",
  ],
  midnight: [
    "Midnight again, future Doctor? Your patients will be lucky to have someone who cares this much. Rest now. 🌟",
    "12AM and still going? That's not just dedication — that's love for medicine. 🩺 Rest when you need to.",
    "The stars are watching, Suhana. So proud of you. But doctors need sleep too 💤 Rest well.",
  ],
};

const AI_MESSAGES = [
  "Suhana, every diagram you study today is a patient you'll save tomorrow. That's not motivation — that's a fact.",
  "You chose medicine because you care. Every question you answer proves it, Suhana.",
  "The difference between a good doctor and a great one? The hours between midnight and dawn. You know this already.",
  "NEET is a door. You have the key. You've had it all along, Suhana.",
  "Today's hard topic is tomorrow's easy question in the exam hall. Keep going.",
];

function getTimeGreeting() {
  const hour = new Date().getHours();
  if (hour >= 0 && hour < 4)   return 'midnight';
  if (hour >= 4 && hour < 12)  return 'morning';
  if (hour >= 12 && hour < 17) return 'afternoon';
  if (hour >= 17 && hour < 21) return 'evening';
  return 'night';
}

function getNEETDays() {
  const neet = new Date('2025-07-17');
  const today = new Date();
  const diff = Math.ceil((neet - today) / (1000 * 60 * 60 * 24));
  return Math.max(0, diff);
}

export default function NovaMomentCard({ streak = 12 }) {
  const [period, setPeriod] = useState(getTimeGreeting());
  const [greeting] = useState(() => {
    const greets = MESSAGES[getTimeGreeting()];
    return greets[Math.floor(Math.random() * greets.length)];
  });
  const [aiMessage] = useState(() =>
    AI_MESSAGES[Math.floor(Math.random() * AI_MESSAGES.length)]
  );

  useEffect(() => {
    const interval = setInterval(() => setPeriod(getTimeGreeting()), 60000);
    return () => clearInterval(interval);
  }, []);

  return (
    <motion.div
      className="nova-border"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, type: 'spring' }}
      style={{ padding: 2 }}
    >
      <div className="nova-moment-card pulse-glow">
        {/* Background radial glow */}
        <div style={{
          position: 'absolute', inset: 0, pointerEvents: 'none',
          background: 'radial-gradient(ellipse at 50% -20%, rgba(255,215,0,0.1) 0%, transparent 60%)',
          borderRadius: 'inherit',
        }} />

        {/* Floating stethoscope */}
        <motion.div
          className="float-anim"
          style={{ position: 'absolute', top: 16, right: 20, fontSize: '2rem', opacity: 0.3 }}
        >
          🩺
        </motion.div>

        {/* Label */}
        <div className="nova-label">
          <span>✦</span> YOUR NOVA MOMENT
        </div>

        {/* Greeting */}
        <motion.div
          className="greeting"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
        >
          {greeting}
        </motion.div>

        {/* AI Message */}
        <div className="message">
          "{aiMessage}"
        </div>

        {/* Stats Row */}
        <div className="stats-row">
          <span className="chip chip-gold">🩺 {getNEETDays()} days to NEET</span>
          <span className="chip chip-pink">🔥 {streak} Day Streak</span>
        </div>

        {/* Permanent Tagline — Never Changes */}
        <div className="permanent-tagline">
          Made specially for Suhana. My Doctor. 🩺
        </div>
      </div>
    </motion.div>
  );
}
