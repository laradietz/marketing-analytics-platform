import { useEffect, useRef, useState } from 'react'
import type { ChangeEvent } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import {
  MessageSquareText,
  Plus,
  Search,
  Upload,
  Download,
  Trash2,
  Sparkles,
  Copy,
  RotateCcw,
  ThumbsUp,
  ThumbsDown,
  Minus,
  Quote,
} from 'lucide-react'
import { commentsApi } from '@/api/comments'
import { contentIdeasApi } from '@/api/contentIdeas'
import { useDebounce } from '@/hooks/useDebounce'
import { usePlatforms } from '@/hooks/usePlatforms'
import { useToast } from '@/context/ToastContext'
import { extractErrorMessage } from '@/api/client'
import type {
  AudienceComment,
  CampaignObjective,
  CommentPlatformSummary,
  CommentRequest,
  CommentSentiment,
  ContentIdea,
  ContentTone,
} from '@/types'
import { Button } from '@/components/ui/Button'
import { Card, CardHeader } from '@/components/ui/Card'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Field } from '@/components/ui/Field'
import { Modal } from '@/components/ui/Modal'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { Table, Thead, Tbody, Tr, Th, Td } from '@/components/ui/Table'
import { Pagination } from '@/components/ui/Pagination'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { SkeletonRows, SkeletonCard } from '@/components/ui/Skeleton'
import { Badge } from '@/components/ui/Badge'
import { CommentForm } from '@/components/domain/CommentForm'
import { commentSentimentLabels, commentSentimentTone, contentToneLabels, campaignObjectiveLabels } from '@/utils/labels'
import { formatDate } from '@/utils/format'

export function CommentsPage() {
  const { showToast } = useToast()
  const { platforms } = usePlatforms()

  const [search, setSearch] = useState('')
  const [platformId, setPlatformId] = useState('')
  const [sentiment, setSentiment] = useState('')
  const [page, setPage] = useState(0)
  const debouncedSearch = useDebounce(search, 350)

  const [comments, setComments] = useState<AudienceComment[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [formOpen, setFormOpen] = useState(false)
  const [deleting, setDeleting] = useState<AudienceComment | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const [importing, setImporting] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [insightsRefreshKey, setInsightsRefreshKey] = useState(0)
  const refreshInsights = () => setInsightsRefreshKey((k) => k + 1)

  const load = () => {
    setLoading(true)
    setError(null)
    commentsApi
      .list({
        search: debouncedSearch || undefined,
        platformId: platformId ? Number(platformId) : undefined,
        sentiment: (sentiment as CommentSentiment) || undefined,
        page,
        size: 10,
      })
      .then((res) => {
        setComments(res.content)
        setTotalPages(res.totalPages)
        setTotalElements(res.totalElements)
      })
      .catch((err) => setError(extractErrorMessage(err, 'No pudimos cargar los comentarios.')))
      .finally(() => setLoading(false))
  }

  useEffect(load, [debouncedSearch, platformId, sentiment, page])
  useEffect(() => setPage(0), [debouncedSearch, platformId, sentiment])

  const handleCreate = async (payload: CommentRequest) => {
    try {
      await commentsApi.create(payload)
      showToast('Comentario registrado')
      setFormOpen(false)
      load()
      refreshInsights()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteLoading(true)
    try {
      await commentsApi.remove(deleting.id)
      showToast('Comentario eliminado')
      setDeleting(null)
      load()
      refreshInsights()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    } finally {
      setDeleteLoading(false)
    }
  }

  const handleFileSelected = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return

    setImporting(true)
    try {
      const result = await commentsApi.importCsv(file)
      if (result.imported > 0) {
        showToast(`Se importaron ${result.imported} de ${result.totalRows} comentarios`)
        load()
        refreshInsights()
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
      const blob = await commentsApi.downloadTemplate()
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'plantilla-comentarios.csv'
      link.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  return (
    <div className="grid grid-cols-1 gap-5 lg:grid-cols-3">
      <div className="flex flex-col gap-4 lg:col-span-2">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2">
            <div className="relative">
              <Search className="pointer-events-none absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-ink-400" />
              <Input placeholder="Buscar en comentarios..." value={search} onChange={(e) => setSearch(e.target.value)} className="w-56 pl-8" />
            </div>
            <Select value={platformId} onChange={(e) => setPlatformId(e.target.value)} className="w-40">
              <option value="">Todas las plataformas</option>
              {platforms.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </Select>
            <Select value={sentiment} onChange={(e) => setSentiment(e.target.value)} className="w-36">
              <option value="">Todo el sentimiento</option>
              {Object.entries(commentSentimentLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button variant="ghost" size="sm" onClick={handleDownloadTemplate}>
              <Download className="size-4" /> Plantilla CSV
            </Button>
            <input ref={fileInputRef} type="file" accept=".csv" className="hidden" onChange={handleFileSelected} />
            <Button variant="outline" size="sm" loading={importing} onClick={() => fileInputRef.current?.click()}>
              <Upload className="size-4" /> Importar CSV
            </Button>
            <Button size="sm" onClick={() => setFormOpen(true)}>
              <Plus className="size-4" /> Nuevo comentario
            </Button>
          </div>
        </div>

        {loading && (
          <div className="rounded-xl border border-ink-200 bg-white p-5">
            <SkeletonRows rows={6} />
          </div>
        )}

        {!loading && error && <ErrorState message={error} onRetry={load} />}

        {!loading && !error && comments.length === 0 && (
          <EmptyState
            icon={MessageSquareText}
            title="No hay comentarios cargados"
            description="Registrá un comentario manualmente o importá un CSV para empezar a escuchar a tu audiencia."
            action={
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="size-4" /> Nuevo comentario
              </Button>
            }
          />
        )}

        {!loading && !error && comments.length > 0 && (
          <>
            <Table>
              <Thead>
                <Tr>
                  <Th>Plataforma</Th>
                  <Th>Autor</Th>
                  <Th>Comentario</Th>
                  <Th>Sentimiento</Th>
                  <Th>Fecha</Th>
                  <Th />
                </Tr>
              </Thead>
              <Tbody>
                {comments.map((c) => (
                  <Tr key={c.id}>
                    <Td>{c.platformName}</Td>
                    <Td>{c.authorName || '—'}</Td>
                    <Td className="max-w-sm">
                      <p className="line-clamp-2 text-sm text-ink-700">{c.text}</p>
                    </Td>
                    <Td>
                      <Badge tone={commentSentimentTone[c.sentiment]} dot>
                        {commentSentimentLabels[c.sentiment]}
                      </Badge>
                    </Td>
                    <Td>{formatDate(c.postedAt)}</Td>
                    <Td>
                      <button
                        title="Eliminar"
                        onClick={() => setDeleting(c)}
                        className="rounded-md p-1.5 text-ink-400 hover:bg-ink-100 hover:text-danger-600"
                      >
                        <Trash2 className="size-4" />
                      </button>
                    </Td>
                  </Tr>
                ))}
              </Tbody>
            </Table>
            <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
          </>
        )}
      </div>

      <SocialListeningPanel refreshSignal={insightsRefreshKey} />

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Nuevo comentario" size="lg">
        <CommentForm defaultPlatformId={platformId ? Number(platformId) : undefined} onSubmit={handleCreate} onCancel={() => setFormOpen(false)} />
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        title="¿Eliminar este comentario?"
        confirmLabel="Eliminar"
        danger
        loading={deleteLoading}
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </div>
  )
}

const ideasSchema = z.object({
  productOrService: z.string().min(1, 'Contanos qué producto o servicio querés promocionar'),
  objective: z.string().min(1, 'Elegí un objetivo'),
  tone: z.string().min(1, 'Elegí un tono'),
})

type IdeasFormValues = z.infer<typeof ideasSchema>

function SocialListeningPanel({ refreshSignal }: { refreshSignal: number }) {
  const { showToast } = useToast()
  const { platforms } = usePlatforms()
  const [platformId, setPlatformId] = useState('')
  const [summary, setSummary] = useState<CommentPlatformSummary | null>(null)
  const [loadingSummary, setLoadingSummary] = useState(false)
  const [generating, setGenerating] = useState(false)
  const [ideas, setIdeas] = useState<ContentIdea[]>([])

  useEffect(() => {
    if (platforms.length > 0 && !platformId) {
      setPlatformId(String(platforms[0].id))
    }
  }, [platforms, platformId])

  const loadSummary = () => {
    if (!platformId) return
    setLoadingSummary(true)
    setIdeas([])
    commentsApi
      .summary(Number(platformId))
      .then(setSummary)
      .catch(() => setSummary(null))
      .finally(() => setLoadingSummary(false))
  }

  useEffect(loadSummary, [platformId, refreshSignal])

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<IdeasFormValues>({
    resolver: zodResolver(ideasSchema),
    defaultValues: { objective: 'ENGAGEMENT', tone: 'CASUAL' },
  })

  const generate = async (values: IdeasFormValues) => {
    if (!platformId) return
    setGenerating(true)
    try {
      const variants = await commentsApi.generateIdeas({
        platformId: Number(platformId),
        productOrService: values.productOrService,
        objective: values.objective as CampaignObjective,
        tone: values.tone as ContentTone,
        variantCount: 3,
      })
      setIdeas(variants)
      showToast(`Se generaron ${variants.length} ideas a partir de los comentarios`)
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos generar ideas.'), 'error')
    } finally {
      setGenerating(false)
    }
  }

  const copyToClipboard = async (idea: ContentIdea) => {
    const text = [idea.generatedTitle, '', idea.generatedCopy, '', idea.generatedCta, idea.generatedHashtags]
      .filter(Boolean)
      .join('\n')
    try {
      await navigator.clipboard.writeText(text)
      showToast('Copiado al portapapeles')
    } catch {
      showToast('No pudimos copiar el texto', 'error')
    }
  }

  const discard = async (idea: ContentIdea) => {
    try {
      await contentIdeasApi.remove(idea.id)
      setIdeas((prev) => prev.filter((i) => i.id !== idea.id))
      showToast('Idea descartada')
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <Card>
        <CardHeader title="Escucha social" description="Qué dice tu audiencia en cada plataforma" />
        <Field label="Plataforma" htmlFor="insights-platform">
          <Select
            id="insights-platform"
            value={platformId}
            onChange={(e) => {
              setPlatformId(e.target.value)
              reset()
            }}
          >
            {platforms.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </Select>
        </Field>

        {loadingSummary && <SkeletonRows rows={4} className="mt-4" />}

        {!loadingSummary && summary && summary.total === 0 && (
          <p className="mt-4 text-sm text-ink-500">Todavía no hay comentarios cargados para esta plataforma.</p>
        )}

        {!loadingSummary && summary && summary.total > 0 && (
          <>
            <div className="mt-4 grid grid-cols-3 gap-2">
              <div className="flex flex-col items-center rounded-lg bg-success-50 py-2">
                <ThumbsUp className="size-4 text-success-600" />
                <span className="mt-1 text-sm font-semibold text-success-700">{summary.positive}</span>
              </div>
              <div className="flex flex-col items-center rounded-lg bg-ink-100 py-2">
                <Minus className="size-4 text-ink-500" />
                <span className="mt-1 text-sm font-semibold text-ink-700">{summary.neutral}</span>
              </div>
              <div className="flex flex-col items-center rounded-lg bg-danger-50 py-2">
                <ThumbsDown className="size-4 text-danger-600" />
                <span className="mt-1 text-sm font-semibold text-danger-700">{summary.negative}</span>
              </div>
            </div>

            {summary.topThemes.length > 0 && (
              <div className="mt-4">
                <h4 className="mb-2 text-xs font-semibold uppercase tracking-wide text-ink-400">Temas recurrentes</h4>
                <ul className="flex flex-col gap-2.5">
                  {summary.topThemes.map((theme) => (
                    <li key={theme.keyword} className="rounded-lg border border-ink-100 p-2.5">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium text-ink-800">{theme.keyword}</span>
                        <span className="text-xs text-ink-400">{theme.mentions} menciones</span>
                      </div>
                      <p className="mt-1 flex items-start gap-1 text-xs italic text-ink-500">
                        <Quote className="mt-0.5 size-3 shrink-0" aria-hidden />
                        {theme.sampleQuote}
                      </p>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </>
        )}
      </Card>

      <Card>
        <CardHeader title="Generar ideas desde comentarios" description="Convertí lo que dice tu audiencia en contenido nuevo" />
        <form onSubmit={handleSubmit(generate)} className="flex flex-col gap-3">
          <Field label="Producto o servicio" htmlFor="ideas-product" error={errors.productOrService?.message} required>
            <Input id="ideas-product" placeholder="Ej: zapatillas running" invalid={!!errors.productOrService} {...register('productOrService')} />
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field label="Objetivo" htmlFor="ideas-objective">
              <Select id="ideas-objective" {...register('objective')}>
                {Object.entries(campaignObjectiveLabels).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Select>
            </Field>
            <Field label="Tono" htmlFor="ideas-tone">
              <Select id="ideas-tone" {...register('tone')}>
                {Object.entries(contentToneLabels).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Select>
            </Field>
          </div>
          <Button type="submit" loading={generating} disabled={!summary || summary.total === 0}>
            <Sparkles className="size-4" /> Generar ideas
          </Button>
        </form>
      </Card>

      {generating && (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      )}

      {!generating && ideas.length > 0 && (
        <div className="flex flex-col gap-3">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-ink-800">Ideas generadas</h3>
            <Button variant="outline" size="sm" onClick={handleSubmit(generate)}>
              <RotateCcw className="size-4" /> Regenerar
            </Button>
          </div>
          {ideas.map((idea) => (
            <Card key={idea.id} className="flex flex-col gap-2">
              <div className="flex items-start justify-between gap-2">
                <span className="rounded-md bg-brand-50 px-2 py-0.5 text-xs font-medium text-brand-700">
                  {contentToneLabels[idea.tone]}
                </span>
                <div className="flex gap-1">
                  <button onClick={() => copyToClipboard(idea)} title="Copiar" className="rounded-md p-1 text-ink-400 hover:bg-ink-100 hover:text-ink-700">
                    <Copy className="size-3.5" />
                  </button>
                  <button onClick={() => discard(idea)} title="Descartar" className="rounded-md p-1 text-ink-400 hover:bg-ink-100 hover:text-danger-600">
                    <Trash2 className="size-3.5" />
                  </button>
                </div>
              </div>
              <h4 className="text-sm font-semibold text-ink-900">{idea.generatedTitle}</h4>
              <p className="line-clamp-4 text-xs text-ink-600">{idea.generatedCopy}</p>
              {idea.generatedHashtags && <p className="text-xs text-brand-600">{idea.generatedHashtags}</p>}
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
