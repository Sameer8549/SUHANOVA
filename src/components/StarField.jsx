// Suhanova — Particle Star Background
import { useEffect, useRef } from 'react';

export default function StarField({ count = 60 }) {
  const containerRef = useRef(null);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;
    container.innerHTML = '';

    for (let i = 0; i < count; i++) {
      const star = document.createElement('div');
      star.className = 'star-particle';
      star.style.cssText = `
        left: ${Math.random() * 100}%;
        top: ${Math.random() * 100}%;
        --duration: ${2 + Math.random() * 4}s;
        --delay: ${Math.random() * 4}s;
        opacity: ${0.2 + Math.random() * 0.8};
        width: ${1 + Math.random() * 3}px;
        height: ${1 + Math.random() * 3}px;
        background: ${Math.random() > 0.7 ? '#FF6EB4' : '#FFD700'};
      `;
      container.appendChild(star);
    }
  }, [count]);

  return (
    <div
      ref={containerRef}
      style={{
        position: 'fixed', inset: 0, zIndex: 0,
        pointerEvents: 'none', overflow: 'hidden'
      }}
    />
  );
}
