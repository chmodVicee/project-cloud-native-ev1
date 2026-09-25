import { useEffect, useState } from 'react'
import { fetchMyOrders, Order } from '../api/orders'
import { OrderStatusStepper } from '../components/OrderStatusStepper'

const REFRESH_MS = 4000

export function MyOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    const load = () => {
      fetchMyOrders()
        .then((data) => {
          if (active) setOrders(data)
        })
        .catch(() => {
          if (active) setError('Error cargando tus pedidos')
        })
    }
    load()
    const timer = setInterval(load, REFRESH_MS)
    return () => {
      active = false
      clearInterval(timer)
    }
  }, [])

  const formatPrice = (value: number) =>
    new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(value)

  return (
    <div>
      <h2>Mis pedidos</h2>
      {error && <div className="alert alert-error">{error}</div>}

      {orders.length === 0 && !error && <p className="muted">Aún no tienes pedidos.</p>}

      {orders.map((order) => (
        <div className="card order-card" key={order.id}>
          <div className="order-head">
            <span>
              <strong>Orden #{order.id}</strong>
            </span>
            <span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span>
            <span className="muted">{new Date(order.createdAt).toLocaleString('es-CL')}</span>
            <strong>{formatPrice(order.total)}</strong>
          </div>
          <OrderStatusStepper status={order.status} />
          <table className="table">
            <thead>
              <tr>
                <th>Producto</th>
                <th>Cantidad</th>
                <th>Precio</th>
                <th>Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {order.items.map((item) => (
                <tr key={item.id}>
                  <td>{item.productName}</td>
                  <td>{item.quantity}</td>
                  <td>{formatPrice(item.price)}</td>
                  <td>{formatPrice(item.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}
    </div>
  )
}