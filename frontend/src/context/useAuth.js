import { useContext } from 'react'
import { AuthContext } from './AuthContext'

export function useAuth() {
  const valore = useContext(AuthContext)
  if (!valore) throw new Error('useAuth va usato dentro AuthProvider')
  return valore
}
