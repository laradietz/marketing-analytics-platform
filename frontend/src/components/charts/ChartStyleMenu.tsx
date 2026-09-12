import { useEffect, useRef, useState } from 'react'
import { Paintbrush, Check } from 'lucide-react'
import { cn } from '@/lib/cn'
import { chartPalettes } from '@/utils/chartStyle'
import type { ChartCurve, ChartPaletteKey, ChartStyle } from '@/utils/chartStyle'

interface ChartStyleMenuProps {
  style: ChartStyle
  onChange: (style: ChartStyle) => void
  /** Curve control only makes sense for line/area charts. */
  showCurveControl?: boolean
}

const curveOptions: { key: ChartCurve; label: string }[] = [
  { key: 'smooth', label: 'Curva suave' },
  { key: 'straight', label: 'Recta' },
  { key: 'stepped', label: 'Escalonada' },
]

export function ChartStyleMenu({ style, onChange, showCurveControl = true }: ChartStyleMenuProps) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const handleClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [open])

  return (
    <div className="relative" ref={ref}>
      <button
        type="button"
        title="Estilo del gráfico"
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="menu"
        aria-expanded={open}
        className={cn(
          'flex size-7 items-center justify-center rounded-md border border-ink-200 transition-colors',
          open ? 'bg-brand-50 text-brand-700' : 'text-ink-400 hover:bg-ink-100 hover:text-ink-600',
        )}
      >
        <Paintbrush className="size-3.5" aria-hidden />
      </button>

      {open && (
        <div
          role="menu"
          className="absolute right-0 z-20 mt-1 w-56 animate-fade-in rounded-lg border border-ink-200 bg-white p-3 shadow-md"
        >
          <p className="mb-1.5 text-xs font-medium text-ink-500">Color</p>
          <div className="mb-3 flex gap-1.5">
            {(Object.entries(chartPalettes) as [ChartPaletteKey, (typeof chartPalettes)[ChartPaletteKey]][]).map(
              ([key, { label, color }]) => (
                <button
                  key={key}
                  type="button"
                  title={label}
                  onClick={() => onChange({ ...style, palette: key })}
                  className="flex size-6 items-center justify-center rounded-full ring-offset-2 focus-visible:outline-none"
                  style={{ backgroundColor: color, boxShadow: style.palette === key ? `0 0 0 2px white, 0 0 0 3.5px ${color}` : undefined }}
                >
                  {style.palette === key && <Check className="size-3 text-white" />}
                </button>
              ),
            )}
          </div>

          {showCurveControl && (
            <>
              <p className="mb-1.5 text-xs font-medium text-ink-500">Trazado</p>
              <div className="mb-3 flex flex-col gap-1">
                {curveOptions.map((opt) => (
                  <label key={opt.key} className="flex cursor-pointer items-center gap-2 text-sm text-ink-700">
                    <input
                      type="radio"
                      name="curve"
                      checked={style.curve === opt.key}
                      onChange={() => onChange({ ...style, curve: opt.key })}
                      className="text-brand-600 focus:ring-brand-500"
                    />
                    {opt.label}
                  </label>
                ))}
              </div>
            </>
          )}

          <label className="flex cursor-pointer items-center gap-2 text-sm text-ink-700">
            <input
              type="checkbox"
              checked={style.showGrid}
              onChange={(e) => onChange({ ...style, showGrid: e.target.checked })}
              className="rounded text-brand-600 focus:ring-brand-500"
            />
            Mostrar grilla
          </label>
          <label className="mt-1.5 flex cursor-pointer items-center gap-2 text-sm text-ink-700">
            <input
              type="checkbox"
              checked={style.showDots}
              onChange={(e) => onChange({ ...style, showDots: e.target.checked })}
              className="rounded text-brand-600 focus:ring-brand-500"
            />
            Mostrar puntos
          </label>
        </div>
      )}
    </div>
  )
}
