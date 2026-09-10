// Verifica la stessa catena del browser: SockJS + @stomp/stompjs contro il backend.
// Uso: node smoke-sockjs.mjs   (backend su :3001 e Postgres avviati)
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const BASE = 'http://localhost:3001'
const TOPIC = 'java'
const suffisso = Date.now().toString().slice(-6)
const alice = { username: 'alice' + suffisso, password: 'password1' }
const bob = { username: 'bob' + suffisso, password: 'password1' }

async function api(path, { method = 'GET', token, body } = {}) {
  const res = await fetch(BASE + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  })
  const testo = await res.text()
  if (!res.ok) throw new Error(`${method} ${path} -> ${res.status} ${testo}`)
  return testo ? JSON.parse(testo) : null
}

const attendi = (ms) => new Promise((r) => setTimeout(r, ms))

const a = await api('/api/auth/register', { method: 'POST', body: alice })
const b = await api('/api/auth/register', { method: 'POST', body: bob })
await api(`/api/topics/${TOPIC}/subscription`, { method: 'POST', token: a.token })
await api(`/api/topics/${TOPIC}/subscription`, { method: 'POST', token: b.token })

const ricevuti = []
const client = new Client({
  webSocketFactory: () => new SockJS(`${BASE}/ws`),
  connectHeaders: { Authorization: `Bearer ${b.token}` },
  reconnectDelay: 5000,
})

await new Promise((resolve, reject) => {
  client.onConnect = () => {
    client.subscribe(`/topic/feed/${TOPIC}`, (f) => ricevuti.push(['feed', JSON.parse(f.body)]))
    client.subscribe('/user/queue/notifications', (f) => ricevuti.push(['notifica', JSON.parse(f.body)]))
    resolve()
  }
  client.onStompError = (f) => reject(new Error(f.headers.message))
  client.activate()
})

await attendi(400)
await api(`/api/topics/${TOPIC}/messages`, { method: 'POST', token: a.token, body: { text: 'prova ' + suffisso } })
await attendi(1200)
await client.deactivate()

ricevuti.forEach(([tipo, dati]) => console.log(tipo, '=>', JSON.stringify(dati)))
const conteggio = await api('/api/notifications/unread-count', { token: b.token })
const ok = ricevuti.some(([t]) => t === 'feed') && ricevuti.some(([t]) => t === 'notifica') && conteggio.count >= 1
console.log('unread-count:', conteggio.count, '| ESITO:', ok ? 'OK' : 'FALLITO')
process.exit(ok ? 0 : 1)
