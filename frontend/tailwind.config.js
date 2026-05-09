/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#df6020',
        'primary-dark': '#c65319',
        dark: '#0f172a',
        secondary: '#64748b',
        muted: '#94a3b8',
        background: '#f8f6f6',
        surface: '#ffffff',
        border: '#e2e8f0',
        error: '#ef4444',
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      boxShadow: {
        'scholarly': '0 4px 20px rgba(45, 41, 38, 0.04)',
        'scholarly-hover': '0 12px 32px rgba(45, 41, 38, 0.08)',
      },
      keyframes: {
        shake: {
          '0%, 100%': { transform: 'translateX(0)' },
          '25%': { transform: 'translateX(-4px)' },
          '75%': { transform: 'translateX(4px)' },
        },
        'flip-front': {
          '0%': { transform: 'rotateY(0deg)' },
          '100%': { transform: 'rotateY(180deg)' },
        },
        'flip-back': {
          '0%': { transform: 'rotateY(-180deg)' },
          '100%': { transform: 'rotateY(0deg)' },
        }
      },
      animation: {
        shake: 'shake 0.4s ease-in-out',
        'flip-front': 'flip-front 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards',
        'flip-back': 'flip-back 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards',
      }
    },
  },
  plugins: [],
}
