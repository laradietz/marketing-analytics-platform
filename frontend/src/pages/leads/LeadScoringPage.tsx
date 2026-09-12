import { useEffect, useMemo, useState } from 'react'
import { Flame, Thermometer, Snowflake } from 'lucide-react'
import { leadsApi } from '@/api/leads'
import type { Lead } from '@/types'
import { Card, CardHeader } from '@/components/ui/Card'
import { Table, Thead, Tbody, Tr, Th, Td } from '@/components/ui/Table'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { SkeletonRows } from '@/components/ui/Skeleton'
import { LeadTemperatureBadge } from '@/components/domain/StatusBadges'
import { leadSourceLabels, leadStatusLabels } from '@/utils/labels'
import { extractErrorMessage } from '@/api/client'

export function LeadScoringPage() {
  const [leads, setLeads] = useState<Lead[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = () => {
    setLoading(true)
    setError(null)
    leadsApi
      .list({ page: 0, size: 200, sort: 'score,desc' })
      .then((res) => setLeads(res.content))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const summary = useMemo(() => {
    const hot = leads.filter((l) => l.temperature === 'HOT').length
    const warm = leads.filter((l) => l.temperature === 'WARM').length
    const cold = leads.filter((l) => l.temperature === 'COLD').length
    return { hot, warm, cold }
  }, [leads])

  if (loading) {
    return (
      <div className="rounded-xl border border-ink-200 bg-white p-5">
        <SkeletonRows rows={8} />
      </div>
    )
  }

  if (error) return <ErrorState message={error} onRetry={load} />

  if (leads.length === 0) {
    return <EmptyState title="Sin leads para calificar" description="El scoring aparecerá acá una vez que registres leads." />
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Card className="flex items-center gap-3">
          <span className="flex size-10 items-center justify-center rounded-lg bg-danger-50 text-danger-600">
            <Flame className="size-5" />
          </span>
          <div>
            <p className="text-2xl font-semibold text-ink-900">{summary.hot}</p>
            <p className="text-xs text-ink-500">Leads calientes</p>
          </div>
        </Card>
        <Card className="flex items-center gap-3">
          <span className="flex size-10 items-center justify-center rounded-lg bg-warning-50 text-warning-600">
            <Thermometer className="size-5" />
          </span>
          <div>
            <p className="text-2xl font-semibold text-ink-900">{summary.warm}</p>
            <p className="text-xs text-ink-500">Leads tibios</p>
          </div>
        </Card>
        <Card className="flex items-center gap-3">
          <span className="flex size-10 items-center justify-center rounded-lg bg-info-50 text-info-600">
            <Snowflake className="size-5" />
          </span>
          <div>
            <p className="text-2xl font-semibold text-ink-900">{summary.cold}</p>
            <p className="text-xs text-ink-500">Leads fríos</p>
          </div>
        </Card>
      </div>

      <Card padded={false}>
        <div className="p-5 pb-0">
          <CardHeader
            title="Ranking de leads por score"
            description="Calculado según origen, cantidad de contactos, estado y comportamiento en campaña"
          />
        </div>
        <Table>
          <Thead>
            <Tr>
              <Th>Lead</Th>
              <Th>Origen</Th>
              <Th>Estado</Th>
              <Th>Contactos</Th>
              <Th>Score</Th>
              <Th>Temperatura</Th>
            </Tr>
          </Thead>
          <Tbody>
            {leads.map((lead) => (
              <Tr key={lead.id}>
                <Td>
                  <p className="font-medium text-ink-900">{lead.name}</p>
                  <p className="text-xs text-ink-500">{lead.email}</p>
                </Td>
                <Td>{leadSourceLabels[lead.source]}</Td>
                <Td>{leadStatusLabels[lead.status]}</Td>
                <Td>{lead.contactCount}</Td>
                <Td className="font-semibold text-ink-900">{lead.score}</Td>
                <Td>
                  <LeadTemperatureBadge temperature={lead.temperature} />
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
      </Card>
    </div>
  )
}
