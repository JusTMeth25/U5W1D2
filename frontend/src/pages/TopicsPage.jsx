import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../context/useAuth'

export function TopicsPage() {
  const { token } = useAuth()
  const [bacheche, setBacheche] = useState([])
  const [errore, setErrore] = useState(null)
  const [inCorso, setInCorso] = useState(null)

  const carica = useCallback(async () => {
    try {
      setBacheche(await api.topics(token))
    } catch (err) {
      setErrore(err.message)
    }
  }, [token])

  useEffect(() => {
    carica()
  }, [carica])

  async function cambiaIscrizione(bacheca) {
    setErrore(null)
    setInCorso(bacheca.name)
    try {
      if (bacheca.iscritto) await api.disiscriviti(bacheca.name, token)
      else await api.iscriviti(bacheca.name, token)
      // Il conteggio iscritti lo calcola il backend: ricarico invece di indovinarlo.
      await carica()
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInCorso(null)
    }
  }

  return (
    <div>
      <h1>Bacheche</h1>
      {errore && <p className="errore">{errore}</p>}

      <ul className="elenco-bacheche">
        {bacheche.map((b) => (
          <li key={b.name}>
            <div>
              <Link to={`/topics/${b.name}`} className="titolo">
                {b.title}
              </Link>
              <small>{b.iscritti} iscritti</small>
            </div>
            <button onClick={() => cambiaIscrizione(b)} disabled={inCorso === b.name}>
              {b.iscritto ? 'Disiscriviti' : 'Iscriviti'}
            </button>
          </li>
        ))}
      </ul>
    </div>
  )
}
