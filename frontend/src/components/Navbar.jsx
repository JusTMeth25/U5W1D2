import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { useNotifiche } from '../context/useNotifiche'

function quando(iso) {
  return new Date(iso).toLocaleString('it-IT', { dateStyle: 'short', timeStyle: 'short' })
}

export function Navbar() {
  const { username, esci } = useAuth()
  const { nonLette, elenco, leggiTutte, leggi } = useNotifiche()
  const [aperto, setAperto] = useState(false)
  const navigate = useNavigate()

  async function logout() {
    await esci()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <Link to="/topics" className="marchio">
        Bacheche
      </Link>

      <div className="navbar-destra">
        <div className="campanella-zona">
          <button className="campanella" onClick={() => setAperto((v) => !v)}>
            🔔
            {nonLette > 0 && <span className="badge">{nonLette}</span>}
          </button>

          {aperto && (
            <div className="pannello">
              <div className="pannello-testata">
                <strong>Notifiche</strong>
                <button onClick={leggiTutte} disabled={nonLette === 0}>
                  Segna tutte lette
                </button>
              </div>

              {elenco.length === 0 && <p className="vuoto">Nessuna notifica.</p>}

              <ul>
                {elenco.map((n) => (
                  <li key={n.id} className={n.readAt ? 'letta' : 'non-letta'}>
                    <Link
                      to={`/topics/${n.topic}`}
                      onClick={() => {
                        leggi(n.id)
                        setAperto(false)
                      }}
                    >
                      <span className="etichetta">{n.topic}</span> {n.author}: {n.preview}
                      <small>{quando(n.createdAt)}</small>
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <span className="utente">{username}</span>
        <button onClick={logout}>Esci</button>
      </div>
    </header>
  )
}
