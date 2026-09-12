export type ChartPaletteKey = 'brand' | 'emerald' | 'amber' | 'rose' | 'slate'
export type ChartCurve = 'smooth' | 'straight' | 'stepped'

export interface ChartStyle {
  palette: ChartPaletteKey
  curve: ChartCurve
  showGrid: boolean
  showDots: boolean
}

export const defaultChartStyle: ChartStyle = {
  palette: 'brand',
  curve: 'smooth',
  showGrid: true,
  showDots: false,
}

export const chartPalettes: Record<ChartPaletteKey, { label: string; color: string }> = {
  brand: { label: 'Índigo', color: '#4f46e5' },
  emerald: { label: 'Esmeralda', color: '#059669' },
  amber: { label: 'Ámbar', color: '#d97706' },
  rose: { label: 'Rosa', color: '#e11d48' },
  slate: { label: 'Grafito', color: '#475569' },
}

export const curveTypeMap: Record<ChartCurve, 'monotone' | 'linear' | 'step'> = {
  smooth: 'monotone',
  straight: 'linear',
  stepped: 'step',
}

const STORAGE_PREFIX = 'map.chart.'

export function readChartStyle(storageKey: string): ChartStyle {
  try {
    const raw = localStorage.getItem(STORAGE_PREFIX + storageKey)
    if (!raw) return defaultChartStyle
    const parsed = JSON.parse(raw) as Partial<ChartStyle>
    return {
      palette: parsed.palette && parsed.palette in chartPalettes ? parsed.palette : defaultChartStyle.palette,
      curve: parsed.curve && parsed.curve in curveTypeMap ? parsed.curve : defaultChartStyle.curve,
      showGrid: typeof parsed.showGrid === 'boolean' ? parsed.showGrid : defaultChartStyle.showGrid,
      showDots: typeof parsed.showDots === 'boolean' ? parsed.showDots : defaultChartStyle.showDots,
    }
  } catch {
    return defaultChartStyle
  }
}

export function writeChartStyle(storageKey: string, extra: Record<string, unknown>) {
  try {
    localStorage.setItem(STORAGE_PREFIX + storageKey, JSON.stringify(extra))
  } catch {
    // Per-viewer convenience only — safe to ignore if storage is unavailable.
  }
}
