/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        'dojo-base': '#06090f',
        surface: {
          1: '#0d1117',
          2: '#161b22',
        },
        border: {
          DEFAULT: 'rgba(255, 255, 255, 0.06)',
          active: 'rgba(245, 158, 11, 0.3)',
        },
        primary: {
          400: '#fbbf24',
          500: '#f59e0b',
          600: '#d97706',
        },
        accent: {
          400: '#fbbf24',
          500: '#f59e0b',
          600: '#d97706',
        },
        success: {
          400: '#34d399',
          500: '#10b981',
        },
        danger: {
          400: '#fb7185',
          500: '#f43f5e',
        },
        info: {
          400: '#38bdf8',
          500: '#0ea5e9',
        },
        content: {
          primary: '#f0f6fc',
          secondary: '#8b949e',
          muted: '#484f58',
        },
      },
      fontFamily: {
        outfit: ['Outfit', 'sans-serif'],
        inter: ['Inter', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      fontSize: {
        xs: '12px',
        sm: '14px',
        base: '16px',
        lg: '20px',
        xl: '24px',
        '2xl': '32px',
        '3xl': '40px',
      },
      spacing: {
        1: '4px',
        2: '8px',
        3: '12px',
        4: '16px',
        6: '24px',
        8: '32px',
        12: '48px',
        16: '64px',
        24: '96px',
      },
      borderRadius: {
        sm: '6px',
        DEFAULT: '8px',
        md: '10px',
        lg: '12px',
        xl: '16px',
        '2xl': '20px',
        full: '9999px',
      },
      boxShadow: {
        sm: '0 1px 3px rgba(0, 0, 0, 0.3), 0 1px 2px rgba(0, 0, 0, 0.2)',
        md: '0 4px 12px rgba(0, 0, 0, 0.4), 0 2px 4px rgba(0, 0, 0, 0.3)',
        lg: '0 12px 40px rgba(0, 0, 0, 0.5), 0 4px 12px rgba(0, 0, 0, 0.4)',
        amber: '0 0 20px rgba(245, 158, 11, 0.15), 0 0 6px rgba(245, 158, 11, 0.1)',
      },
      backdropBlur: {
        xs: '4px',
        sm: '8px',
        md: '12px',
        lg: '16px',
        xl: '24px',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(8px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'pulse-amber': {
          '0%, 100%': { boxShadow: '0 0 8px rgba(245, 158, 11, 0.2)' },
          '50%': { boxShadow: '0 0 20px rgba(245, 158, 11, 0.4)' },
        },
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.4s ease-out',
        'pulse-amber': 'pulse-amber 2s ease-in-out infinite',
      },
    },
  },
  plugins: [],
};
