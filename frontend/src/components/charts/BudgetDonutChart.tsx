import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import type { BudgetDistributionItem } from '@/types'
import { formatCurrency } from '@/utils/format'

export function BudgetDonutChart({ data }: { data: BudgetDistributionItem[] }) {
  return (
    <div className="flex items-center gap-4">
      <ResponsiveContainer width="100%" height={180}>
        <PieChart>
          <Pie
            data={data}
            dataKey="spend"
            nameKey="platformName"
            innerRadius={50}
            outerRadius={80}
            paddingAngle={2}
            strokeWidth={2}
            stroke="#ffffff"
          >
            {data.map((entry) => (
              <Cell key={entry.platformId} fill={entry.colorHex ?? '#4f46e5'} />
            ))}
          </Pie>
          <Tooltip
            contentStyle={{ borderRadius: 8, borderColor: '#e2e8f0', fontSize: 12 }}
            formatter={(value, _name, item) => [formatCurrency(Number(value)), item.payload.platformName]}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  )
}
