import { useEffect, useState } from 'react'
import { Lightbulb, RefreshCw, Check, X } from 'lucide-react'
import { recommendationsApi } from '@/api/recommendations'
import { useToast } from '@/context/ToastContext'
import type { Recommendation } from '@/types'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { SkeletonCard } from '@/components/ui/Skeleton'
import { Pagination } from '@/components/ui/Pagination'
import { RecommendationPriorityBadge } from '@/components/domain/StatusBadges'
import { recommendationTypeLabels } from '@/utils/labels'
import { extractErrorMessage } from '@/api/client'

export function RecommendationsPage() {
  const { showToast } = useToast()
  const [items, setItems] = useState<Recommendation[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [generating, setGenerating] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = () => {
    setLoading(true)
    setError(null)
    recommendationsApi
      .list(page, 9)
      .then((res) => {
        setItems(res.content)
        setTotalPages(res.totalPages)
        setTotalElements(res.totalElements)
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(load, [page])

  const handleGenerate = async () => {
    setGenerating(true)
    try {
      const generated = await recommendationsApi.generate()
      showToast(
        generated.length
          ? `Se generaron ${generated.length} recomendaciones a partir de tus métricas`
          : 'El análisis no encontró oportunidades nuevas con los datos actuales',
      )
      setPage(0)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    } finally {
      setGenerating(false)
    }
  }

  const updateStatus = async (rec: Recommendation, status: 'APPLIED' | 'DISMISSED') => {
    try {
      await recommendationsApi.updateStatus(rec.id, status)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <p className="max-w-xl text-sm text-ink-500">
          El optimizador analiza el rendimiento real de tus campañas y plataformas para sugerir dónde mover presupuesto,
          qué pausar o qué creatividades renovar.
        </p>
        <Button onClick={handleGenerate} loading={generating}>
          <RefreshCw className="size-4" /> Analizar campañas
        </Button>
      </div>

      {loading && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      )}

      {!loading && error && <ErrorState message={error} onRetry={load} />}

      {!loading && !error && items.length === 0 && (
        <EmptyState
          icon={Lightbulb}
          title="Sin recomendaciones todavía"
          description="Ejecutá el análisis para que el optimizador revise tus campañas activas."
          action={
            <Button onClick={handleGenerate} loading={generating}>
              <RefreshCw className="size-4" /> Analizar campañas
            </Button>
          }
        />
      )}

      {!loading && !error && items.length > 0 && (
        <>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            {items.map((rec) => (
              <Card key={rec.id} className="flex flex-col gap-3">
                <div className="flex items-start justify-between gap-2">
                  <span className="text-xs font-medium text-ink-500">{recommendationTypeLabels[rec.type]}</span>
                  <RecommendationPriorityBadge priority={rec.priority} />
                </div>
                <h3 className="text-sm font-semibold text-ink-900">{rec.title}</h3>
                <p className="flex-1 text-xs text-ink-600">{rec.description}</p>
                {rec.relatedMetricName && (
                  <p className="text-xs font-medium text-brand-600">
                    {rec.relatedMetricName}: {rec.relatedMetricValue}
                  </p>
                )}
                {rec.status === 'NEW' && (
                  <div className="flex gap-2 pt-1">
                    <Button size="sm" variant="outline" className="flex-1" onClick={() => updateStatus(rec, 'APPLIED')}>
                      <Check className="size-3.5" /> Aplicada
                    </Button>
                    <Button size="sm" variant="ghost" className="flex-1" onClick={() => updateStatus(rec, 'DISMISSED')}>
                      <X className="size-3.5" /> Descartar
                    </Button>
                  </div>
                )}
                {rec.status !== 'NEW' && (
                  <span className="text-xs font-medium text-ink-400">
                    {rec.status === 'APPLIED' ? 'Marcada como aplicada' : 'Descartada'}
                  </span>
                )}
              </Card>
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
