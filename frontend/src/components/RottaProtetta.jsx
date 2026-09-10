import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { Navbar } from './Navbar'

/** Senza token non c'e' niente da mostrare: ogni chiamata REST risponderebbe 401. */
export function RottaProtetta() {
  const { token } = useAuth()
  if (!token) return <Navigate to="/login" replace />

  return (
    <>
      <Navbar />
      <main className="contenuto">
        <Outlet />
      </main>
    </>
  )
}
