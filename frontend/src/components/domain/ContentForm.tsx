import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useEffect, useState } from 'react'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Textarea } from '@/components/ui/Textarea'
import { Button } from '@/components/ui/Button'
import { usePlatforms } from '@/hooks/usePlatforms'
import { campaignObjectiveLabels, contentStatusLabels } from '@/utils/labels'
import { campaignsApi } from '@/api/campaigns'
import type { Campaign, Content, ContentRequest } from '@/types'

const schema = z.object({
  title: z.string().min(1, 'El título es obligatorio'),
  copyText: z.string().optional(),
  platformId: z.coerce.number().positive('Elegí una plataforma'),
  objective: z.string().optional(),
  status: z.string().min(1),
  scheduledDate: z.string().optional(),
  hashtags: z.string().optional(),
  ctaText: z.string().optional(),
  campaignId: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

interface ContentFormProps {
  initialValue?: Content
  onSubmit: (payload: ContentRequest) => Promise<void>
  onCancel: () => void
}

export function ContentForm({ initialValue, onSubmit, onCancel }: ContentFormProps) {
  const { platforms } = usePlatforms()
  const [campaigns, setCampaigns] = useState<Campaign[]>([])

  useEffect(() => {
    campaignsApi.list({ size: 100 }).then((res) => setCampaigns(res.content))
  }, [])

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      title: initialValue?.title ?? '',
      copyText: initialValue?.copyText ?? '',
      platformId: initialValue?.platform.id,
      objective: initialValue?.objective ?? '',
      status: initialValue?.status ?? 'IDEA',
      scheduledDate: initialValue?.scheduledDate ?? '',
      hashtags: initialValue?.hashtags ?? '',
      ctaText: initialValue?.ctaText ?? '',
      campaignId: initialValue?.campaignId ? String(initialValue.campaignId) : '',
    },
  })

  const submit = async (values: FormValues) => {
    await onSubmit({
      title: values.title,
      copyText: values.copyText || undefined,
      platformId: values.platformId,
      objective: (values.objective || undefined) as ContentRequest['objective'],
      status: values.status as ContentRequest['status'],
      scheduledDate: values.scheduledDate || null,
      hashtags: values.hashtags || undefined,
      ctaText: values.ctaText || undefined,
      campaignId: values.campaignId ? Number(values.campaignId) : null,
    })
  }

  return (
    <form onSubmit={handleSubmit(submit)} className="flex flex-col gap-4">
      <Field label="Título" htmlFor="title" error={errors.title?.message} required>
        <Input id="title" invalid={!!errors.title} {...register('title')} />
      </Field>
      <Field label="Copy" htmlFor="copyText">
        <Textarea id="copyText" rows={3} {...register('copyText')} />
      </Field>
      <div className="grid grid-cols-2 gap-4">
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
        <Field label="Estado" htmlFor="status" required>
          <Select id="status" {...register('status')}>
            {Object.entries(contentStatusLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Objetivo" htmlFor="objective">
          <Select id="objective" {...register('objective')}>
            <option value="">Sin especificar</option>
            {Object.entries(campaignObjectiveLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Fecha programada" htmlFor="scheduledDate">
          <Input id="scheduledDate" type="date" {...register('scheduledDate')} />
        </Field>
        <Field label="Campaña asociada" htmlFor="campaignId">
          <Select id="campaignId" {...register('campaignId')}>
            <option value="">Ninguna</option>
            {campaigns.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Call to action" htmlFor="ctaText">
          <Input id="ctaText" {...register('ctaText')} />
        </Field>
      </div>
      <Field label="Hashtags" htmlFor="hashtags" hint="Separados por espacio, ej: #moda #verano">
        <Input id="hashtags" {...register('hashtags')} />
      </Field>
      <div className="flex justify-end gap-2 pt-2">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
          Cancelar
        </Button>
        <Button type="submit" loading={isSubmitting}>
          Guardar
        </Button>
      </div>
    </form>
  )
}
