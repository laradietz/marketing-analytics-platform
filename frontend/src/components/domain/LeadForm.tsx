import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useEffect, useState } from 'react'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Textarea } from '@/components/ui/Textarea'
import { Button } from '@/components/ui/Button'
import { leadSourceLabels } from '@/utils/labels'
import { campaignsApi } from '@/api/campaigns'
import type { Campaign, Lead, LeadRequest } from '@/types'

const schema = z.object({
  name: z.string().min(1, 'El nombre es obligatorio'),
  email: z.string().min(1, 'El email es obligatorio').email('El email no es válido'),
  phone: z.string().optional(),
  company: z.string().optional(),
  source: z.string().min(1, 'Elegí un origen'),
  campaignId: z.string().optional(),
  notes: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

interface LeadFormProps {
  initialValue?: Lead
  onSubmit: (payload: LeadRequest) => Promise<void>
  onCancel: () => void
}

export function LeadForm({ initialValue, onSubmit, onCancel }: LeadFormProps) {
  const [campaigns, setCampaigns] = useState<Campaign[]>([])

  useEffect(() => {
    campaignsApi.list({ size: 100 }).then((res) => setCampaigns(res.content))
  }, [])

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: initialValue?.name ?? '',
      email: initialValue?.email ?? '',
      phone: initialValue?.phone ?? '',
      company: initialValue?.company ?? '',
      source: initialValue?.source ?? 'ORGANIC',
      campaignId: initialValue?.campaignId ? String(initialValue.campaignId) : '',
      notes: initialValue?.notes ?? '',
    },
  })

  const submit = async (values: FormValues) => {
    await onSubmit({
      name: values.name,
      email: values.email,
      phone: values.phone || undefined,
      company: values.company || undefined,
      source: values.source as LeadRequest['source'],
      campaignId: values.campaignId ? Number(values.campaignId) : null,
      notes: values.notes || undefined,
    })
  }

  return (
    <form onSubmit={handleSubmit(submit)} className="flex flex-col gap-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Nombre" htmlFor="name" error={errors.name?.message} required>
          <Input id="name" invalid={!!errors.name} {...register('name')} />
        </Field>
        <Field label="Email" htmlFor="email" error={errors.email?.message} required>
          <Input id="email" type="email" invalid={!!errors.email} {...register('email')} />
        </Field>
        <Field label="Teléfono" htmlFor="phone">
          <Input id="phone" {...register('phone')} />
        </Field>
        <Field label="Empresa" htmlFor="company">
          <Input id="company" {...register('company')} />
        </Field>
        <Field label="Origen" htmlFor="source" error={errors.source?.message} required>
          <Select id="source" {...register('source')}>
            {Object.entries(leadSourceLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
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
      </div>
      <Field label="Notas" htmlFor="notes">
        <Textarea id="notes" rows={3} {...register('notes')} />
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
