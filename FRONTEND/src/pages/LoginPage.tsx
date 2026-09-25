import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login, register } from '../api/auth'
import { useAuth } from '../auth/AuthContext'
import { apiBaseUrl } from '../api/client'

export function LoginPage() {
  const { setCredentials } = useAuth()
  const navigate = useNavigate()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const response =
        mode === 'login'
          ? await login(username, password)
          : await register(username, email, password)
      await setCredentials(response.token)
      navigate('/')
    } catch (err: any) {
      setError(err.response?.data?.error || err.response?.data?.message || 'Error de autenticación')
    } finally {
      setSubmitting(false)
    }
  }

  const handleMicrosoft = () => {
    window.location.href = `${apiBaseUrl}/oauth2/authorization/azure`
  }

  const handleGoogle = () => {
    window.location.href = `${apiBaseUrl}/oauth2/authorization/google`
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>Pedidos360</h1>
        <p className="muted">Inicia sesión para gestionar tus pedidos</p>

        <div className="tabs">
          <button
            className={mode === 'login' ? 'tab active' : 'tab'}
            onClick={() => setMode('login')}
          >
            Iniciar sesión
          </button>
          <button
            className={mode === 'register' ? 'tab active' : 'tab'}
            onClick={() => setMode('register')}
          >
            Registrarse
          </button>
        </div>

        <form onSubmit={handleSubmit} className="form">
          <label>
            Usuario
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="username"
            />
          </label>
          {mode === 'register' && (
            <label>
              Email
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </label>
          )}
          <label>
            Contraseña
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            />
          </label>

          {error && <div className="alert alert-error">{error}</div>}

          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {mode === 'login' ? 'Entrar' : 'Crear cuenta'}
          </button>
        </form>

        <div className="divider">
          <span>o</span>
        </div>

        <button type="button" className="btn btn-microsoft" onClick={handleMicrosoft}>
          Ingresar con Microsoft
        </button>
        <button type="button" className="btn btn-google" onClick={handleGoogle}>
          Ingresar con Google
        </button>
      </div>
    </div>
  )
}