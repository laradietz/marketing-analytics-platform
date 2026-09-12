import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useEffect } from 'react'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Textarea } from '@/components/ui/Textarea'
import { Button } from '@/components/ui/Button'
import { usePlatforms } from '@/hooks/usePlatforms'
import { campaignObjectiveLabels } from '@/utils/labels'
import type { Campaign, CampaignRequest } from '@/types'

const schema = z.object({
  name: z.string().min(1, 'El nombre es obligatorio').max(150),
  description: z.string().optional(),
  platformId: z.coerce.number({ message: 'Elegí una plataforma' }).positive('Elegí una plataforma'),
  objective: z.string().min(1, 'Elegí un objetivo'),
  budget: z.coerce.number({ message: 'Ingresá un presupuesto' }).min(0, 'El presupuesto no puede ser negativo'),
  startDate: z.string().min(1, 'La fecha de inicio es obligatoria'),
  endDate: z.string().optional(),
  targetAudience: z.string().optional(),
  notes: z.string().optional(),
  externalCampaignId: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

interface CampaignFormProps {
  initialValue?: Campaign
  onSubmit: (payload: CampaignRequest) => Promise<void>
  onCancel: () => void
  submitLabel?: string
}

export function CampaignForm({ initialValue, onSubmit, onCancel, submitLabel = 'Guardar' }: CampaignFormProps) {
  const { platforms } = usePlatforms()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: initialValue?.name ?? '',
      description: initialValue?.description ?? '',
      platformId: initialValue?.platform.id,
      objective: initialValue?.objective ?? 'AWARENESS',
      budget: initialValue?.budget ?? 0,
      startDate: initialValue?.startDate ?? new Date().toISOString().slice(0, 10),
      endDate: initialValue?.endDate ?? '',
      targetAudience: initialValue?.targetAudience ?? '',
      notes: initialValue?.notes ?? '',
      externalCampaignId: initialValue?.externalCampaignId ?? '',
    },
  })

  useEffect(() => {
    if (initialValue) {
      reset({
        name: initialValue.name,
        description: initialValue.description ?? '',
        platformId: initialValue.platform.id,
        objective: initialValue.objective,
        budget: initialValue.budget,
        startDate: initialValue.startDate,
        endDate: initialValue.endDate ?? '',
        targetAudience: initialValue.targetAudience ?? '',
        notes: initialValue.notes ?? '',
        externalCampaignId: initialValue.externalCampaignId ?? '',
      })
    }
  }, [initialValue, reset])

  const submit = async (values: FormValues) => {
    await onSubmit({
      name: values.name,
      description: values.description || undefined,
      platformId: values.platformId,
      objective: values.objective as CampaignRequest['objective'],
      budget: values.budget,
      startDate: values.startDate,
      endDate: values.endDate || null,
      targetAudience: values.targetAudience || undefined,
      notes: values.notes || undefined,
      externalCampaignId: values.externalCampaignId || undefined,
    })
  }

  return (
    <form onSubmit={handleSubmit(submit)} className="flex flex-col gap-4">
      <Field label="Nombre" htmlFor="name" error={errors.name?.message} required>
        <Input id="name" invalid={!!errors.name} {...register('name')} />
      </Field>

      <Field label="Descripción" htmlFor="description">
        <Textarea id="description" rows={2} {...register('description')} />
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
        <Field label="Objetivo" htmlFor="objective" error={errors.objective?.message} required>
          <Select id="objective" invalid={!!errors.objective} {...register('objective')}>
            {Object.entries(campaignObjectiveLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>
      </div>

      <div className="grid grid-cols-3 gap-4">
        <Field label="Presupuesto" htmlFor="budget" error={errors.budget?.message} required>
          <Input id="budget" type="number" step="0.01" min="0" invalid={!!errors.budget} {...register('budget')} />
        </Field>
        <Field label="Fecha de inicio" htmlFor="startDate" error={errors.startDate?.message} required>
          <Input id="startDate" type="date" invalid={!!errors.startDate} {...register('startDate')} />
        </Field>
        <Field label="Fecha de fin" htmlFor="endDate">
          <Input id="endDate" type="date" {...register('endDate')} />
        </Field>
      </div>

      <Field label="Público objetivo" htmlFor="targetAudience">
        <Textarea id="targetAudience" rows={2} placeholder="Ej: mujeres 25-40 años interesadas en moda" {...register('targetAudience')} />
      </Field>

      <Field label="Notas" htmlFor="notes">
        <Textarea id="notes" rows={2} {...register('notes')} />
      </Field>

      <Field
        label="ID de campaña externa (opcional)"
        htmlFor="externalCampaignId"
        hint="El ID de esta campaña en Meta/Google/TikTok Ads Manager, para sincronizar métricas automáticamente desde Configuración › Integraciones"
      >
        <Input id="externalCampaignId" {...register('externalCampaignId')} />
      </Field>

      <div className="flex justify-end gap-2 pt-2">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
          Cancelar
        </Button>
        <Button type="submit" loading={isSubmitting}>
          {submitLabel}
        </Button>
      </div>
    </form>
  )
}
