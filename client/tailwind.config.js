/** @type {import('tailwindcss').Config} */
import PrimeUI from 'tailwindcss-primeui';

export default {
  darkMode: ['selector', '[class*="app-dark"]'],
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  plugins: [PrimeUI],
  theme: {
    screens: {
      xs: '375px',
      sm: '576px',
      md: '768px',
      lg: '992px',
      xl: '1200px',
      '2xl': '1920px',
    },
    extend: {
      fontSize: {
        xxxxs: '0.325rem',
        xxxs: '0.5rem',
        xxs: '0.625rem',
        xs: '0.75rem',
      },
    }
  }
};
