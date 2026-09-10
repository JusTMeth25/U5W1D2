import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../context/useAuth'
import { iscriviti } from '../ws/stompClient'

function quando(iso) {
  return new Date(iso).toLocaleString('it-IT', { dateStyle: 'short', timeStyle: 'short' })
}

export function TopicPage() {
  const { nome } = useParams()
  const { token, username } = useAuth()
  const [messaggi, setMessaggi] = useState([])
  const [bacheca, setBacheca] = useState(null)
  const [testo, setTesto] = useState('')
  const [errore, setErrore] = useState(null)
  const [inCorso, setInCorso] = useState(false)

  // Storico dal REST: il canale porta solo cio' che nasce da adesso in poi.
  useEffect(() => {
    let annullato = false
    setErrore(null)
    Promise.all([api.storico(nome, token), api.topics(token)])
      .then(([pagina, bacheche]) => {
        if (annullato) return
        setMessaggi([...pagina.content].reverse()) // il backend ordina dal piu' recente
        setBacheca(bacheche.find((b) => b.name === nome) ?? null)
      })
      .catch((err) => !annullato && setErrore(err.message))
    return () => {
      annullato = true
    }
  }, [nome, token])

  // Feed della bacheca: la pulizia chiude la sottoscrizione quando si cambia pagina.
  useEffect(() => {
    return iscriviti(`/topic/feed/${nome}`, (messaggio) => {
      setMessaggi((precedenti) =>
        precedenti.some((m) => m.id === messaggio.id) ? precedenti : [...precedenti, messaggio],
      )
    })
  }, [nome])

  async function invia(e) {
    e.preventDefault()
    setErrore(null)
    setInCorso(true)
    try {
      // Il messaggio non viene aggiunto qui: arriva dal canale come per tutti gli altri.
      await api.pubblica(nome, testo, token)
      setTesto('')
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInCorso(false)
    }
  }

  const iscritto = bacheca?.iscritto ?? false

  return (
    <div>
      <p>
        <Link to="/topics">← Tutte le bacheche</Link>
      </p>
      <h1>{bacheca?.title ?? nome}</h1>

      <ul className="messaggi">
        {messaggi.map((m) => (
          <li key={m.id} className={m.author === username ? 'mio' : ''}>
            <strong>{m.author}</strong>
            <span>{m.text}</span>
            <small>{quando(m.createdAt)}</small>
          </li>
        ))}
      </ul>

      <form onSubmit={invia} className="scrittura">
        <input
          value={testo}
          onChange={(e) => setTesto(e.target.value)}
          placeholder={iscritto ? 'Scrivi un messaggio…' : 'Iscriviti alla bacheca per scrivere'}
          disabled={!iscritto || inCorso}
          required
        />
        <button type="submit" disabled={!iscritto || inCorso}>
          Invia
        </button>
      </form>

      {errore && <p className="errore">{errore}</p>}
    </div>
  )
}
