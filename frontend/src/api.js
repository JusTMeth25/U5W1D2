const BASE = import.meta.env.VITE_API_URL

/**
 * Unico punto di uscita verso il backend. Il token viene passato dal chiamante:
 * cosi' l'unica copia vive nel contesto di autenticazione.
 */
async function richiesta(path, { method = 'GET', token, body } = {}) {
  const res = await fetch(BASE + path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  })

  const testo = await res.text()
  const dati = testo ? JSON.parse(testo) : null

  if (!res.ok) {
    // Il GlobalExceptionHandler risponde sempre con { status, error, message }.
    const errore = new Error(dati?.message ?? `Errore ${res.status}`)
    errore.status = res.status
    throw errore
  }
  return dati
}

export const api = {
  register: (username, password) =>
    richiesta('/api/auth/register', { method: 'POST', body: { username, password } }),

  login: (username, password) =>
    richiesta('/api/auth/login', { method: 'POST', body: { username, password } }),

  topics: (token) => richiesta('/api/topics', { token }),

  iscriviti: (nome, token) =>
    richiesta(`/api/topics/${nome}/subscription`, { method: 'POST', token }),

  disiscriviti: (nome, token) =>
    richiesta(`/api/topics/${nome}/subscription`, { method: 'DELETE', token }),

  storico: (nome, token, page = 0, size = 50) =>
    richiesta(`/api/topics/${nome}/messages?page=${page}&size=${size}`, { token }),

  pubblica: (nome, testo, token) =>
    richiesta(`/api/topics/${nome}/messages`, { method: 'POST', token, body: { text: testo } }),

  notifiche: (token, page = 0, size = 20) =>
    richiesta(`/api/notifications?page=${page}&size=${size}`, { token }),

  nonLette: (token) => richiesta('/api/notifications/unread-count', { token }),

  leggiTutte: (token) => richiesta('/api/notifications/read-all', { method: 'POST', token }),

  leggi: (id, token) => richiesta(`/api/notifications/${id}/read`, { method: 'PATCH', token }),
}
