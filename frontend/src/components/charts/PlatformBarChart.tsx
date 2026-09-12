import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { PlatformPerformance } from '@/types'
import { formatMultiplier } from '@/utils/format'

export function PlatformBarChart({ data }: { data: PlatformPerformance[] }) {
  return (
    <ResponsiveContainer width="100%" height={220}>
      <BarChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
        <XAxis
          dataKey="platformName"
          tick={{ fontSize: 11, fill: '#94a3b8' }}
          axisLine={{ stroke: '#e2e8f0' }}
          tickLine={false}
        />
        <YAxis
          tick={{ fontSize: 11, fill: '#94a3b8' }}
          axisLine={false}
          tickLine={false}
          width={40}
          tickFormatter={(v) => formatMultiplier(v)}
        />
        <Tooltip
          contentStyle={{ borderRadius: 8, borderColor: '#e2e8f0', fontSize: 12 }}
          formatter={(value) => [formatMultiplier(Number(value)), 'ROAS']}
        />
        <Bar dataKey="roas" radius={[6, 6, 0, 0]} maxBarSize={40}>
          {data.map((entry) => (
            <Cell key={entry.platformId} fill={entry.colorHex ?? '#4f46e5'} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}
