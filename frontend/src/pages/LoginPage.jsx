import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

export function LoginPage() {
  const { token, entra } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errore, setErrore] = useState(null)
  const [inCorso, setInCorso] = useState(false)
  const navigate = useNavigate()

  if (token) return <Navigate to="/topics" replace />

  async function invia(e) {
    e.preventDefault()
    setErrore(null)
    setInCorso(true)
    try {
      // entra() salva il token: l'attivazione del client STOMP parte da li'.
      await entra(username, password)
      navigate('/topics')
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInCorso(false)
    }
  }

  return (
    <div className="scheda-centrata">
      <h1>Accedi</h1>
      <form onSubmit={invia}>
        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} required minLength={3} />
        </label>
        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={6}
          />
        </label>
        {errore && <p className="errore">{errore}</p>}
        <button type="submit" disabled={inCorso}>
          {inCorso ? 'Attendi…' : 'Entra'}
        </button>
      </form>
      <p>
        Non hai un account? <Link to="/register">Registrati</Link>
      </p>
    </div>
  )
}
