import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Sparkles, Copy, Save, RotateCcw, Trash2, Lightbulb } from 'lucide-react'
import { contentIdeasApi } from '@/api/contentIdeas'
import { usePlatforms } from '@/hooks/usePlatforms'
import { useToast } from '@/context/ToastContext'
import type { ContentGenerationRequest, ContentIdea } from '@/types'
import { Card, CardHeader } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { EmptyState } from '@/components/ui/EmptyState'
import { SkeletonCard } from '@/components/ui/Skeleton'
import { contentToneLabels, campaignObjectiveLabels } from '@/utils/labels'
import { extractErrorMessage } from '@/api/client'

const schema = z.object({
  productOrService: z.string().min(1, 'Contanos qué producto o servicio querés promocionar'),
  targetAudience: z.string().min(1, 'Describí tu público objetivo'),
  platformId: z.coerce.number().positive('Elegí una plataforma'),
  objective: z.string().min(1, 'Elegí un objetivo'),
  tone: z.string().min(1, 'Elegí un tono'),
  callToAction: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

export function ContentIdeasPage() {
  const { showToast } = useToast()
  const { platforms } = usePlatforms()
  const [generating, setGenerating] = useState(false)
  const [results, setResults] = useState<ContentIdea[]>([])
  const [saved, setSaved] = useState<ContentIdea[]>([])
  const [loadingSaved, setLoadingSaved] = useState(true)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { objective: 'ENGAGEMENT', tone: 'ENERGETIC' },
  })

  const loadSaved = () => {
    setLoadingSaved(true)
    contentIdeasApi
      .list(0, 12)
      .then((res) => setSaved(res.content))
      .finally(() => setLoadingSaved(false))
  }

  useEffect(loadSaved, [])

  const generate = async (values: FormValues) => {
    const payload: ContentGenerationRequest = {
      productOrService: values.productOrService,
      targetAudience: values.targetAudience,
      platformId: values.platformId,
      objective: values.objective as ContentGenerationRequest['objective'],
      tone: values.tone as ContentGenerationRequest['tone'],
      callToAction: values.callToAction || undefined,
      variantCount: 3,
    }
    setGenerating(true)
    try {
      const variants = await contentIdeasApi.generate(payload)
      setResults(variants)
      loadSaved()
      showToast(`Se generaron ${variants.length} variantes`)
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos generar contenido.'), 'error')
    } finally {
      setGenerating(false)
    }
  }

  const regenerate = () => {
    handleSubmit(generate)()
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
      setResults((prev) => prev.filter((r) => r.id !== idea.id))
      setSaved((prev) => prev.filter((s) => s.id !== idea.id))
      showToast('Idea descartada')
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <Card>
        <CardHeader
          title="Generador de contenido con IA"
          description="Completá los datos de tu producto y audiencia para generar variantes de copy listas para publicar"
        />
        <form onSubmit={handleSubmit(generate)} className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <Field label="Producto o servicio" htmlFor="productOrService" error={errors.productOrService?.message} required>
            <Input id="productOrService" placeholder="Ej: curso online de marketing digital" invalid={!!errors.productOrService} {...register('productOrService')} />
          </Field>
          <Field label="Público objetivo" htmlFor="targetAudience" error={errors.targetAudience?.message} required>
            <Input id="targetAudience" placeholder="Ej: emprendedores de 25 a 40 años" invalid={!!errors.targetAudience} {...register('targetAudience')} />
          </Field>
          <Field label="Plataforma" htmlFor="platformId" error={errors.platformId?.message} required>
            <Select id="platformId" invalid={!!errors.platformId} {...register('platformId')}>
              <option value="">Seleccionar...</option>
              {platforms.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Objetivo" htmlFor="objective" required>
            <Select id="objective" {...register('objective')}>
              {Object.entries(campaignObjectiveLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Tono" htmlFor="tone" required>
            <Select id="tone" {...register('tone')}>
              {Object.entries(contentToneLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Call to action (opcional)" htmlFor="callToAction">
            <Input id="callToAction" placeholder="Se genera uno automáticamente si lo dejás vacío" {...register('callToAction')} />
          </Field>
          <div className="md:col-span-2 flex justify-end gap-2">
            <Button type="submit" loading={generating}>
              <Sparkles className="size-4" /> Generar variantes
            </Button>
          </div>
        </form>
      </Card>

      {generating && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      )}

      {!generating && results.length > 0 && (
        <div>
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-ink-800">Variantes generadas</h3>
            <Button variant="outline" size="sm" onClick={regenerate}>
              <RotateCcw className="size-4" /> Regenerar
            </Button>
          </div>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            {results.map((idea) => (
              <IdeaCard key={idea.id} idea={idea} onCopy={() => copyToClipboard(idea)} onDiscard={() => discard(idea)} />
            ))}
          </div>
        </div>
      )}

      <div>
        <h3 className="mb-3 text-sm font-semibold text-ink-800">Ideas guardadas</h3>
        {loadingSaved ? (
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <SkeletonCard key={i} />
            ))}
          </div>
        ) : saved.length === 0 ? (
          <EmptyState icon={Lightbulb} title="Todavía no generaste ideas de contenido" description="Usá el generador de arriba para crear tu primera variante." />
        ) : (
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            {saved.map((idea) => (
              <IdeaCard key={idea.id} idea={idea} onCopy={() => copyToClipboard(idea)} onDiscard={() => discard(idea)} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function IdeaCard({ idea, onCopy, onDiscard }: { idea: ContentIdea; onCopy: () => void; onDiscard: () => void }) {
  return (
    <Card className="flex flex-col gap-2.5">
      <div className="flex items-start justify-between gap-2">
        <span className="rounded-md bg-brand-50 px-2 py-0.5 text-xs font-medium text-brand-700">
          {contentToneLabels[idea.tone]}
        </span>
        <div className="flex gap-1">
          <button onClick={onCopy} title="Copiar" className="rounded-md p-1 text-ink-400 hover:bg-ink-100 hover:text-ink-700">
            <Copy className="size-3.5" />
          </button>
          <button onClick={onDiscard} title="Descartar" className="rounded-md p-1 text-ink-400 hover:bg-ink-100 hover:text-danger-600">
            <Trash2 className="size-3.5" />
          </button>
        </div>
      </div>
      <h4 className="text-sm font-semibold text-ink-900">{idea.generatedTitle}</h4>
      <p className="line-clamp-4 text-xs text-ink-600">{idea.generatedCopy}</p>
      {idea.generatedCta && (
        <span className="inline-flex w-fit items-center gap-1 rounded-md bg-success-50 px-2 py-1 text-xs font-medium text-success-700">
          <Save className="size-3" /> {idea.generatedCta}
        </span>
      )}
      {idea.generatedHashtags && <p className="text-xs text-brand-600">{idea.generatedHashtags}</p>}
    </Card>
  )
}
