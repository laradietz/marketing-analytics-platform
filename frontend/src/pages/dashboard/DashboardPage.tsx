import { useEffect, useMemo, useState } from 'react'
import { DollarSign, MousePointerClick, Target, TrendingUp, Percent, Wallet } from 'lucide-react'
import { dashboardApi } from '@/api/dashboard'
import { campaignsApi } from '@/api/campaigns'
import { usePlatforms } from '@/hooks/usePlatforms'
import type { Campaign, DashboardData, DashboardFilters } from '@/types'
import { Card, CardHeader } from '@/components/ui/Card'
import { StatCard } from '@/components/ui/StatCard'
import { Select } from '@/components/ui/Select'
import { Input } from '@/components/ui/Input'
import { SkeletonCard } from '@/components/ui/Skeleton'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { ConfigurableTrendChart } from '@/components/charts/ConfigurableTrendChart'
import { PlatformBarChart } from '@/components/charts/PlatformBarChart'
import { BudgetDonutChart } from '@/components/charts/BudgetDonutChart'
import { LeadTemperatureBadge } from '@/components/domain/StatusBadges'
import { formatCurrency, formatMultiplier, formatPercent, formatNumber, timeAgo } from '@/utils/format'
import { campaignObjectiveLabels } from '@/utils/labels'
import { extractErrorMessage } from '@/api/client'

const objectives = Object.entries(campaignObjectiveLabels)

function defaultFilters(): DashboardFilters {
  const to = new Date()
  const from = new Date()
  from.setDate(from.getDate() - 30)
  return { from: from.toISOString().slice(0, 10), to: to.toISOString().slice(0, 10) }
}

export function DashboardPage() {
  const [filters, setFilters] = useState<DashboardFilters>(defaultFilters)
  const [data, setData] = useState<DashboardData | null>(null)
  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { platforms } = usePlatforms()

  useEffect(() => {
    campaignsApi.list({ size: 100 }).then((res) => setCampaigns(res.content)).catch(() => undefined)
  }, [])

  const load = () => {
    setLoading(true)
    setError(null)
    dashboardApi
      .get(filters)
      .then(setData)
      .catch((err) => setError(extractErrorMessage(err, 'No pudimos cargar el dashboard.')))
      .finally(() => setLoading(false))
  }

  useEffect(load, [filters])

  const hasAnyData = useMemo(() => (data ? data.kpis.totals.impressions > 0 || data.kpis.totals.spend > 0 : false), [data])

  return (
    <div className="flex flex-col gap-6">
      <FiltersBar filters={filters} setFilters={setFilters} campaigns={campaigns} platforms={platforms} />

      {loading && (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
          {Array.from({ length: 6 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      )}

      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {!loading && !error && data && !hasAnyData && (
        <EmptyState
          icon={TrendingUp}
          title="Todavía no hay métricas en este período"
          description="Registrá métricas en tus campañas o ajustá los filtros para ver resultados acá."
        />
      )}

      {!loading && !error && data && hasAnyData && (
        <>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
            <StatCard label="Inversión total" value={formatCurrency(data.kpis.totals.spend)} icon={Wallet} />
            <StatCard label="Ingresos" value={formatCurrency(data.kpis.totals.revenue)} icon={DollarSign} />
            <StatCard label="ROAS" value={formatMultiplier(data.kpis.roas)} icon={TrendingUp} />
            <StatCard label="Conversiones" value={formatNumber(data.kpis.totals.conversions)} icon={Target} />
            <StatCard label="CTR" value={formatPercent(data.kpis.ctr)} icon={MousePointerClick} />
            <StatCard label="CPA" value={formatCurrency(data.kpis.cpa)} icon={Percent} />
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader title="Evolución diaria" description="Elegí la métrica y el tipo de gráfico" />
              <ConfigurableTrendChart data={data.dailyBreakdown} storageKey="dashboard-trend-1" defaultMetric="spend" />
            </Card>
            <Card>
              <CardHeader title="Evolución diaria" description="Elegí la métrica y el tipo de gráfico" />
              <ConfigurableTrendChart data={data.dailyBreakdown} storageKey="dashboard-trend-2" defaultMetric="conversions" defaultChartType="bar" />
            </Card>
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader title="Rendimiento por plataforma" description="ROAS por canal" />
              {data.platformPerformance.length ? (
                <PlatformBarChart data={data.platformPerformance} />
              ) : (
                <EmptyState title="Sin datos de plataformas" className="border-none py-8" />
              )}
            </Card>
            <Card>
              <CardHeader title="Distribución del presupuesto" description="Inversión por plataforma" />
              {data.budgetDistribution.length ? (
                <div className="flex items-center gap-6">
                  <BudgetDonutChart data={data.budgetDistribution} />
                  <ul className="flex flex-1 flex-col gap-2">
                    {data.budgetDistribution.map((item) => (
                      <li key={item.platformId} className="flex items-center justify-between text-sm">
                        <span className="flex items-center gap-2">
                          <span className="size-2 rounded-full" style={{ backgroundColor: item.colorHex ?? '#94a3b8' }} />
                          {item.platformName}
                        </span>
                        <span className="font-medium text-ink-700">{item.percentage.toFixed(0)}%</span>
                      </li>
                    ))}
                  </ul>
                </div>
              ) : (
                <EmptyState title="Sin datos de presupuesto" className="border-none py-8" />
              )}
            </Card>
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <Card padded={false}>
              <div className="p-5 pb-0">
                <CardHeader title="Mejores campañas" description="Ordenadas por ROAS" />
              </div>
              <RankingList items={data.topCampaigns} />
            </Card>
            <Card padded={false}>
              <div className="p-5 pb-0">
                <CardHeader title="Campañas con peor rendimiento" description="Ordenadas por ROAS" />
              </div>
              <RankingList items={data.bottomCampaigns} />
            </Card>
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader title="Últimos leads" />
              {data.recentLeads.length ? (
                <ul className="flex flex-col gap-3">
                  {data.recentLeads.map((lead) => (
                    <li key={lead.id} className="flex items-center justify-between gap-3">
                      <div>
                        <p className="text-sm font-medium text-ink-800">{lead.name}</p>
                        <p className="text-xs text-ink-500">{lead.email}</p>
                      </div>
                      <LeadTemperatureBadge temperature={lead.temperature} />
                    </li>
                  ))}
                </ul>
              ) : (
                <EmptyState title="Todavía no hay leads" className="border-none py-6" />
              )}
            </Card>
            <Card>
              <CardHeader title="Recomendaciones recientes" />
              {data.recentRecommendations.length ? (
                <ul className="flex flex-col gap-3">
                  {data.recentRecommendations.map((rec) => (
                    <li key={rec.id} className="border-b border-ink-100 pb-3 last:border-0 last:pb-0">
                      <p className="text-sm font-medium text-ink-800">{rec.title}</p>
                      <p className="mt-0.5 line-clamp-2 text-xs text-ink-500">{rec.description}</p>
                    </li>
                  ))}
                </ul>
              ) : (
                <EmptyState title="Sin recomendaciones todavía" description="Generalas desde la sección de Recomendaciones." className="border-none py-6" />
              )}
            </Card>
          </div>
        </>
      )}

      {!loading && !error && data && (
        <Card>
          <CardHeader title="Actividad reciente" />
          {data.recentActivity.length ? (
            <ul className="flex flex-col gap-2.5">
              {data.recentActivity.map((entry) => (
                <li key={entry.id} className="flex items-center justify-between text-sm">
                  <span className="text-ink-600">{entry.description}</span>
                  <span className="shrink-0 text-xs text-ink-400">{timeAgo(entry.createdAt)}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-sm text-ink-500">Sin actividad reciente.</p>
          )}
        </Card>
      )}
    </div>
  )
}

function RankingList({ items }: { items: DashboardData['topCampaigns'] }) {
  if (!items.length) {
    return <EmptyState title="Sin datos suficientes" className="border-none py-6" />
  }
  return (
    <ul className="divide-y divide-ink-100">
      {items.map((item) => (
        <li key={item.campaignId} className="flex items-center justify-between px-5 py-3">
          <div>
            <p className="text-sm font-medium text-ink-800">{item.campaignName}</p>
            <p className="text-xs text-ink-500">{item.platformName}</p>
          </div>
          <span className="text-sm font-semibold text-ink-900">{formatMultiplier(item.roas)}</span>
        </li>
      ))}
    </ul>
  )
}

interface FiltersBarProps {
  filters: DashboardFilters
  setFilters: (f: DashboardFilters) => void
  campaigns: Campaign[]
  platforms: { id: number; name: string }[]
}

function FiltersBar({ filters, setFilters, campaigns, platforms }: FiltersBarProps) {
  return (
    <div className="flex flex-wrap items-end gap-3 rounded-xl border border-ink-200 bg-white p-4">
      <div className="flex flex-col gap-1">
        <label className="text-xs font-medium text-ink-500">Desde</label>
        <Input
          type="date"
          value={filters.from ?? ''}
          onChange={(e) => setFilters({ ...filters, from: e.target.value })}
          className="w-40"
        />
      </div>
      <div className="flex flex-col gap-1">
        <label className="text-xs font-medium text-ink-500">Hasta</label>
        <Input
          type="date"
          value={filters.to ?? ''}
          onChange={(e) => setFilters({ ...filters, to: e.target.value })}
          className="w-40"
        />
      </div>
      <div className="flex flex-col gap-1">
        <label className="text-xs font-medium text-ink-500">Plataforma</label>
        <Select
          className="w-40"
          value={filters.platformId ?? ''}
          onChange={(e) => setFilters({ ...filters, platformId: e.target.value ? Number(e.target.value) : undefined })}
        >
          <option value="">Todas</option>
          {platforms.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </Select>
      </div>
      <div className="flex flex-col gap-1">
        <label className="text-xs font-medium text-ink-500">Campaña</label>
        <Select
          className="w-48"
          value={filters.campaignId ?? ''}
          onChange={(e) => setFilters({ ...filters, campaignId: e.target.value ? Number(e.target.value) : undefined })}
        >
          <option value="">Todas</option>
          {campaigns.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </Select>
      </div>
      <div className="flex flex-col gap-1">
        <label className="text-xs font-medium text-ink-500">Objetivo</label>
        <Select
          className="w-40"
          value={filters.objective ?? ''}
          onChange={(e) => setFilters({ ...filters, objective: e.target.value || undefined })}
        >
          <option value="">Todos</option>
          {objectives.map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
      </div>
    </div>
  )
}
