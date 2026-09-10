import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import { api } from '../api'
import { iscriviti } from '../ws/stompClient'
import { useAuth } from './useAuth'

export const NotificheContext = createContext(null)

/**
 * Tiene il contatore della campanella. Il valore di partenza arriva dal database
 * (unread-count): cosi' e' corretto anche dopo un refresh o dopo una finestra chiusa.
 * I frame su /user/queue/notifications lo incrementano mentre la pagina e' aperta.
 */
export function NotificheProvider({ children }) {
  const { token } = useAuth()
  const [nonLette, setNonLette] = useState(0)
  const [elenco, setElenco] = useState([])

  const ricarica = useCallback(async () => {
    if (!token) return
    const [conteggio, pagina] = await Promise.all([api.nonLette(token), api.notifiche(token)])
    setNonLette(conteggio.count)
    setElenco(pagina.content)
  }, [token])

  useEffect(() => {
    if (!token) {
      setNonLette(0)
      setElenco([])
      return
    }
    ricarica()
  }, [token, ricarica])

  useEffect(() => {
    if (!token) return
    // Destinazione personale: il backend la risolve dallo username legato alla sessione WebSocket.
    return iscriviti('/user/queue/notifications', (notifica) => {
      setNonLette((n) => n + 1)
      // notifica.id e' l'id della riga notifications di questo destinatario:
      // vale come chiave React e come id da mandare a PATCH .../read.
      setElenco((precedenti) => [{ ...notifica, readAt: null }, ...precedenti])
    })
  }, [token])

  const leggiTutte = useCallback(async () => {
    await api.leggiTutte(token)
    setNonLette(0)
    setElenco((precedenti) => precedenti.map((n) => ({ ...n, readAt: new Date().toISOString() })))
  }, [token])

  /**
   * Segna letta la singola notifica aperta. Lo stato locale si aggiorna subito:
   * la navigazione al topic parte nello stesso clic e non deve aspettare la rete.
   */
  const leggi = useCallback(
    async (id) => {
      if (!token) return
      // Gia' letta: niente chiamata e soprattutto niente decremento del badge.
      const bersaglio = elenco.find((n) => n.id === id)
      if (!bersaglio || bersaglio.readAt) return

      setElenco((precedenti) =>
        precedenti.map((n) => (n.id === id ? { ...n, readAt: new Date().toISOString() } : n)),
      )
      setNonLette((n) => Math.max(0, n - 1))

      try {
        await api.leggi(id, token)
      } catch {
        // La scrittura non e' passata: il conteggio vero torna dal server.
        ricarica()
      }
    },
    [token, elenco, ricarica],
  )

  const valore = useMemo(
    () => ({ nonLette, elenco, ricarica, leggiTutte, leggi }),
    [nonLette, elenco, ricarica, leggiTutte, leggi],
  )

  return <NotificheContext.Provider value={valore}>{children}</NotificheContext.Provider>
}
