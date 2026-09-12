import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Textarea } from '@/components/ui/Textarea'
import { Select } from '@/components/ui/Select'
import { Button } from '@/components/ui/Button'
import { adFormatLabels } from '@/utils/labels'
import type { AdRequest } from '@/types'

const schema = z.object({
  name: z.string().min(1, 'El nombre es obligatorio'),
  format: z.string().min(1),
  headline: z.string().optional(),
  body: z.string().optional(),
  ctaLabel: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

interface AdFormProps {
  onSubmit: (payload: AdRequest) => Promise<void>
  onCancel: () => void
}

export function AdForm({ onSubmit, onCancel }: AdFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { format: 'IMAGE' } })

  const submit = async (values: FormValues) => {
    await onSubmit({
      name: values.name,
      format: values.format as AdRequest['format'],
      headline: values.headline || undefined,
      body: values.body || undefined,
      ctaLabel: values.ctaLabel || undefined,
    })
  }

  return (
    <form onSubmit={handleSubmit(submit)} className="flex flex-col gap-4">
      <Field label="Nombre" htmlFor="name" error={errors.name?.message} required>
        <Input id="name" invalid={!!errors.name} {...register('name')} />
      </Field>
      <Field label="Formato" htmlFor="format" required>
        <Select id="format" {...register('format')}>
          {Object.entries(adFormatLabels).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
      </Field>
      <Field label="Título" htmlFor="headline">
        <Input id="headline" {...register('headline')} />
      </Field>
      <Field label="Texto" htmlFor="body">
        <Textarea id="body" rows={3} {...register('body')} />
      </Field>
      <Field label="Call to action" htmlFor="ctaLabel">
        <Input id="ctaLabel" placeholder="Ej: Comprar ahora" {...register('ctaLabel')} />
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
