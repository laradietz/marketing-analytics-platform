import { createContext, useCallback, useContext, useState } from 'react'
import type { ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react'
import { cn } from '@/lib/cn'

type ToastVariant = 'success' | 'error' | 'info'

interface Toast {
  id: number
  message: string
  variant: ToastVariant
}

interface ToastContextValue {
  showToast: (message: string, variant?: ToastVariant) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

const variantConfig: Record<ToastVariant, { icon: typeof CheckCircle2; classes: string }> = {
  success: { icon: CheckCircle2, classes: 'bg-white border-success-200 text-success-700' },
  error: { icon: AlertCircle, classes: 'bg-white border-danger-200 text-danger-700' },
  info: { icon: Info, classes: 'bg-white border-info-200 text-info-700' },
}

let idCounter = 0

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const showToast = useCallback((message: string, variant: ToastVariant = 'success') => {
    const id = ++idCounter
    setToasts((prev) => [...prev, { id, message, variant }])
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, 4000)
  }, [])

  const dismiss = (id: number) => setToasts((prev) => prev.filter((t) => t.id !== id))

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      {createPortal(
        <div className="fixed bottom-4 right-4 z-[100] flex flex-col gap-2">
          {toasts.map((toast) => {
            const config = variantConfig[toast.variant]
            const Icon = config.icon
            return (
              <div
                key={toast.id}
                role="status"
                className={cn(
                  'animate-fade-in flex w-80 items-start gap-2.5 rounded-lg border px-3.5 py-3 shadow-md',
                  config.classes,
                )}
              >
                <Icon className="mt-0.5 size-4 shrink-0" aria-hidden />
                <p className="flex-1 text-sm text-ink-800">{toast.message}</p>
                <button
                  onClick={() => dismiss(toast.id)}
                  aria-label="Cerrar notificación"
                  className="text-ink-400 hover:text-ink-600"
                >
                  <X className="size-4" />
                </button>
              </div>
            )
          })}
        </div>,
        document.body,
      )}
    </ToastContext.Provider>
  )
}

export function useToast(): ToastContextValue {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToast debe usarse dentro de ToastProvider')
  }
  return context
}
