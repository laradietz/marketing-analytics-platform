import { useEffect, useRef, useState } from 'react'
import type { ChangeEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Plus, Trash2, Play, Pause, CheckCircle, XCircle, Pencil, Download, Upload } from 'lucide-react'
import { campaignsApi } from '@/api/campaigns'
import { metricsApi } from '@/api/metrics'
import { adsApi } from '@/api/ads'
import { budgetsApi } from '@/api/budgets'
import { useToast } from '@/context/ToastContext'
import type { Ad, AdRequest, Budget, BudgetRequest, Campaign, CampaignMetric, CampaignMetricRequest, CampaignRequest } from '@/types'
import { Button } from '@/components/ui/Button'
import { Card, CardHeader } from '@/components/ui/Card'
import { StatCard } from '@/components/ui/StatCard'
import { Tabs } from '@/components/ui/Tabs'
import { Modal } from '@/components/ui/Modal'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { Table, Thead, Tbody, Tr, Th, Td } from '@/components/ui/Table'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { Skeleton } from '@/components/ui/Skeleton'
import { CampaignStatusBadge } from '@/components/domain/StatusBadges'
import { PlatformTag } from '@/components/domain/PlatformTag'
import { CampaignForm } from '@/components/domain/CampaignForm'
import { MetricForm } from '@/components/domain/MetricForm'
import { AdForm } from '@/components/domain/AdForm'
import { BudgetForm } from '@/components/domain/BudgetForm'
import { ConfigurableTrendChart } from '@/components/charts/ConfigurableTrendChart'
import { campaignObjectiveLabels, adFormatLabels, adStatusLabels } from '@/utils/labels'
import { formatCurrency, formatMultiplier, formatPercent, formatDate, formatNumber } from '@/utils/format'
import { extractErrorMessage } from '@/api/client'

const tabs = [
  { key: 'overview', label: 'Resumen' },
  { key: 'metrics', label: 'Métricas' },
  { key: 'ads', label: 'Ads' },
  { key: 'budgets', label: 'Presupuestos' },
]

export function CampaignDetailPage() {
  const { id } = useParams<{ id: string }>()
  const campaignId = Number(id)
  const navigate = useNavigate()
  const { showToast } = useToast()

  const [campaign, setCampaign] = useState<Campaign | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [tab, setTab] = useState('overview')

  const [editOpen, setEditOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)

  const loadCampaign = () => {
    setLoading(true)
    setError(null)
    campaignsApi
      .getById(campaignId)
      .then(setCampaign)
      .catch((err) => setError(extractErrorMessage(err, 'No pudimos cargar la campaña.')))
      .finally(() => setLoading(false))
  }

  useEffect(loadCampaign, [campaignId])

  const handleUpdate = async (payload: CampaignRequest) => {
    try {
      const updated = await campaignsApi.update(campaignId, payload)
      setCampaign(updated)
      showToast('Campaña actualizada')
      setEditOpen(false)
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleDelete = async () => {
    setDeleteLoading(true)
    try {
      await campaignsApi.remove(campaignId)
      showToast('Campaña eliminada')
      navigate('/campaigns')
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
      setDeleteLoading(false)
    }
  }

  const handleAction = async (action: (id: number) => Promise<Campaign>, successMessage: string) => {
    try {
      const updated = await action(campaignId)
      setCampaign(updated)
      showToast(successMessage)
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  if (loading) {
    return (
      <div className="flex flex-col gap-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-40 w-full" />
      </div>
    )
  }

  if (error || !campaign) {
    return <ErrorState message={error ?? 'Campaña no encontrada.'} onRetry={loadCampaign} />
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <button
            onClick={() => navigate('/campaigns')}
            className="mb-2 flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800"
          >
            <ArrowLeft className="size-3.5" /> Volver a campañas
          </button>
          <div className="flex flex-wrap items-center gap-2.5">
            <h2 className="text-xl font-semibold text-ink-900">{campaign.name}</h2>
            <CampaignStatusBadge status={campaign.status} />
          </div>
          <div className="mt-1 flex items-center gap-3 text-sm text-ink-500">
            <PlatformTag platform={campaign.platform} />
            <span>·</span>
            <span>{campaignObjectiveLabels[campaign.objective]}</span>
            <span>·</span>
            <span>
              {formatDate(campaign.startDate)} — {campaign.endDate ? formatDate(campaign.endDate) : 'sin fecha de fin'}
            </span>
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          {(campaign.status === 'DRAFT' || campaign.status === 'PAUSED') && (
            <Button variant="outline" size="sm" onClick={() => handleAction(campaignsApi.activate, 'Campaña activada')}>
              <Play className="size-4" /> Activar
            </Button>
          )}
          {campaign.status === 'ACTIVE' && (
            <Button variant="outline" size="sm" onClick={() => handleAction(campaignsApi.pause, 'Campaña pausada')}>
              <Pause className="size-4" /> Pausar
            </Button>
          )}
          {(campaign.status === 'ACTIVE' || campaign.status === 'PAUSED') && (
            <Button variant="outline" size="sm" onClick={() => handleAction(campaignsApi.complete, 'Campaña finalizada')}>
              <CheckCircle className="size-4" /> Finalizar
            </Button>
          )}
          {campaign.status !== 'COMPLETED' && campaign.status !== 'CANCELLED' && (
            <Button variant="outline" size="sm" onClick={() => handleAction(campaignsApi.cancel, 'Campaña cancelada')}>
              <XCircle className="size-4" /> Cancelar
            </Button>
          )}
          <Button variant="outline" size="sm" onClick={() => setEditOpen(true)}>
            <Pencil className="size-4" /> Editar
          </Button>
          <Button variant="danger" size="sm" onClick={() => setDeleteOpen(true)}>
            <Trash2 className="size-4" /> Eliminar
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
        <StatCard label="Inversión" value={formatCurrency(campaign.metrics.totals.spend)} />
        <StatCard label="Ingresos" value={formatCurrency(campaign.metrics.totals.revenue)} />
        <StatCard label="ROAS" value={formatMultiplier(campaign.metrics.roas)} />
        <StatCard label="Conversiones" value={formatNumber(campaign.metrics.totals.conversions)} />
        <StatCard label="CTR" value={formatPercent(campaign.metrics.ctr)} />
        <StatCard label="CPA" value={formatCurrency(campaign.metrics.cpa)} />
      </div>

      <Tabs tabs={tabs} active={tab} onChange={setTab} />

      {tab === 'overview' && <OverviewTab campaign={campaign} />}
      {tab === 'metrics' && <MetricsTab campaignId={campaignId} onRecorded={loadCampaign} />}
      {tab === 'ads' && <AdsTab campaignId={campaignId} />}
      {tab === 'budgets' && <BudgetsTab campaignId={campaignId} />}

      <Modal open={editOpen} onClose={() => setEditOpen(false)} title="Editar campaña" size="lg">
        <CampaignForm initialValue={campaign} onSubmit={handleUpdate} onCancel={() => setEditOpen(false)} />
      </Modal>

      <ConfirmDialog
        open={deleteOpen}
        title={`¿Eliminar "${campaign.name}"?`}
        confirmLabel="Eliminar"
        danger
        loading={deleteLoading}
        onConfirm={handleDelete}
        onCancel={() => setDeleteOpen(false)}
      />
    </div>
  )
}

function OverviewTab({ campaign }: { campaign: Campaign }) {
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <Card className="lg:col-span-2">
        <CardHeader title="Descripción" />
        <p className="text-sm text-ink-600">{campaign.description || 'Sin descripción.'}</p>
        {campaign.targetAudience && (
          <>
            <h4 className="mt-4 text-xs font-semibold uppercase tracking-wide text-ink-400">Público objetivo</h4>
            <p className="mt-1 text-sm text-ink-600">{campaign.targetAudience}</p>
          </>
        )}
        {campaign.notes && (
          <>
            <h4 className="mt-4 text-xs font-semibold uppercase tracking-wide text-ink-400">Notas</h4>
            <p className="mt-1 text-sm text-ink-600">{campaign.notes}</p>
          </>
        )}
      </Card>
      <Card>
        <CardHeader title="Detalles" />
        <dl className="flex flex-col gap-3 text-sm">
          <div className="flex justify-between">
            <dt className="text-ink-500">Responsable</dt>
            <dd className="font-medium text-ink-800">{campaign.ownerName ?? '—'}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-ink-500">Presupuesto</dt>
            <dd className="font-medium text-ink-800">{formatCurrency(campaign.budget)}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-ink-500">CPC</dt>
            <dd className="font-medium text-ink-800">{formatCurrency(campaign.metrics.cpc)}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-ink-500">CPM</dt>
            <dd className="font-medium text-ink-800">{formatCurrency(campaign.metrics.cpm)}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-ink-500">Tasa de conversión</dt>
            <dd className="font-medium text-ink-800">{formatPercent(campaign.metrics.conversionRate)}</dd>
          </div>
        </dl>
      </Card>
    </div>
  )
}

function MetricsTab({ campaignId, onRecorded }: { campaignId: number; onRecorded: () => void }) {
  const { showToast } = useToast()
  const [metrics, setMetrics] = useState<CampaignMetric[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [formOpen, setFormOpen] = useState(false)
  const [importing, setImporting] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const load = () => {
    setLoading(true)
    metricsApi
      .listByCampaign(campaignId)
      .then(setMetrics)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(load, [campaignId])

  const handleSubmit = async (payload: CampaignMetricRequest) => {
    try {
      await metricsApi.record(campaignId, payload)
      showToast('Métricas registradas')
      setFormOpen(false)
      load()
      onRecorded()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleFileSelected = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return

    setImporting(true)
    try {
      const result = await metricsApi.importCsv(campaignId, file)
      if (result.imported > 0) {
        showToast(`Se importaron ${result.imported} de ${result.totalRows} filas`)
        load()
        onRecorded()
      }
      if (result.errors.length > 0) {
        showToast(`${result.errors.length} fila(s) con errores: ${result.errors[0].message}`, 'error')
      }
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos importar el archivo.'), 'error')
    } finally {
      setImporting(false)
    }
  }

  const handleDownloadTemplate = async () => {
    try {
      const blob = await metricsApi.downloadTemplate()
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'plantilla-metricas.csv'
      link.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-end gap-2">
        <Button variant="ghost" size="sm" onClick={handleDownloadTemplate}>
          <Download className="size-4" /> Plantilla CSV
        </Button>
        <input ref={fileInputRef} type="file" accept=".csv" className="hidden" onChange={handleFileSelected} />
        <Button variant="outline" size="sm" loading={importing} onClick={() => fileInputRef.current?.click()}>
          <Upload className="size-4" /> Importar CSV
        </Button>
        <Button size="sm" onClick={() => setFormOpen(true)}>
          <Plus className="size-4" /> Registrar métricas
        </Button>
      </div>

      {loading && <Skeleton className="h-48 w-full" />}
      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {!loading && !error && (
        <>
          {metrics.length === 0 ? (
            <EmptyState title="Todavía no hay métricas cargadas" description="Registrá el primer día de rendimiento de esta campaña." />
          ) : (
            <>
              <Card>
                <CardHeader title="Evolución diaria" description="Elegí la métrica y el tipo de gráfico" />
                <ConfigurableTrendChart
                  data={metrics.map((m) => ({ date: m.recordedDate, ...m }))}
                  storageKey={`campaign-${campaignId}-trend`}
                  defaultMetric="spend"
                />
              </Card>
              <Table>
                <Thead>
                  <Tr>
                    <Th>Fecha</Th>
                    <Th>Impresiones</Th>
                    <Th>Clics</Th>
                    <Th>Conversiones</Th>
                    <Th>Gasto</Th>
                    <Th>Ingresos</Th>
                    <Th>CTR</Th>
                    <Th>ROAS</Th>
                  </Tr>
                </Thead>
                <Tbody>
                  {metrics.map((m) => (
                    <Tr key={m.id}>
                      <Td>{formatDate(m.recordedDate)}</Td>
                      <Td>{formatNumber(m.impressions)}</Td>
                      <Td>{formatNumber(m.clicks)}</Td>
                      <Td>{formatNumber(m.conversions)}</Td>
                      <Td>{formatCurrency(m.spend)}</Td>
                      <Td>{formatCurrency(m.revenue)}</Td>
                      <Td>{formatPercent(m.ctr)}</Td>
                      <Td className="font-medium">{formatMultiplier(m.roas)}</Td>
                    </Tr>
                  ))}
                </Tbody>
              </Table>
            </>
          )}
        </>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Registrar métricas diarias" size="lg">
        <MetricForm onSubmit={handleSubmit} onCancel={() => setFormOpen(false)} />
      </Modal>
    </div>
  )
}

function AdsTab({ campaignId }: { campaignId: number }) {
  const { showToast } = useToast()
  const [ads, setAds] = useState<Ad[]>([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [deleting, setDeleting] = useState<Ad | null>(null)

  const load = () => {
    setLoading(true)
    adsApi.listByCampaign(campaignId).then(setAds).finally(() => setLoading(false))
  }

  useEffect(load, [campaignId])

  const handleCreate = async (payload: AdRequest) => {
    try {
      await adsApi.create(campaignId, payload)
      showToast('Ad creado')
      setFormOpen(false)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleDelete = async () => {
    if (!deleting) return
    try {
      await adsApi.remove(campaignId, deleting.id)
      showToast('Ad eliminado')
      setDeleting(null)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  if (loading) return <Skeleton className="h-40 w-full" />

  return (
    <div className="flex flex-col gap-4">
      <div className="flex justify-end">
        <Button size="sm" onClick={() => setFormOpen(true)}>
          <Plus className="size-4" /> Nuevo ad
        </Button>
      </div>

      {ads.length === 0 ? (
        <EmptyState title="Sin creatividades cargadas" description="Agregá los avisos que forman parte de esta campaña." />
      ) : (
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-3">
          {ads.map((ad) => (
            <Card key={ad.id} className="flex flex-col gap-2">
              <div className="flex items-start justify-between">
                <span className="text-sm font-medium text-ink-900">{ad.name}</span>
                <button onClick={() => setDeleting(ad)} className="text-ink-400 hover:text-danger-600">
                  <Trash2 className="size-4" />
                </button>
              </div>
              <span className="text-xs text-ink-500">{adFormatLabels[ad.format]} · {adStatusLabels[ad.status]}</span>
              {ad.headline && <p className="text-sm font-medium text-ink-700">{ad.headline}</p>}
              {ad.body && <p className="line-clamp-2 text-xs text-ink-500">{ad.body}</p>}
              {ad.ctaLabel && (
                <span className="mt-1 inline-flex w-fit rounded-md bg-brand-50 px-2 py-1 text-xs font-medium text-brand-700">
                  {ad.ctaLabel}
                </span>
              )}
            </Card>
          ))}
        </div>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Nuevo ad">
        <AdForm onSubmit={handleCreate} onCancel={() => setFormOpen(false)} />
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        title={`¿Eliminar "${deleting?.name}"?`}
        confirmLabel="Eliminar"
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </div>
  )
}

function BudgetsTab({ campaignId }: { campaignId: number }) {
  const { showToast } = useToast()
  const [budgets, setBudgets] = useState<Budget[]>([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [deleting, setDeleting] = useState<Budget | null>(null)

  const load = () => {
    setLoading(true)
    budgetsApi.listByCampaign(campaignId).then(setBudgets).finally(() => setLoading(false))
  }

  useEffect(load, [campaignId])

  const handleCreate = async (payload: BudgetRequest) => {
    try {
      await budgetsApi.create(campaignId, payload)
      showToast('Presupuesto agregado')
      setFormOpen(false)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleDelete = async () => {
    if (!deleting) return
    try {
      await budgetsApi.remove(campaignId, deleting.id)
      showToast('Presupuesto eliminado')
      setDeleting(null)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  if (loading) return <Skeleton className="h-40 w-full" />

  return (
    <div className="flex flex-col gap-4">
      <div className="flex justify-end">
        <Button size="sm" onClick={() => setFormOpen(true)}>
          <Plus className="size-4" /> Nuevo período
        </Button>
      </div>

      {budgets.length === 0 ? (
        <EmptyState title="Sin presupuestos planificados" description="Agregá una asignación de presupuesto por período." />
      ) : (
        <Table>
          <Thead>
            <Tr>
              <Th>Período</Th>
              <Th>Planificado</Th>
              <Th>Gasto real</Th>
              <Th>Notas</Th>
              <Th />
            </Tr>
          </Thead>
          <Tbody>
            {budgets.map((b) => (
              <Tr key={b.id}>
                <Td>
                  {formatDate(b.periodStart)} — {formatDate(b.periodEnd)}
                </Td>
                <Td>{formatCurrency(b.plannedAmount)}</Td>
                <Td>{formatCurrency(b.actualSpend)}</Td>
                <Td className="max-w-xs truncate">{b.notes || '—'}</Td>
                <Td>
                  <button onClick={() => setDeleting(b)} className="text-ink-400 hover:text-danger-600">
                    <Trash2 className="size-4" />
                  </button>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Nuevo período de presupuesto">
        <BudgetForm onSubmit={handleCreate} onCancel={() => setFormOpen(false)} />
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        title="¿Eliminar este presupuesto?"
        confirmLabel="Eliminar"
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </div>
  )
}
