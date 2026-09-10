import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // sockjs-client cerca la variabile `global` di Node: nel browser non esiste.
  define: { global: 'globalThis' },
  // Il backend accetta una sola origine (app.cors.allowed-origin): questa.
  server: { port: 5173, strictPort: true },
})
