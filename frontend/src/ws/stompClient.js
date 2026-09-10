import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const BASE = import.meta.env.VITE_API_URL

/**
 * Un solo Client per tutta l'applicazione: un Client e' una connessione.
 * Crearne uno per componente aprirebbe tante connessioni quanti sono i componenti.
 */
export const client = new Client({
  webSocketFactory: () => new SockJS(`${BASE}/ws`),
  reconnectDelay: 5000,
})

/**
 * Registro delle sottoscrizioni desiderate. Serve perche' un componente puo'
 * chiedere una destinazione mentre il client e' ancora in connessione, e perche'
 * dopo una riconnessione le sottoscrizioni vanno riaperte: il broker le ha perse.
 */
const richieste = new Map()
let contatore = 0

function apri(richiesta) {
  richiesta.sub = client.subscribe(richiesta.destinazione, (frame) => {
    // frame.body e' una stringa: il JSON.parse tocca a noi.
    richiesta.onMessaggio(JSON.parse(frame.body))
  })
}

// Le subscribe vanno qui, non dopo activate(): al momento di activate() il
// canale non e' ancora aperto.
client.onConnect = () => {
  for (const richiesta of richieste.values()) apri(richiesta)
}

client.onStompError = (frame) => {
  console.error('Errore STOMP:', frame.headers.message, frame.body)
}

/**
 * Chiede una destinazione e ritorna la funzione di pulizia da usare nel useEffect.
 */
export function iscriviti(destinazione, onMessaggio) {
  const id = ++contatore
  const richiesta = { destinazione, onMessaggio, sub: null }
  richieste.set(id, richiesta)
  if (client.connected) apri(richiesta)

  return () => {
    richieste.delete(id)
    richiesta.sub?.unsubscribe()
  }
}

/** Si attiva dopo il login, quando il token esiste. */
export function attiva(token) {
  if (client.active) return
  client.connectHeaders = { Authorization: `Bearer ${token}` }
  client.activate()
}

/** Al logout la connessione va chiusa, altrimenti resta aperta con l'identita' vecchia. */
export function disattiva() {
  for (const richiesta of richieste.values()) richiesta.sub?.unsubscribe()
  richieste.clear()
  return client.deactivate()
}
