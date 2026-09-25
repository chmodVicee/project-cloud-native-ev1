const STEPS = ['RECIBIDO', 'CONFIRMADO', 'PREPARANDO', 'LISTO', 'ENTREGADO']

export function OrderStatusStepper({ status }: { status: string }) {
  if (status === 'CANCELADO') {
    return <div className="status-step cancelled">Orden cancelada</div>
  }

  const current = STEPS.indexOf(status)

  return (
    <div className="status-steps">
      {STEPS.map((step, index) => (
        <div
          key={step}
          className={`status-step ${index <= current ? 'done' : ''} ${index === current ? 'current' : ''}`}
        >
          <span className="status-dot" />
          <span className="status-label">{step}</span>
        </div>
      ))}
    </div>
  )
}