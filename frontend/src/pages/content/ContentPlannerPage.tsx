import { useEffect, useState } from 'react'
import { Plus, CalendarDays, Trash2, Pencil } from 'lucide-react'
import { contentApi } from '@/api/content'
import type { ContentListParams } from '@/api/content'
import { usePlatforms } from '@/hooks/usePlatforms'
import { useToast } from '@/context/ToastContext'
import type { Content, ContentRequest } from '@/types'
import { Button } from '@/components/ui/Button'
import { Select } from '@/components/ui/Select'
import { Modal } from '@/components/ui/Modal'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { Card } from '@/components/ui/Card'
import { Pagination } from '@/components/ui/Pagination'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { SkeletonCard } from '@/components/ui/Skeleton'
import { PlatformTag } from '@/components/domain/PlatformTag'
import { ContentStatusBadge } from '@/components/domain/StatusBadges'
import { ContentForm } from '@/components/domain/ContentForm'
import { contentStatusLabels } from '@/utils/labels'
import { formatDate } from '@/utils/format'
import { extractErrorMessage } from '@/api/client'

export function ContentPlannerPage() {
  const { showToast } = useToast()
  const { platforms } = usePlatforms()
  const [status, setStatus] = useState('')
  const [platformId, setPlatformId] = useState('')
  const [page, setPage] = useState(0)

  const [items, setItems] = useState<Content[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Content | null>(null)
  const [deleting, setDeleting] = useState<Content | null>(null)

  const params: ContentListParams = {
    status: status || undefined,
    platformId: platformId ? Number(platformId) : undefined,
    page,
    size: 12,
    sort: 'scheduledDate,asc',
  }

  const load = () => {
    setLoading(true)
    setError(null)
    contentApi
      .list(params)
      .then((res) => {
        setItems(res.content)
        setTotalPages(res.totalPages)
        setTotalElements(res.totalElements)
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(load, [status, platformId, page])
  useEffect(() => setPage(0), [status, platformId])

  const handleCreate = async (payload: ContentRequest) => {
    try {
      await contentApi.create(payload)
      showToast('Contenido creado')
      setFormOpen(false)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleUpdate = async (payload: ContentRequest) => {
    if (!editing) return
    try {
      await contentApi.update(editing.id, payload)
      showToast('Contenido actualizado')
      setEditing(null)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleDelete = async () => {
    if (!deleting) return
    try {
      await contentApi.remove(deleting.id)
      showToast('Contenido eliminado')
      setDeleting(null)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <Select value={status} onChange={(e) => setStatus(e.target.value)} className="w-40">
            <option value="">Todos los estados</option>
            {Object.entries(contentStatusLabels).map(([value, label]) => (
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
        </div>
        <Button onClick={() => setFormOpen(true)}>
          <Plus className="size-4" /> Nuevo contenido
        </Button>
      </div>

      {loading && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      )}

      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {!loading && !error && items.length === 0 && (
        <EmptyState
          icon={CalendarDays}
          title="Sin contenido planificado"
          description="Creá tu primera publicación para el calendario de contenido."
          action={
            <Button onClick={() => setFormOpen(true)}>
              <Plus className="size-4" /> Nuevo contenido
            </Button>
          }
        />
      )}

      {!loading && !error && items.length > 0 && (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {items.map((item) => (
              <Card key={item.id} className="flex flex-col gap-2.5">
                <div className="flex items-start justify-between gap-2">
                  <PlatformTag platform={item.platform} />
                  <div className="flex items-center gap-1">
                    <button onClick={() => setEditing(item)} className="rounded-md p-1 text-ink-400 hover:bg-ink-100 hover:text-ink-700">
                      <Pencil className="size-3.5" />
                    </button>
                    <button onClick={() => setDeleting(item)} className="rounded-md p-1 text-ink-400 hover:bg-ink-100 hover:text-danger-600">
                      <Trash2 className="size-3.5" />
                    </button>
                  </div>
                </div>
                <h3 className="text-sm font-semibold text-ink-900">{item.title}</h3>
                {item.copyText && <p className="line-clamp-2 text-xs text-ink-500">{item.copyText}</p>}
                <div className="mt-1 flex items-center justify-between">
                  <ContentStatusBadge status={item.status} />
                  <span className="text-xs text-ink-400">
                    {item.scheduledDate ? formatDate(item.scheduledDate) : 'Sin fecha'}
                  </span>
                </div>
              </Card>
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
        </>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Nuevo contenido" size="lg">
        <ContentForm onSubmit={handleCreate} onCancel={() => setFormOpen(false)} />
      </Modal>

      <Modal open={!!editing} onClose={() => setEditing(null)} title="Editar contenido" size="lg">
        {editing && <ContentForm initialValue={editing} onSubmit={handleUpdate} onCancel={() => setEditing(null)} />}
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        title={`¿Eliminar "${deleting?.title}"?`}
        confirmLabel="Eliminar"
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </div>
  )
}
