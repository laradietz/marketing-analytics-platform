import { useEffect, useMemo, useState } from 'react'
import { ArrowUpDown, BarChart3 } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { campaignsApi } from '@/api/campaigns'
import { usePlatforms } from '@/hooks/usePlatforms'
import type { Campaign } from '@/types'
import { Select } from '@/components/ui/Select'
import { Table, Thead, Tbody, Tr, Th, Td } from '@/components/ui/Table'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { SkeletonRows } from '@/components/ui/Skeleton'
import { PlatformTag } from '@/components/domain/PlatformTag'
import { formatCurrency, formatMultiplier, formatNumber, formatPercent } from '@/utils/format'
import { extractErrorMessage } from '@/api/client'

type SortKey = 'name' | 'spend' | 'revenue' | 'roas' | 'ctr' | 'cpa' | 'conversions'

export function MetricsOverviewPage() {
  const navigate = useNavigate()
  const { platforms } = usePlatforms()
  const [platformId, setPlatformId] = useState('')
  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [sortKey, setSortKey] = useState<SortKey>('spend')
  const [sortDesc, setSortDesc] = useState(true)

  const load = () => {
    setLoading(true)
    setError(null)
    campaignsApi
      .list({ size: 100, platformId: platformId ? Number(platformId) : undefined })
      .then((res) => setCampaigns(res.content))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(load, [platformId])

  const sorted = useMemo(() => {
    const value = (c: Campaign): number | string => {
      switch (sortKey) {
        case 'name':
          return c.name.toLowerCase()
        case 'spend':
          return c.metrics.totals.spend
        case 'revenue':
          return c.metrics.totals.revenue
        case 'roas':
          return c.metrics.roas
        case 'ctr':
          return c.metrics.ctr
        case 'cpa':
          return c.metrics.cpa
        case 'conversions':
          return c.metrics.totals.conversions
      }
    }
    return [...campaigns].sort((a, b) => {
      const va = value(a)
      const vb = value(b)
      const cmp = typeof va === 'string' ? va.localeCompare(vb as string) : va - (vb as number)
      return sortDesc ? -cmp : cmp
    })
  }, [campaigns, sortKey, sortDesc])

  const toggleSort = (key: SortKey) => {
    if (key === sortKey) {
      setSortDesc((v) => !v)
    } else {
      setSortKey(key)
      setSortDesc(true)
    }
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <p className="text-sm text-ink-500">Vista consolidada del rendimiento de todas tus campañas.</p>
        <Select value={platformId} onChange={(e) => setPlatformId(e.target.value)} className="w-48">
          <option value="">Todas las plataformas</option>
          {platforms.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </Select>
      </div>

      {loading && (
        <div className="rounded-xl border border-ink-200 bg-white p-5">
          <SkeletonRows rows={6} />
        </div>
      )}

      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {!loading && !error && campaigns.length === 0 && (
        <EmptyState icon={BarChart3} title="Sin campañas para mostrar" description="Creá campañas y registrá métricas para ver el consolidado acá." />
      )}

      {!loading && !error && campaigns.length > 0 && (
        <Table>
          <Thead>
            <Tr>
              <SortableTh label="Campaña" sortKey="name" current={sortKey} onSort={toggleSort} />
              <Th>Plataforma</Th>
              <SortableTh label="Gasto" sortKey="spend" current={sortKey} onSort={toggleSort} />
              <SortableTh label="Ingresos" sortKey="revenue" current={sortKey} onSort={toggleSort} />
              <SortableTh label="ROAS" sortKey="roas" current={sortKey} onSort={toggleSort} />
              <SortableTh label="CTR" sortKey="ctr" current={sortKey} onSort={toggleSort} />
              <SortableTh label="CPA" sortKey="cpa" current={sortKey} onSort={toggleSort} />
              <SortableTh label="Conversiones" sortKey="conversions" current={sortKey} onSort={toggleSort} />
            </Tr>
          </Thead>
          <Tbody>
            {sorted.map((c) => (
              <Tr key={c.id} className="cursor-pointer" onClick={() => navigate(`/campaigns/${c.id}`)}>
                <Td className="font-medium text-ink-900">{c.name}</Td>
                <Td>
                  <PlatformTag platform={c.platform} />
                </Td>
                <Td>{formatCurrency(c.metrics.totals.spend)}</Td>
                <Td>{formatCurrency(c.metrics.totals.revenue)}</Td>
                <Td className="font-medium">{formatMultiplier(c.metrics.roas)}</Td>
                <Td>{formatPercent(c.metrics.ctr)}</Td>
                <Td>{formatCurrency(c.metrics.cpa)}</Td>
                <Td>{formatNumber(c.metrics.totals.conversions)}</Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
      )}
    </div>
  )
}

function SortableTh({
  label,
  sortKey,
  current,
  onSort,
}: {
  label: string
  sortKey: SortKey
  current: SortKey
  onSort: (key: SortKey) => void
}) {
  return (
    <Th>
      <button onClick={() => onSort(sortKey)} className="flex items-center gap-1 hover:text-ink-800">
        {label}
        <ArrowUpDown className={`size-3 ${current === sortKey ? 'text-brand-600' : 'text-ink-300'}`} />
      </button>
    </Th>
  )
}
