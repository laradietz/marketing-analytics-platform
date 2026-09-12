import { useEffect, useState } from 'react'
import { Plus, Search, Users, Trash2, Pencil, PhoneCall } from 'lucide-react'
import { leadsApi } from '@/api/leads'
import type { LeadListParams } from '@/api/leads'
import { useDebounce } from '@/hooks/useDebounce'
import { useToast } from '@/context/ToastContext'
import type { Lead, LeadRequest, LeadStatus } from '@/types'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Modal } from '@/components/ui/Modal'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { Table, Thead, Tbody, Tr, Th, Td } from '@/components/ui/Table'
import { Pagination } from '@/components/ui/Pagination'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { SkeletonRows } from '@/components/ui/Skeleton'
import { LeadTemperatureBadge } from '@/components/domain/StatusBadges'
import { LeadForm } from '@/components/domain/LeadForm'
import { leadSourceLabels, leadStatusLabels } from '@/utils/labels'
import { extractErrorMessage } from '@/api/client'

export function LeadsListPage() {
  const { showToast } = useToast()
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [source, setSource] = useState('')
  const [page, setPage] = useState(0)
  const debouncedSearch = useDebounce(search, 350)

  const [leads, setLeads] = useState<Lead[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Lead | null>(null)
  const [deleting, setDeleting] = useState<Lead | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  const params: LeadListParams = {
    search: debouncedSearch || undefined,
    status: status || undefined,
    source: source || undefined,
    page,
    size: 10,
    sort: 'createdAt,desc',
  }

  const load = () => {
    setLoading(true)
    setError(null)
    leadsApi
      .list(params)
      .then((res) => {
        setLeads(res.content)
        setTotalPages(res.totalPages)
        setTotalElements(res.totalElements)
      })
      .catch((err) => setError(extractErrorMessage(err, 'No pudimos cargar los leads.')))
      .finally(() => setLoading(false))
  }

  useEffect(load, [debouncedSearch, status, source, page])
  useEffect(() => setPage(0), [debouncedSearch, status, source])

  const handleCreate = async (payload: LeadRequest) => {
    try {
      await leadsApi.create(payload)
      showToast('Lead creado correctamente')
      setFormOpen(false)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleUpdate = async (payload: LeadRequest) => {
    if (!editing) return
    try {
      await leadsApi.update(editing.id, payload)
      showToast('Lead actualizado')
      setEditing(null)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleStatusChange = async (lead: Lead, newStatus: LeadStatus) => {
    try {
      await leadsApi.updateStatus(lead.id, newStatus)
      showToast('Estado actualizado')
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleContact = async (lead: Lead) => {
    try {
      await leadsApi.registerContact(lead.id)
      showToast('Contacto registrado, score actualizado')
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteLoading(true)
    try {
      await leadsApi.remove(deleting.id)
      showToast('Lead eliminado')
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
            <Input placeholder="Buscar leads..." value={search} onChange={(e) => setSearch(e.target.value)} className="w-56 pl-8" />
          </div>
          <Select value={status} onChange={(e) => setStatus(e.target.value)} className="w-40">
            <option value="">Todos los estados</option>
            {Object.entries(leadStatusLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
          <Select value={source} onChange={(e) => setSource(e.target.value)} className="w-40">
            <option value="">Todos los orígenes</option>
            {Object.entries(leadSourceLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </div>
        <Button onClick={() => setFormOpen(true)}>
          <Plus className="size-4" /> Nuevo lead
        </Button>
      </div>

      {loading && (
        <div className="rounded-xl border border-ink-200 bg-white p-5">
          <SkeletonRows rows={6} />
        </div>
      )}

      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {!loading && !error && leads.length === 0 && (
        <EmptyState
          icon={Users}
          title="No se encontraron leads"
          description="Registrá tu primer lead o ajustá los filtros."
          action={
            <Button onClick={() => setFormOpen(true)}>
              <Plus className="size-4" /> Nuevo lead
            </Button>
          }
        />
      )}

      {!loading && !error && leads.length > 0 && (
        <>
          <Table>
            <Thead>
              <Tr>
                <Th>Nombre</Th>
                <Th>Empresa</Th>
                <Th>Origen</Th>
                <Th>Estado</Th>
                <Th>Score</Th>
                <Th />
              </Tr>
            </Thead>
            <Tbody>
              {leads.map((lead) => (
                <Tr key={lead.id}>
                  <Td>
                    <p className="font-medium text-ink-900">{lead.name}</p>
                    <p className="text-xs text-ink-500">{lead.email}</p>
                  </Td>
                  <Td>{lead.company || '—'}</Td>
                  <Td>{leadSourceLabels[lead.source]}</Td>
                  <Td>
                    <Select
                      value={lead.status}
                      onChange={(e) => handleStatusChange(lead, e.target.value as LeadStatus)}
                      className="w-36"
                    >
                      {Object.entries(leadStatusLabels).map(([value, label]) => (
                        <option key={value} value={value}>
                          {label}
                        </option>
                      ))}
                    </Select>
                  </Td>
                  <Td>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-ink-800">{lead.score}</span>
                      <LeadTemperatureBadge temperature={lead.temperature} />
                    </div>
                  </Td>
                  <Td>
                    <div className="flex items-center gap-1">
                      <button
                        title="Registrar contacto"
                        onClick={() => handleContact(lead)}
                        className="rounded-md p-1.5 text-ink-400 hover:bg-ink-100 hover:text-brand-600"
                      >
                        <PhoneCall className="size-4" />
                      </button>
                      <button
                        title="Editar"
                        onClick={() => setEditing(lead)}
                        className="rounded-md p-1.5 text-ink-400 hover:bg-ink-100 hover:text-ink-700"
                      >
                        <Pencil className="size-4" />
                      </button>
                      <button
                        title="Eliminar"
                        onClick={() => setDeleting(lead)}
                        className="rounded-md p-1.5 text-ink-400 hover:bg-ink-100 hover:text-danger-600"
                      >
                        <Trash2 className="size-4" />
                      </button>
                    </div>
                  </Td>
                </Tr>
              ))}
            </Tbody>
          </Table>
          <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
        </>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Nuevo lead" size="lg">
        <LeadForm onSubmit={handleCreate} onCancel={() => setFormOpen(false)} />
      </Modal>

      <Modal open={!!editing} onClose={() => setEditing(null)} title="Editar lead" size="lg">
        {editing && <LeadForm initialValue={editing} onSubmit={handleUpdate} onCancel={() => setEditing(null)} />}
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        title={`¿Eliminar a "${deleting?.name}"?`}
        confirmLabel="Eliminar"
        danger
        loading={deleteLoading}
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </div>
  )
}
