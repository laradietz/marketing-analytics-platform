import { useEffect, useState } from 'react'
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { LineChart as LineIcon, BarChart3, AreaChart as AreaIcon } from 'lucide-react'
import { Select } from '@/components/ui/Select'
import { ChartStyleMenu } from './ChartStyleMenu'
import { cn } from '@/lib/cn'
import { formatShortDate } from '@/utils/format'
import { computeSeries, seriesMetricDefinitions } from '@/utils/metricSeries'
import type { RawDailyPoint, SeriesMetricKey } from '@/utils/metricSeries'
import { chartPalettes, curveTypeMap, readChartStyle, writeChartStyle } from '@/utils/chartStyle'
import type { ChartStyle } from '@/utils/chartStyle'

type ChartType = 'line' | 'bar' | 'area'

interface ConfigurableTrendChartProps {
  data: RawDailyPoint[]
  storageKey: string
  defaultMetric?: SeriesMetricKey
  defaultChartType?: ChartType
  availableMetrics?: SeriesMetricKey[]
}

const chartTypeOptions: { key: ChartType; label: string; icon: typeof LineIcon }[] = [
  { key: 'line', label: 'Línea', icon: LineIcon },
  { key: 'bar', label: 'Barras', icon: BarChart3 },
  { key: 'area', label: 'Área', icon: AreaIcon },
]

const allMetrics: SeriesMetricKey[] = ['spend', 'revenue', 'impressions', 'clicks', 'conversions', 'ctr', 'cpc', 'roas']

function readStored<T extends string>(storageKey: string, field: string, fallback: T, allowed: string[]): T {
  try {
    const raw = localStorage.getItem(`map.chart.${storageKey}`)
    if (!raw) return fallback
    const parsed = JSON.parse(raw) as Record<string, string>
    const value = parsed[field]
    return value && allowed.includes(value) ? (value as T) : fallback
  } catch {
    return fallback
  }
}

export function ConfigurableTrendChart({
  data,
  storageKey,
  defaultMetric = 'spend',
  defaultChartType = 'line',
  availableMetrics = allMetrics,
}: ConfigurableTrendChartProps) {
  const [metric, setMetric] = useState<SeriesMetricKey>(() => readStored(storageKey, 'metric', defaultMetric, availableMetrics))
  const [chartType, setChartType] = useState<ChartType>(() =>
    readStored(storageKey, 'chartType', defaultChartType, ['line', 'bar', 'area']),
  )
  const [style, setStyle] = useState<ChartStyle>(() => readChartStyle(storageKey))

  useEffect(() => {
    writeChartStyle(storageKey, { metric, chartType, ...style })
  }, [storageKey, metric, chartType, style])

  const definition = seriesMetricDefinitions[metric]
  const series = computeSeries(data, metric)
  const color = chartPalettes[style.palette].color
  const curve = curveTypeMap[style.curve]
  const grid = style.showGrid ? <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} /> : null
  const dot = style.showDots ? { r: 3, fill: color, strokeWidth: 0 } : false

  return (
    <div>
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <Select value={metric} onChange={(e) => setMetric(e.target.value as SeriesMetricKey)} className="w-40">
          {availableMetrics.map((key) => (
            <option key={key} value={key}>
              {seriesMetricDefinitions[key].label}
            </option>
          ))}
        </Select>
        <div className="flex items-center gap-2">
          <div className="flex rounded-lg border border-ink-200 p-0.5">
            {chartTypeOptions.map((opt) => (
              <button
                key={opt.key}
                type="button"
                title={opt.label}
                onClick={() => setChartType(opt.key)}
                className={cn(
                  'flex size-7 items-center justify-center rounded-md transition-colors',
                  chartType === opt.key ? 'bg-brand-50 text-brand-700' : 'text-ink-400 hover:bg-ink-100 hover:text-ink-600',
                )}
              >
                <opt.icon className="size-3.5" aria-hidden />
              </button>
            ))}
          </div>
          <ChartStyleMenu style={style} onChange={setStyle} showCurveControl={chartType !== 'bar'} />
        </div>
      </div>
      <ResponsiveContainer width="100%" height={220}>
        {chartType === 'bar' ? (
          <BarChart data={series} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
            {grid}
            <XAxis dataKey="date" tickFormatter={formatShortDate} tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={{ stroke: '#e2e8f0' }} tickLine={false} />
            <YAxis tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} width={56} tickFormatter={(v) => definition.axisFormat(v)} />
            <Tooltip
              contentStyle={{ borderRadius: 8, borderColor: '#e2e8f0', fontSize: 12 }}
              labelFormatter={(v) => formatShortDate(v as string)}
              formatter={(value) => [definition.format(Number(value)), definition.label]}
            />
            <Bar dataKey="value" fill={color} radius={[4, 4, 0, 0]} maxBarSize={28} />
          </BarChart>
        ) : chartType === 'area' ? (
          <AreaChart data={series} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
            {grid}
            <XAxis dataKey="date" tickFormatter={formatShortDate} tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={{ stroke: '#e2e8f0' }} tickLine={false} />
            <YAxis tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} width={56} tickFormatter={(v) => definition.axisFormat(v)} />
            <Tooltip
              contentStyle={{ borderRadius: 8, borderColor: '#e2e8f0', fontSize: 12 }}
              labelFormatter={(v) => formatShortDate(v as string)}
              formatter={(value) => [definition.format(Number(value)), definition.label]}
            />
            <Area type={curve} dataKey="value" stroke={color} fill={color} fillOpacity={0.15} strokeWidth={2} dot={dot} />
          </AreaChart>
        ) : (
          <LineChart data={series} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
            {grid}
            <XAxis dataKey="date" tickFormatter={formatShortDate} tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={{ stroke: '#e2e8f0' }} tickLine={false} />
            <YAxis tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} width={56} tickFormatter={(v) => definition.axisFormat(v)} />
            <Tooltip
              contentStyle={{ borderRadius: 8, borderColor: '#e2e8f0', fontSize: 12 }}
              labelFormatter={(v) => formatShortDate(v as string)}
              formatter={(value) => [definition.format(Number(value)), definition.label]}
            />
            <Line type={curve} dataKey="value" stroke={color} strokeWidth={2} dot={dot} activeDot={{ r: 4 }} />
          </LineChart>
        )}
      </ResponsiveContainer>
    </div>
  )
}
