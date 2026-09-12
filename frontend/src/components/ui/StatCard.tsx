import type { LucideIcon } from 'lucide-react'
import { ArrowDownRight, ArrowUpRight } from 'lucide-react'
import { cn } from '@/lib/cn'

interface StatCardProps {
  label: string
  value: string
  icon?: LucideIcon
  trend?: { value: string; direction: 'up' | 'down'; positive?: boolean }
  className?: string
}

export function StatCard({ label, value, icon: Icon, trend, className }: StatCardProps) {
  const trendPositive = trend?.positive ?? trend?.direction === 'up'
  return (
    <div className={cn('min-w-0 rounded-xl border border-ink-200 bg-white p-4 shadow-xs', className)}>
      <div className="flex items-center justify-between gap-2">
        <span className="text-xs font-medium text-ink-500">{label}</span>
        {Icon && (
          <span className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-brand-50 text-brand-600">
            <Icon className="size-4" aria-hidden />
          </span>
        )}
      </div>
      <div className="mt-2 flex items-baseline gap-2">
        <span className="truncate text-xl font-semibold tracking-tight text-ink-900 sm:text-2xl">{value}</span>
      </div>
      {trend && (
        <div
          className={cn(
            'mt-1.5 inline-flex items-center gap-1 text-xs font-medium',
            trendPositive ? 'text-success-600' : 'text-danger-600',
          )}
        >
          {trend.direction === 'up' ? (
            <ArrowUpRight className="size-3.5" aria-hidden />
          ) : (
            <ArrowDownRight className="size-3.5" aria-hidden />
          )}
          {trend.value}
        </div>
      )}
    </div>
  )
}
