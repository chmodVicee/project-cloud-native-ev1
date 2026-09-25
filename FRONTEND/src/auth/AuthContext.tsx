import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { fetchProfile, UserProfile } from '../api/auth'
import { TOKEN_KEY } from '../api/client'

interface AuthState {
  token: string | null
  user: UserProfile | null
  loading: boolean
  setCredentials: (token: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthState | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState<boolean>(!!localStorage.getItem(TOKEN_KEY))

  useEffect(() => {
    if (token && !user) {
      fetchProfile()
        .then(setUser)
        .catch(() => {
          localStorage.removeItem(TOKEN_KEY)
          setToken(null)
        })
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [token, user])

  const setCredentials = useCallback(async (newToken: string) => {
    localStorage.setItem(TOKEN_KEY, newToken)
    setToken(newToken)
    setLoading(true)
    try {
      const profile = await fetchProfile()
      setUser(profile)
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ token, user, loading, setCredentials, logout }),
    [token, user, loading, setCredentials, logout]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return ctx
}