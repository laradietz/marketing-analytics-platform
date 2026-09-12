import { forwardRef } from 'react'
import type { InputHTMLAttributes } from 'react'
import { cn } from '@/lib/cn'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  invalid?: boolean
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, invalid, ...props }, ref) => {
    return (
      <input
        ref={ref}
        className={cn(
          'h-9 w-full rounded-lg border bg-white px-3 text-sm text-ink-900 placeholder:text-ink-400',
          'transition-shadow duration-150',
          'focus:outline-none focus:ring-2 focus:ring-brand-500/40 focus:border-brand-500',
          'disabled:cursor-not-allowed disabled:bg-ink-50 disabled:text-ink-400',
          invalid ? 'border-danger-400' : 'border-ink-200',
          className,
        )}
        {...props}
      />
    )
  },
)
Input.displayName = 'Input'
