import { useEffect, useState } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { FileBarChart, Download, Eye } from 'lucide-react'
import { reportsApi } from '@/api/reports'
import { campaignsApi } from '@/api/campaigns'
import { usePlatforms } from '@/hooks/usePlatforms'
import { useToast } from '@/context/ToastContext'
import type { Campaign, Report } from '@/types'
import { Card, CardHeader } from '@/components/ui/Card'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { Modal } from '@/components/ui/Modal'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { SkeletonRows } from '@/components/ui/Skeleton'
import { Table, Thead, Tbody, Tr, Th, Td } from '@/components/ui/Table'
import { formatCurrency, formatDate, formatMultiplier, formatPercent } from '@/utils/format'
import { extractErrorMessage } from '@/api/client'

const schema = z.object({
  name: z.string().min(1, 'El nombre es obligatorio'),
  periodStart: z.string().min(1, 'La fecha de inicio es obligatoria'),
  periodEnd: z.string().min(1, 'La fecha de fin es obligatoria'),
  campaignIds: z.array(z.number()).optional(),
  platformIds: z.array(z.number()).optional(),
})

type FormValues = z.infer<typeof schema>

export function ReportsPage() {
  const { showToast } = useToast()
  const { platforms } = usePlatforms()
  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [reports, setReports] = useState<Report[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [viewing, setViewing] = useState<Report | null>(null)

  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      periodStart: new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10),
      periodEnd: new Date().toISOString().slice(0, 10),
      campaignIds: [],
      platformIds: [],
    },
  })

  useEffect(() => {
    campaignsApi.list({ size: 100 }).then((res) => setCampaigns(res.content))
  }, [])

  const load = () => {
    setLoading(true)
    reportsApi
      .list(0, 10)
      .then((res) => setReports(res.content))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const onSubmit = async (values: FormValues) => {
    try {
      const report = await reportsApi.generate(values)
      showToast('Reporte generado')
      setReports((prev) => [report, ...prev])
      setViewing(report)
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const exportCsv = async (report: Report) => {
    try {
      const blob = await reportsApi.exportCsv(report.id)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `reporte-${report.id}.csv`
      link.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos exportar el reporte.'), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <Card>
        <CardHeader title="Generar reporte" description="Elegí un período y filtros opcionales para armar un reporte" />
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <Field label="Nombre del reporte" htmlFor="name" error={errors.name?.message} required>
              <Input id="name" placeholder="Ej: Reporte mensual - Septiembre" invalid={!!errors.name} {...register('name')} />
            </Field>
            <Field label="Desde" htmlFor="periodStart" error={errors.periodStart?.message} required>
              <Input id="periodStart" type="date" invalid={!!errors.periodStart} {...register('periodStart')} />
            </Field>
            <Field label="Hasta" htmlFor="periodEnd" error={errors.periodEnd?.message} required>
              <Input id="periodEnd" type="date" invalid={!!errors.periodEnd} {...register('periodEnd')} />
            </Field>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <Field label="Plataformas (opcional)" htmlFor="platformIds">
              <Controller
                control={control}
                name="platformIds"
                render={({ field }) => (
                  <div className="flex flex-wrap gap-2 rounded-lg border border-ink-200 p-2.5">
                    {platforms.map((p) => {
                      const checked = field.value?.includes(p.id) ?? false
                      return (
                        <label key={p.id} className="flex items-center gap-1.5 text-sm text-ink-600">
                          <input
                            type="checkbox"
                            checked={checked}
                            onChange={(e) => {
                              const current = field.value ?? []
                              field.onChange(e.target.checked ? [...current, p.id] : current.filter((id) => id !== p.id))
                            }}
                            className="rounded border-ink-300 text-brand-600 focus:ring-brand-500"
                          />
                          {p.name}
                        </label>
                      )
                    })}
                  </div>
                )}
              />
            </Field>
            <Field label="Campañas (opcional)" htmlFor="campaignIds">
              <Controller
                control={control}
                name="campaignIds"
                render={({ field }) => (
                  <div className="flex max-h-28 flex-col gap-1.5 overflow-y-auto rounded-lg border border-ink-200 p-2.5">
                    {campaigns.map((c) => {
                      const checked = field.value?.includes(c.id) ?? false
                      return (
                        <label key={c.id} className="flex items-center gap-1.5 text-sm text-ink-600">
                          <input
                            type="checkbox"
                            checked={checked}
                            onChange={(e) => {
                              const current = field.value ?? []
                              field.onChange(e.target.checked ? [...current, c.id] : current.filter((id) => id !== c.id))
                            }}
                            className="rounded border-ink-300 text-brand-600 focus:ring-brand-500"
                          />
                          {c.name}
                        </label>
                      )
                    })}
                  </div>
                )}
              />
            </Field>
          </div>

          <div className="flex justify-end">
            <Button type="submit" loading={isSubmitting}>
              <FileBarChart className="size-4" /> Generar reporte
            </Button>
          </div>
        </form>
      </Card>

      <div>
        <h3 className="mb-3 text-sm font-semibold text-ink-800">Reportes generados</h3>
        {loading && (
          <div className="rounded-xl border border-ink-200 bg-white p-5">
            <SkeletonRows rows={4} />
          </div>
        )}
        {!loading && error && <ErrorState message={error} onRetry={load} />}
        {!loading && !error && reports.length === 0 && (
          <EmptyState icon={FileBarChart} title="Sin reportes generados" description="Generá tu primer reporte con el formulario de arriba." />
        )}
        {!loading && !error && reports.length > 0 && (
          <Table>
            <Thead>
              <Tr>
                <Th>Nombre</Th>
                <Th>Período</Th>
                <Th>ROAS</Th>
                <Th>Generado por</Th>
                <Th />
              </Tr>
            </Thead>
            <Tbody>
              {reports.map((report) => (
                <Tr key={report.id}>
                  <Td className="font-medium text-ink-900">{report.name}</Td>
                  <Td>
                    {formatDate(report.periodStart)} — {formatDate(report.periodEnd)}
                  </Td>
                  <Td>{formatMultiplier(report.summary.kpis.roas)}</Td>
                  <Td>{report.generatedByName ?? '—'}</Td>
                  <Td>
                    <div className="flex gap-1">
                      <button onClick={() => setViewing(report)} title="Ver" className="rounded-md p-1.5 text-ink-400 hover:bg-ink-100 hover:text-ink-700">
                        <Eye className="size-4" />
                      </button>
                      <button onClick={() => exportCsv(report)} title="Exportar CSV" className="rounded-md p-1.5 text-ink-400 hover:bg-ink-100 hover:text-ink-700">
                        <Download className="size-4" />
                      </button>
                    </div>
                  </Td>
                </Tr>
              ))}
            </Tbody>
          </Table>
        )}
      </div>

      <Modal open={!!viewing} onClose={() => setViewing(null)} title={viewing?.name ?? ''} size="lg">
        {viewing && (
          <div className="flex flex-col gap-4">
            <div className="grid grid-cols-3 gap-3">
              <SummaryStat label="Inversión" value={formatCurrency(viewing.summary.kpis.totals.spend)} />
              <SummaryStat label="Ingresos" value={formatCurrency(viewing.summary.kpis.totals.revenue)} />
              <SummaryStat label="ROAS" value={formatMultiplier(viewing.summary.kpis.roas)} />
              <SummaryStat label="CTR" value={formatPercent(viewing.summary.kpis.ctr)} />
              <SummaryStat label="CPA" value={formatCurrency(viewing.summary.kpis.cpa)} />
              <SummaryStat label="Conversiones" value={String(viewing.summary.kpis.totals.conversions)} />
            </div>

            <div>
              <h4 className="mb-2 text-xs font-semibold uppercase tracking-wide text-ink-400">Campañas incluidas</h4>
              {viewing.summary.campaigns.length === 0 ? (
                <p className="text-sm text-ink-500">No hay campañas en este período.</p>
              ) : (
                <Table>
                  <Thead>
                    <Tr>
                      <Th>Campaña</Th>
                      <Th>Plataforma</Th>
                      <Th>ROAS</Th>
                    </Tr>
                  </Thead>
                  <Tbody>
                    {viewing.summary.campaigns.map((c) => (
                      <Tr key={c.campaignId}>
                        <Td>{c.campaignName}</Td>
                        <Td>{c.platformName}</Td>
                        <Td>{formatMultiplier(c.metrics.roas)}</Td>
                      </Tr>
                    ))}
                  </Tbody>
                </Table>
              )}
            </div>

            {viewing.summary.recommendations.length > 0 && (
              <div>
                <h4 className="mb-2 text-xs font-semibold uppercase tracking-wide text-ink-400">Recomendaciones</h4>
                <ul className="flex flex-col gap-2">
                  {viewing.summary.recommendations.map((rec) => (
                    <li key={rec.id} className="text-sm text-ink-600">
                      {rec.title}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <div className="flex justify-end">
              <Button variant="outline" onClick={() => exportCsv(viewing)}>
                <Download className="size-4" /> Exportar CSV
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}

function SummaryStat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg bg-ink-50 p-3">
      <p className="text-xs text-ink-500">{label}</p>
      <p className="text-lg font-semibold text-ink-900">{value}</p>
    </div>
  )
}
