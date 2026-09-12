import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import type { CampaignMetricRequest } from '@/types'

const schema = z.object({
  recordedDate: z.string().min(1, 'La fecha es obligatoria'),
  impressions: z.coerce.number().min(0),
  reach: z.coerce.number().min(0),
  clicks: z.coerce.number().min(0),
  conversions: z.coerce.number().min(0),
  spend: z.coerce.number().min(0),
  revenue: z.coerce.number().min(0),
  likes: z.coerce.number().min(0).optional(),
  comments: z.coerce.number().min(0).optional(),
  shares: z.coerce.number().min(0).optional(),
  saves: z.coerce.number().min(0).optional(),
})

type FormValues = z.infer<typeof schema>

interface MetricFormProps {
  onSubmit: (payload: CampaignMetricRequest) => Promise<void>
  onCancel: () => void
}

export function MetricForm({ onSubmit, onCancel }: MetricFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { recordedDate: new Date().toISOString().slice(0, 10) },
  })

  const submit = async (values: FormValues) => {
    await onSubmit({
      recordedDate: values.recordedDate,
      impressions: values.impressions,
      reach: values.reach,
      clicks: values.clicks,
      conversions: values.conversions,
      spend: values.spend,
      revenue: values.revenue,
      likes: values.likes ?? 0,
      comments: values.comments ?? 0,
      shares: values.shares ?? 0,
      saves: values.saves ?? 0,
    })
  }

  return (
    <form onSubmit={handleSubmit(submit)} className="flex flex-col gap-4">
      <Field label="Fecha" htmlFor="recordedDate" error={errors.recordedDate?.message} required>
        <Input id="recordedDate" type="date" invalid={!!errors.recordedDate} {...register('recordedDate')} />
      </Field>

      <div className="grid grid-cols-2 gap-4">
        <Field label="Impresiones" htmlFor="impressions" error={errors.impressions?.message} required>
          <Input id="impressions" type="number" min="0" invalid={!!errors.impressions} {...register('impressions')} />
        </Field>
        <Field label="Alcance" htmlFor="reach" error={errors.reach?.message} required>
          <Input id="reach" type="number" min="0" invalid={!!errors.reach} {...register('reach')} />
        </Field>
        <Field label="Clics" htmlFor="clicks" error={errors.clicks?.message} required>
          <Input id="clicks" type="number" min="0" invalid={!!errors.clicks} {...register('clicks')} />
        </Field>
        <Field label="Conversiones" htmlFor="conversions" error={errors.conversions?.message} required>
          <Input id="conversions" type="number" min="0" invalid={!!errors.conversions} {...register('conversions')} />
        </Field>
        <Field label="Gasto" htmlFor="spend" error={errors.spend?.message} required>
          <Input id="spend" type="number" step="0.01" min="0" invalid={!!errors.spend} {...register('spend')} />
        </Field>
        <Field label="Ingresos" htmlFor="revenue" error={errors.revenue?.message} required>
          <Input id="revenue" type="number" step="0.01" min="0" invalid={!!errors.revenue} {...register('revenue')} />
        </Field>
        <Field label="Likes" htmlFor="likes">
          <Input id="likes" type="number" min="0" {...register('likes')} />
        </Field>
        <Field label="Comentarios" htmlFor="comments">
          <Input id="comments" type="number" min="0" {...register('comments')} />
        </Field>
        <Field label="Compartidos" htmlFor="shares">
          <Input id="shares" type="number" min="0" {...register('shares')} />
        </Field>
        <Field label="Guardados" htmlFor="saves">
          <Input id="saves" type="number" min="0" {...register('saves')} />
        </Field>
      </div>

      <div className="flex justify-end gap-2 pt-2">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
          Cancelar
        </Button>
        <Button type="submit" loading={isSubmitting}>
          Guardar métricas
        </Button>
      </div>
    </form>
  )
}
