import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Textarea } from '@/components/ui/Textarea'
import { Button } from '@/components/ui/Button'
import { usePlatforms } from '@/hooks/usePlatforms'
import type { CommentRequest } from '@/types'

const schema = z.object({
  platformId: z.coerce.number().positive('Elegí una plataforma'),
  authorName: z.string().optional(),
  text: z.string().min(1, 'El comentario no puede estar vacío'),
  postedAt: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

interface CommentFormProps {
  defaultPlatformId?: number
  onSubmit: (payload: CommentRequest) => Promise<void>
  onCancel: () => void
}

export function CommentForm({ defaultPlatformId, onSubmit, onCancel }: CommentFormProps) {
  const { platforms } = usePlatforms()

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { platformId: defaultPlatformId ?? undefined },
  })

  const submit = async (values: FormValues) => {
    await onSubmit({
      platformId: values.platformId,
      authorName: values.authorName || undefined,
      text: values.text,
      postedAt: values.postedAt || undefined,
    })
  }

  return (
    <form onSubmit={handleSubmit(submit)} className="flex flex-col gap-4">
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
        <Field label="Autor (opcional)" htmlFor="authorName">
          <Input id="authorName" placeholder="Ej: usuario123" {...register('authorName')} />
        </Field>
      </div>
      <Field label="Comentario" htmlFor="text" error={errors.text?.message} required>
        <Textarea id="text" rows={3} placeholder="Pegá el comentario tal como lo dejó la persona" {...register('text')} />
      </Field>
      <Field label="Fecha (opcional)" htmlFor="postedAt" hint="Si la dejás vacía, se usa la fecha de hoy">
        <Input id="postedAt" type="date" {...register('postedAt')} />
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
