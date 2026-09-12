import { cn } from '@/lib/cn'

interface ProgressBarProps {
  value: number
  className?: string
  tone?: 'brand' | 'success' | 'warning' | 'danger'
}

const toneClasses = {
  brand: 'bg-brand-600',
  success: 'bg-success-600',
  warning: 'bg-warning-600',
  danger: 'bg-danger-600',
}

export function ProgressBar({ value, className, tone = 'brand' }: ProgressBarProps) {
  const clamped = Math.min(100, Math.max(0, value))
  return (
    <div className={cn('h-1.5 w-full overflow-hidden rounded-full bg-ink-100', className)}>
      <div
        className={cn('h-full rounded-full transition-all duration-300', toneClasses[tone])}
        style={{ width: `${clamped}%` }}
      />
    </div>
  )
}
