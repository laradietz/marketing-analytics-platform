import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Textarea } from '@/components/ui/Textarea'
import { Button } from '@/components/ui/Button'
import type { BudgetRequest } from '@/types'

const schema = z.object({
  periodStart: z.string().min(1, 'La fecha de inicio es obligatoria'),
  periodEnd: z.string().min(1, 'La fecha de fin es obligatoria'),
  plannedAmount: z.coerce.number().min(0, 'El monto no puede ser negativo'),
  notes: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

interface BudgetFormProps {
  onSubmit: (payload: BudgetRequest) => Promise<void>
  onCancel: () => void
}

export function BudgetForm({ onSubmit, onCancel }: BudgetFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  const submit = async (values: FormValues) => {
    await onSubmit({
      periodStart: values.periodStart,
      periodEnd: values.periodEnd,
      plannedAmount: values.plannedAmount,
      notes: values.notes || undefined,
    })
  }

  return (
    <form onSubmit={handleSubmit(submit)} className="flex flex-col gap-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Inicio del período" htmlFor="periodStart" error={errors.periodStart?.message} required>
          <Input id="periodStart" type="date" invalid={!!errors.periodStart} {...register('periodStart')} />
        </Field>
        <Field label="Fin del período" htmlFor="periodEnd" error={errors.periodEnd?.message} required>
          <Input id="periodEnd" type="date" invalid={!!errors.periodEnd} {...register('periodEnd')} />
        </Field>
      </div>
      <Field label="Monto planificado" htmlFor="plannedAmount" error={errors.plannedAmount?.message} required>
        <Input id="plannedAmount" type="number" step="0.01" min="0" invalid={!!errors.plannedAmount} {...register('plannedAmount')} />
      </Field>
      <Field label="Notas" htmlFor="notes">
        <Textarea id="notes" rows={2} {...register('notes')} />
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
