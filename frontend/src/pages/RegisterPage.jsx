import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

export function RegisterPage() {
  const { token, registrati } = useAuth()
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
      // Username gia' preso: il backend risponde 409 e il messaggio finisce qui.
      await registrati(username, password)
      navigate('/topics')
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInCorso(false)
    }
  }

  return (
    <div className="scheda-centrata">
      <h1>Registrati</h1>
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
          {inCorso ? 'Attendi…' : 'Crea account'}
        </button>
      </form>
      <p>
        Hai gia' un account? <Link to="/login">Accedi</Link>
      </p>
    </div>
  )
}
