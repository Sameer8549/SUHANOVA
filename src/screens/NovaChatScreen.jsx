// Suhanova — Nova Chat AI Tutor Screen
import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const INITIAL_MESSAGES = [
  {
    id: 1, role: 'nova',
    text: "Hey Suhana! 👋 I'm Nova — your personal AI tutor. Ask me anything about Biology, Physics, Chemistry, or Maths. I'm here for you, always. 🌟\n\nWhat's on your mind today?",
    timestamp: new Date(Date.now() - 300000),
  },
];

const CANNED_RESPONSES = {
  default: [
    "Great question, Suhana! Let me break this down for you in the simplest way possible. 💡",
    "Of course! This is actually one of my favorite topics to explain. Here's how I think about it...",
    "Ah, this comes up a lot in NEET! Let me give you the full picture so you never forget it. 🧠",
  ],
  bio: "🌱 Osmosis is the movement of water molecules through a semi-permeable membrane from a region of higher water concentration (dilute/hypotonic) to lower water concentration (concentrated/hypertonic).\n\nMemory trick: **Water chases salt** 🌊\n\nFor NEET: If a cell is placed in hypertonic solution → water leaves the cell → cell shrinks (PLASMOLYSIS in plants).\nIf placed in hypotonic solution → water enters the cell → cell swells (ENDOSMOSIS).",
  phys: "⚡ Newton's 3rd Law: For every action, there is an equal and opposite reaction.\n\nReal example: When you push against a wall, the wall pushes back with equal force. When you fire a gun, the bullet goes forward but the gun recoils backward.\n\nFor NEET: Remember this applies to FORCES, not objects. The forces are equal in magnitude but opposite in direction and act on DIFFERENT objects.",
  chem: "🧪 Le Chatelier's Principle: If a system at equilibrium is disturbed, it adjusts to minimize the disturbance and re-establish equilibrium.\n\nSimple memory: Think of equilibrium as a stubborn system — whenever you disturb it, it fights back!\n\nFor NEET: Increasing temperature favors endothermic reaction. Increasing pressure favors reaction with fewer moles of gas.",
};

function formatTime(date) {
  return date.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
}

function detectTopic(text) {
  const lower = text.toLowerCase();
  if (lower.includes('osmosis') || lower.includes('cell') || lower.includes('bio') || lower.includes('photosyn')) return 'bio';
  if (lower.includes('newton') || lower.includes('force') || lower.includes('energy') || lower.includes('physics')) return 'phys';
  if (lower.includes('chem') || lower.includes('acid') || lower.includes('reaction') || lower.includes('equilibrium')) return 'chem';
  return 'default';
}

export default function NovaChat() {
  const [messages, setMessages] = useState(INITIAL_MESSAGES);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const endRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  const sendMessage = async () => {
    const text = input.trim();
    if (!text) return;

    const userMsg = { id: Date.now(), role: 'user', text, timestamp: new Date() };
    setMessages(m => [...m, userMsg]);
    setInput('');
    setIsTyping(true);

    await new Promise(r => setTimeout(r, 1200 + Math.random() * 800));

    const topic = detectTopic(text);
    let response;
    if (topic !== 'default') {
      response = CANNED_RESPONSES[topic];
    } else {
      response = `${CANNED_RESPONSES.default[Math.floor(Math.random() * 3)]}\n\nFor NEET specifically, the key things to remember are:\n1. The fundamental principle behind this concept\n2. How it's tested in MCQ format\n3. Common traps to avoid\n\nWant me to create a flashcard for this? Just say "make flashcard" and I'll add it to your Biology deck! 🃏`;
    }

    setIsTyping(false);
    setMessages(m => [...m, { id: Date.now() + 1, role: 'nova', text: response, timestamp: new Date() }]);
  };

  const QUICK_ASKS = [
    "Explain osmosis 💧",
    "Newton's 3rd Law ⚡",
    "Le Chatelier's Principle 🧪",
    "What is mitosis? 🧬",
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100dvh', position: 'relative' }}>
      {/* Header */}
      <div style={{
        padding: '20px 20px 12px',
        background: 'rgba(10,10,15,0.9)',
        backdropFilter: 'blur(20px)',
        borderBottom: '1px solid var(--glass-border)',
        flexShrink: 0,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{
            width: 46, height: 46, borderRadius: '50%',
            background: 'linear-gradient(135deg, var(--nova-gold), var(--stellar-pink))',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '1.4rem', boxShadow: '0 0 20px var(--nova-gold-glow)',
          }}>✨</div>
          <div>
            <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1rem', color: 'var(--nova-gold)' }}>Nova</div>
            <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--bio-green)', display: 'flex', alignItems: 'center', gap: 4 }}>
              <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--bio-green)', display: 'inline-block' }} />
              Always here for you, Suhana
            </div>
          </div>
          <div style={{ marginLeft: 'auto' }}>
            <span className="chip chip-gold" style={{ fontSize: '0.65rem' }}>Groq · Instant ⚡</span>
          </div>
        </div>
      </div>

      {/* Messages */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '16px 20px', display: 'flex', flexDirection: 'column', gap: 14, paddingBottom: 140 }}>
        <AnimatePresence>
          {messages.map(msg => (
            <motion.div
              key={msg.id}
              initial={{ opacity: 0, y: 12, scale: 0.96 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{ type: 'spring', damping: 20 }}
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: msg.role === 'user' ? 'flex-end' : 'flex-start',
                gap: 4,
              }}
            >
              {msg.role === 'nova' && (
                <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.65rem', color: 'var(--text-muted)', paddingLeft: 4, display: 'flex', alignItems: 'center', gap: 4 }}>
                  ✨ Nova · {formatTime(msg.timestamp)}
                </div>
              )}
              <div className={`chat-bubble ${msg.role === 'user' ? 'chat-bubble-user' : 'chat-bubble-nova'}`}>
                {msg.role === 'nova' && (
                  <div className="nova-tag">🌟 Nova says:</div>
                )}
                <div style={{ whiteSpace: 'pre-line', fontSize: '0.88rem', lineHeight: 1.55 }}>
                  {msg.text}
                </div>
              </div>
              {msg.role === 'user' && (
                <div style={{ fontFamily: 'var(--font-label)', fontSize: '0.63rem', color: 'var(--text-muted)', paddingRight: 4 }}>
                  Suhana · {formatTime(msg.timestamp)}
                </div>
              )}
            </motion.div>
          ))}

          {isTyping && (
            <motion.div
              key="typing"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
              className="chat-bubble chat-bubble-nova"
              style={{ alignSelf: 'flex-start', display: 'flex', gap: 5, alignItems: 'center', padding: '14px 18px' }}
            >
              {[0, 0.15, 0.3].map((delay, i) => (
                <motion.div
                  key={i}
                  style={{ width: 7, height: 7, borderRadius: '50%', background: 'var(--nova-gold)' }}
                  animate={{ y: [0, -6, 0] }}
                  transition={{ duration: 0.7, delay, repeat: Infinity }}
                />
              ))}
            </motion.div>
          )}
        </AnimatePresence>
        <div ref={endRef} />
      </div>

      {/* Quick Asks */}
      <div style={{
        position: 'fixed', bottom: 80, left: 0, right: 0,
        padding: '8px 20px',
        overflowX: 'auto', display: 'flex', gap: 8,
        scrollbarWidth: 'none',
        background: 'linear-gradient(to right, var(--bg-space) 0%, transparent 5%, transparent 95%, var(--bg-space) 100%)',
      }}>
        {QUICK_ASKS.map(q => (
          <button
            key={q}
            className="chip chip-gold"
            style={{ cursor: 'pointer', whiteSpace: 'nowrap', fontSize: '0.72rem' }}
            onClick={() => { setInput(q); inputRef.current?.focus(); }}
          >
            {q}
          </button>
        ))}
      </div>

      {/* Input Bar */}
      <div className="chat-input-bar">
        <button className="btn-ghost" style={{ padding: '8px', fontSize: '1.1rem' }}>📷</button>
        <input
          ref={inputRef}
          className="nova-input"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && sendMessage()}
          placeholder="Ask Nova anything... 💭"
          style={{ flex: 1, padding: '12px 16px' }}
        />
        <button className="btn-ghost" style={{ padding: '8px', fontSize: '1.1rem' }}>🎙️</button>
        <motion.button
          className="btn-primary"
          style={{ padding: '10px 16px', fontSize: '1rem', borderRadius: 12 }}
          onClick={sendMessage}
          whileTap={{ scale: 0.93 }}
          disabled={!input.trim()}
        >
          ›
        </motion.button>
      </div>
    </div>
  );
}
