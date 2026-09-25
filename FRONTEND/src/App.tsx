import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { CartProvider } from './cart/CartContext'
import { Layout } from './components/Layout'
import { LoginPage } from './pages/LoginPage'
import { AuthCallbackPage } from './pages/AuthCallbackPage'
import { CatalogPage } from './pages/CatalogPage'
import { CartPage } from './pages/CartPage'
import { MyOrdersPage } from './pages/MyOrdersPage'
import { AdminProductsPage } from './pages/admin/AdminProductsPage'
import { AdminOrdersPage } from './pages/admin/AdminOrdersPage'
import { AdminUsersPage } from './pages/admin/AdminUsersPage'
import { AdminRoute, ProtectedRoute } from './routes/AuthGuards'

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/callback" element={<AuthCallbackPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<CatalogPage />} />
              <Route path="/carrito" element={<CartPage />} />
              <Route path="/mis-pedidos" element={<MyOrdersPage />} />

              <Route element={<AdminRoute />}>
                <Route path="/admin/productos" element={<AdminProductsPage />} />
                <Route path="/admin/ordenes" element={<AdminOrdersPage />} />
                <Route path="/admin/usuarios" element={<AdminUsersPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </CartProvider>
    </AuthProvider>
  )
}