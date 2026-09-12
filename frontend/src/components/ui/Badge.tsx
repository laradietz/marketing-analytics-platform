import type { ReactNode } from 'react'
import { cn } from '@/lib/cn'

export type BadgeTone = 'neutral' | 'brand' | 'success' | 'warning' | 'danger' | 'info'

const toneClasses: Record<BadgeTone, string> = {
  neutral: 'bg-ink-100 text-ink-700',
  brand: 'bg-brand-50 text-brand-700',
  success: 'bg-success-50 text-success-700',
  warning: 'bg-warning-50 text-warning-700',
  danger: 'bg-danger-50 text-danger-700',
  info: 'bg-info-50 text-info-700',
}

interface BadgeProps {
  tone?: BadgeTone
  children: ReactNode
  className?: string
  dot?: boolean
}

export function Badge({ tone = 'neutral', children, className, dot }: BadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-md px-2 py-0.5 text-xs font-medium',
        toneClasses[tone],
        className,
      )}
    >
      {dot && <span className={cn('size-1.5 rounded-full', dotColor(tone))} aria-hidden />}
      {children}
    </span>
  )
}

function dotColor(tone: BadgeTone): string {
  switch (tone) {
    case 'success':
      return 'bg-success-600'
    case 'warning':
      return 'bg-warning-600'
    case 'danger':
      return 'bg-danger-600'
    case 'info':
      return 'bg-info-600'
    case 'brand':
      return 'bg-brand-600'
    default:
      return 'bg-ink-400'
  }
}
