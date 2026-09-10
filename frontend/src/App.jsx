import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { RottaProtetta } from './components/RottaProtetta'
import { AuthProvider } from './context/AuthContext'
import { NotificheProvider } from './context/NotificheContext'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { TopicPage } from './pages/TopicPage'
import { TopicsPage } from './pages/TopicsPage'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <NotificheProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route element={<RottaProtetta />}>
              <Route path="/topics" element={<TopicsPage />} />
              <Route path="/topics/:nome" element={<TopicPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/topics" replace />} />
          </Routes>
        </NotificheProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
