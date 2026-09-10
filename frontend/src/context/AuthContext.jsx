import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import { api } from '../api'
import { attiva, disattiva } from '../ws/stompClient'

export const AuthContext = createContext(null)

const CHIAVE = 'u5w1d2.auth'

function leggiSalvato() {
  const grezzo = localStorage.getItem(CHIAVE)
  return grezzo ? JSON.parse(grezzo) : null
}

export function AuthProvider({ children }) {
  const [sessione, setSessione] = useState(leggiSalvato)

  // Il token opaco vive in memoria del backend: al riavvio decade e le chiamate
  // tornano 401. Qui basta tenerlo in localStorage per sopravvivere al refresh.
  useEffect(() => {
    if (sessione?.token) attiva(sessione.token)
  }, [sessione])

  const entra = useCallback(async (username, password) => {
    const risposta = await api.login(username, password)
    localStorage.setItem(CHIAVE, JSON.stringify(risposta))
    setSessione(risposta)
  }, [])

  const registrati = useCallback(async (username, password) => {
    const risposta = await api.register(username, password)
    localStorage.setItem(CHIAVE, JSON.stringify(risposta))
    setSessione(risposta)
  }, [])

  const esci = useCallback(async () => {
    localStorage.removeItem(CHIAVE)
    setSessione(null)
    await disattiva()
  }, [])

  const valore = useMemo(
    () => ({
      token: sessione?.token ?? null,
      username: sessione?.username ?? null,
      entra,
      registrati,
      esci,
    }),
    [sessione, entra, registrati, esci],
  )

  return <AuthContext.Provider value={valore}>{children}</AuthContext.Provider>
}
