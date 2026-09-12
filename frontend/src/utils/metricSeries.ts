import { formatCurrency, formatCurrencyCompact, formatMultiplier, formatNumber, formatPercent } from './format'

export type SeriesMetricKey = 'spend' | 'revenue' | 'impressions' | 'clicks' | 'conversions' | 'ctr' | 'cpc' | 'roas'

export interface RawDailyPoint {
  date: string
  impressions: number
  clicks: number
  conversions: number
  spend: number
  revenue: number
}

interface MetricDefinition {
  label: string
  color: string
  format: (value: number) => string
  axisFormat: (value: number) => string
  compute: (point: RawDailyPoint) => number
}

export const seriesMetricDefinitions: Record<SeriesMetricKey, MetricDefinition> = {
  spend: {
    label: 'Inversión',
    color: '#4f46e5',
    format: formatCurrency,
    axisFormat: formatCurrencyCompact,
    compute: (p) => p.spend,
  },
  revenue: {
    label: 'Ingresos',
    color: '#16a34a',
    format: formatCurrency,
    axisFormat: formatCurrencyCompact,
    compute: (p) => p.revenue,
  },
  impressions: {
    label: 'Impresiones',
    color: '#0284c7',
    format: formatNumber,
    axisFormat: formatNumber,
    compute: (p) => p.impressions,
  },
  clicks: {
    label: 'Clics',
    color: '#d97706',
    format: formatNumber,
    axisFormat: formatNumber,
    compute: (p) => p.clicks,
  },
  conversions: {
    label: 'Conversiones',
    color: '#16a34a',
    format: formatNumber,
    axisFormat: formatNumber,
    compute: (p) => p.conversions,
  },
  ctr: {
    label: 'CTR',
    color: '#0284c7',
    format: formatPercent,
    axisFormat: formatPercent,
    // clicks / impressions * 100, guarded against division by zero — same rule as the backend.
    compute: (p) => (p.impressions > 0 ? (p.clicks / p.impressions) * 100 : 0),
  },
  cpc: {
    label: 'CPC',
    color: '#dc2626',
    format: formatCurrency,
    axisFormat: formatCurrencyCompact,
    compute: (p) => (p.clicks > 0 ? p.spend / p.clicks : 0),
  },
  roas: {
    label: 'ROAS',
    color: '#4f46e5',
    format: formatMultiplier,
    axisFormat: formatMultiplier,
    compute: (p) => (p.spend > 0 ? p.revenue / p.spend : 0),
  },
}

export function computeSeries(points: RawDailyPoint[], metric: SeriesMetricKey): { date: string; value: number }[] {
  const definition = seriesMetricDefinitions[metric]
  return points.map((p) => ({ date: p.date, value: definition.compute(p) }))
}
