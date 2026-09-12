import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, Search, Megaphone, Pencil, Trash2, Play, Pause, CheckCircle, XCircle } from 'lucide-react'
import { campaignsApi } from '@/api/campaigns'
import type { CampaignListParams } from '@/api/campaigns'
import { usePlatforms } from '@/hooks/usePlatforms'
import { useDebounce } from '@/hooks/useDebounce'
import { useToast } from '@/context/ToastContext'
import type { Campaign, CampaignRequest } from '@/types'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Modal } from '@/components/ui/Modal'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { Table, Thead, Tbody, Tr, Th, Td } from '@/components/ui/Table'
import { Pagination } from '@/components/ui/Pagination'
import { Dropdown } from '@/components/ui/Dropdown'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { SkeletonRows } from '@/components/ui/Skeleton'
import { PlatformTag } from '@/components/domain/PlatformTag'
import { CampaignStatusBadge } from '@/components/domain/StatusBadges'
import { CampaignForm } from '@/components/domain/CampaignForm'
import { campaignObjectiveLabels, campaignStatusLabels } from '@/utils/labels'
import { formatCurrency, formatMultiplier, formatDate } from '@/utils/format'
import { extractErrorMessage } from '@/api/client'

export function CampaignsListPage() {
  const navigate = useNavigate()
  const { showToast } = useToast()
  const { platforms } = usePlatforms()

  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [platformId, setPlatformId] = useState('')
  const [objective, setObjective] = useState('')
  const [page, setPage] = useState(0)

  const debouncedSearch = useDebounce(search, 350)

  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Campaign | null>(null)
  const [deleting, setDeleting] = useState<Campaign | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  const params: CampaignListParams = {
    search: debouncedSearch || undefined,
    status: status || undefined,
    platformId: platformId ? Number(platformId) : undefined,
    objective: objective || undefined,
    page,
    size: 10,
    sort: 'createdAt,desc',
  }

  const load = () => {
    setLoading(true)
    setError(null)
    campaignsApi
      .list(params)
      .then((res) => {
        setCampaigns(res.content)
        setTotalPages(res.totalPages)
        setTotalElements(res.totalElements)
      })
      .catch((err) => setError(extractErrorMessage(err, 'No pudimos cargar las campañas.')))
      .finally(() => setLoading(false))
  }

  useEffect(load, [debouncedSearch, status, platformId, objective, page])

  useEffect(() => setPage(0), [debouncedSearch, status, platformId, objective])

  const handleCreate = async (payload: CampaignRequest) => {
    try {
      await campaignsApi.create(payload)
      showToast('Campaña creada correctamente')
      setFormOpen(false)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos crear la campaña.'), 'error')
    }
  }

  const handleUpdate = async (payload: CampaignRequest) => {
    if (!editing) return
    try {
      await campaignsApi.update(editing.id, payload)
      showToast('Campaña actualizada')
      setEditing(null)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos actualizar la campaña.'), 'error')
    }
  }

  const handleAction = async (action: (id: number) => Promise<Campaign>, campaign: Campaign, successMessage: string) => {
    try {
      await action(campaign.id)
      showToast(successMessage)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteLoading(true)
    try {
      await campaignsApi.remove(deleting.id)
      showToast('Campaña eliminada')
      setDeleting(null)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    } finally {
      setDeleteLoading(false)
    }
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <div className="relative">
            <Search className="pointer-events-none absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-ink-400" />
            <Input
              placeholder="Buscar campañas..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-56 pl-8"
            />
          </div>
          <Select value={status} onChange={(e) => setStatus(e.target.value)} className="w-40">
            <option value="">Todos los estados</option>
            {Object.entries(campaignStatusLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
          <Select value={platformId} onChange={(e) => setPlatformId(e.target.value)} className="w-40">
            <option value="">Todas las plataformas</option>
            {platforms.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </Select>
          <Select value={objective} onChange={(e) => setObjective(e.target.value)} className="w-40">
            <option value="">Todos los objetivos</option>
            {Object.entries(campaignObjectiveLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </div>
        <Button onClick={() => setFormOpen(true)}>
          <Plus className="size-4" /> Nueva campaña
        </Button>
      </div>

      {loading && (
        <div className="rounded-xl border border-ink-200 bg-white p-5">
          <SkeletonRows rows={6} />
        </div>
      )}

      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {!loading && !error && campaigns.length === 0 && (
        <EmptyState
          icon={Megaphone}
          title="No se encontraron campañas"
          description="Creá tu primera campaña o ajustá los filtros de búsqueda."
          action={
            <Button onClick={() => setFormOpen(true)}>
              <Plus className="size-4" /> Nueva campaña
            </Button>
          }
        />
      )}

      {!loading && !error && campaigns.length > 0 && (
        <>
          <Table>
            <Thead>
              <Tr>
                <Th>Campaña</Th>
                <Th>Plataforma</Th>
                <Th>Objetivo</Th>
                <Th>Estado</Th>
                <Th>Presupuesto</Th>
                <Th>ROAS</Th>
                <Th>Inicio</Th>
                <Th />
              </Tr>
            </Thead>
            <Tbody>
              {campaigns.map((campaign) => (
                <Tr key={campaign.id} className="cursor-pointer" onClick={() => navigate(`/campaigns/${campaign.id}`)}>
                  <Td className="font-medium text-ink-900">{campaign.name}</Td>
                  <Td>
                    <PlatformTag platform={campaign.platform} />
                  </Td>
                  <Td>{campaignObjectiveLabels[campaign.objective]}</Td>
                  <Td>
                    <CampaignStatusBadge status={campaign.status} />
                  </Td>
                  <Td>{formatCurrency(campaign.budget)}</Td>
                  <Td className="font-medium">{formatMultiplier(campaign.metrics.roas)}</Td>
                  <Td>{formatDate(campaign.startDate)}</Td>
                  <Td onClick={(e) => e.stopPropagation()}>
                    <Dropdown
                      items={[
                        { label: 'Editar', icon: <Pencil className="size-3.5" />, onClick: () => setEditing(campaign) },
                        ...(campaign.status === 'DRAFT' || campaign.status === 'PAUSED'
                          ? [{ label: 'Activar', icon: <Play className="size-3.5" />, onClick: () => handleAction(campaignsApi.activate, campaign, 'Campaña activada') }]
                          : []),
                        ...(campaign.status === 'ACTIVE'
                          ? [{ label: 'Pausar', icon: <Pause className="size-3.5" />, onClick: () => handleAction(campaignsApi.pause, campaign, 'Campaña pausada') }]
                          : []),
                        ...(campaign.status === 'ACTIVE' || campaign.status === 'PAUSED'
                          ? [{ label: 'Finalizar', icon: <CheckCircle className="size-3.5" />, onClick: () => handleAction(campaignsApi.complete, campaign, 'Campaña finalizada') }]
                          : []),
                        ...(campaign.status !== 'COMPLETED' && campaign.status !== 'CANCELLED'
                          ? [{ label: 'Cancelar', icon: <XCircle className="size-3.5" />, onClick: () => handleAction(campaignsApi.cancel, campaign, 'Campaña cancelada') }]
                          : []),
                        { label: 'Eliminar', icon: <Trash2 className="size-3.5" />, danger: true, onClick: () => setDeleting(campaign) },
                      ]}
                    />
                  </Td>
                </Tr>
              ))}
            </Tbody>
          </Table>
          <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
        </>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Nueva campaña" size="lg">
        <CampaignForm onSubmit={handleCreate} onCancel={() => setFormOpen(false)} submitLabel="Crear campaña" />
      </Modal>

      <Modal open={!!editing} onClose={() => setEditing(null)} title="Editar campaña" size="lg">
        {editing && <CampaignForm initialValue={editing} onSubmit={handleUpdate} onCancel={() => setEditing(null)} />}
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        title={`¿Eliminar "${deleting?.name}"?`}
        confirmLabel="Eliminar"
        danger
        loading={deleteLoading}
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </div>
  )
}
