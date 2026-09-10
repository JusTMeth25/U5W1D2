import { useContext } from 'react'
import { NotificheContext } from './NotificheContext'

export function useNotifiche() {
  const valore = useContext(NotificheContext)
  if (!valore) throw new Error('useNotifiche va usato dentro NotificheProvider')
  return valore
}
