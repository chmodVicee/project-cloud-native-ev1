import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function ProtectedRoute() {
  const { token, loading } = useAuth()
  const location = useLocation()

  if (loading) return <div className="centered">Cargando...</div>
  if (!token) return <Navigate to="/login" state={{ from: location }} replace />
  return <Outlet />
}

export function AdminRoute() {
  const { token, user, loading } = useAuth()
  const location = useLocation()

  if (loading) return <div className="centered">Cargando...</div>
  if (!token) return <Navigate to="/login" state={{ from: location }} replace />
  if (user?.role !== 'ADMIN') return <Navigate to="/" replace />
  return <Outlet />
}