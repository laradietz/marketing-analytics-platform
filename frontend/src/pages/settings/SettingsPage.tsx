import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Target, Trash2 } from 'lucide-react'
import { usersApi } from '@/api/users'
import { goalsApi } from '@/api/goals'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'
import type { Goal, GoalRequest } from '@/types'
import { Card, CardHeader } from '@/components/ui/Card'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Button } from '@/components/ui/Button'
import { Modal } from '@/components/ui/Modal'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { EmptyState } from '@/components/ui/EmptyState'
import { goalMetricLabels } from '@/utils/labels'
import { formatDate, formatNumber } from '@/utils/format'
import { extractErrorMessage } from '@/api/client'

const profileSchema = z.object({
  name: z.string().min(1, 'El nombre es obligatorio'),
  jobTitle: z.string().optional(),
  companyName: z.string().optional(),
})
type ProfileValues = z.infer<typeof profileSchema>

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Ingresá tu contraseña actual'),
    newPassword: z.string().min(8, 'La nueva contraseña debe tener al menos 8 caracteres'),
    confirmPassword: z.string().min(1, 'Confirmá la nueva contraseña'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Las contraseñas no coinciden',
    path: ['confirmPassword'],
  })
type PasswordValues = z.infer<typeof passwordSchema>

const goalSchema = z.object({
  name: z.string().min(1, 'El nombre es obligatorio'),
  metricType: z.string().min(1),
  targetValue: z.coerce.number().min(0),
  periodStart: z.string().min(1),
  periodEnd: z.string().min(1),
})
type GoalValues = z.infer<typeof goalSchema>

export function SettingsPage() {
  const { user, refreshProfile } = useAuth()
  const { showToast } = useToast()

  const profileForm = useForm<ProfileValues>({
    resolver: zodResolver(profileSchema),
    values: { name: user?.name ?? '', jobTitle: user?.jobTitle ?? '', companyName: user?.companyName ?? '' },
  })

  const passwordForm = useForm<PasswordValues>({ resolver: zodResolver(passwordSchema) })

  const onProfileSubmit = async (values: ProfileValues) => {
    try {
      await usersApi.updateProfile(values)
      await refreshProfile()
      showToast('Perfil actualizado')
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const onPasswordSubmit = async (values: PasswordValues) => {
    try {
      await usersApi.changePassword(values)
      showToast('Contraseña actualizada')
      passwordForm.reset()
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos actualizar tu contraseña.'), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <Card className="max-w-2xl">
        <CardHeader title="Mi perfil" description="Información básica de tu cuenta" />
        <form onSubmit={profileForm.handleSubmit(onProfileSubmit)} className="flex flex-col gap-4">
          <Field label="Email" htmlFor="email">
            <Input id="email" value={user?.email ?? ''} disabled />
          </Field>
          <Field label="Nombre" htmlFor="name" error={profileForm.formState.errors.name?.message} required>
            <Input id="name" invalid={!!profileForm.formState.errors.name} {...profileForm.register('name')} />
          </Field>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Cargo" htmlFor="jobTitle">
              <Input id="jobTitle" {...profileForm.register('jobTitle')} />
            </Field>
            <Field label="Empresa" htmlFor="companyName">
              <Input id="companyName" {...profileForm.register('companyName')} />
            </Field>
          </div>
          <div className="flex justify-end">
            <Button type="submit" loading={profileForm.formState.isSubmitting}>
              Guardar cambios
            </Button>
          </div>
        </form>
      </Card>

      <Card className="max-w-2xl">
        <CardHeader title="Cambiar contraseña" />
        <form onSubmit={passwordForm.handleSubmit(onPasswordSubmit)} className="flex flex-col gap-4">
          <Field label="Contraseña actual" htmlFor="currentPassword" error={passwordForm.formState.errors.currentPassword?.message} required>
            <Input id="currentPassword" type="password" invalid={!!passwordForm.formState.errors.currentPassword} {...passwordForm.register('currentPassword')} />
          </Field>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Nueva contraseña" htmlFor="newPassword" error={passwordForm.formState.errors.newPassword?.message} required>
              <Input id="newPassword" type="password" invalid={!!passwordForm.formState.errors.newPassword} {...passwordForm.register('newPassword')} />
            </Field>
            <Field label="Confirmar contraseña" htmlFor="confirmPassword" error={passwordForm.formState.errors.confirmPassword?.message} required>
              <Input id="confirmPassword" type="password" invalid={!!passwordForm.formState.errors.confirmPassword} {...passwordForm.register('confirmPassword')} />
            </Field>
          </div>
          <div className="flex justify-end">
            <Button type="submit" loading={passwordForm.formState.isSubmitting}>
              Actualizar contraseña
            </Button>
          </div>
        </form>
      </Card>

      <GoalsSection />
    </div>
  )
}

function GoalsSection() {
  const { showToast } = useToast()
  const [goals, setGoals] = useState<Goal[]>([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [deleting, setDeleting] = useState<Goal | null>(null)

  const load = () => {
    setLoading(true)
    goalsApi.list().then(setGoals).finally(() => setLoading(false))
  }

  useEffect(load, [])

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(goalSchema), defaultValues: { metricType: 'REVENUE' } })

  const onSubmit = async (values: GoalValues) => {
    const payload: GoalRequest = {
      name: values.name,
      metricType: values.metricType as GoalRequest['metricType'],
      targetValue: values.targetValue,
      periodStart: values.periodStart,
      periodEnd: values.periodEnd,
    }
    try {
      await goalsApi.create(payload)
      showToast('Objetivo creado')
      setFormOpen(false)
      reset()
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  const handleDelete = async () => {
    if (!deleting) return
    try {
      await goalsApi.remove(deleting.id)
      showToast('Objetivo eliminado')
      setDeleting(null)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  return (
    <Card className="max-w-2xl">
      <CardHeader
        title="Objetivos"
        description="Metas de rendimiento para comparar contra los resultados reales"
        action={
          <Button size="sm" onClick={() => setFormOpen(true)}>
            <Plus className="size-4" /> Nuevo objetivo
          </Button>
        }
      />
      {loading ? (
        <p className="text-sm text-ink-500">Cargando...</p>
      ) : goals.length === 0 ? (
        <EmptyState icon={Target} title="Sin objetivos definidos" className="border-none py-6" />
      ) : (
        <ul className="flex flex-col gap-4">
          {goals.map((goal) => (
            <li key={goal.id} className="flex flex-col gap-1.5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink-800">{goal.name}</p>
                  <p className="text-xs text-ink-500">
                    {goalMetricLabels[goal.metricType]} · {formatDate(goal.periodStart)} — {formatDate(goal.periodEnd)}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-semibold text-ink-800">
                    {formatNumber(goal.currentValue)} / {formatNumber(goal.targetValue)}
                  </span>
                  <button onClick={() => setDeleting(goal)} className="text-ink-400 hover:text-danger-600">
                    <Trash2 className="size-4" />
                  </button>
                </div>
              </div>
              <ProgressBar value={goal.progressPercentage} tone={goal.progressPercentage >= 100 ? 'success' : 'brand'} />
            </li>
          ))}
        </ul>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Nuevo objetivo">
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          <Field label="Nombre" htmlFor="goalName" error={errors.name?.message} required>
            <Input id="goalName" invalid={!!errors.name} {...register('name')} />
          </Field>
          <Field label="Métrica" htmlFor="metricType" required>
            <Select id="metricType" {...register('metricType')}>
              {Object.entries(goalMetricLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Valor objetivo" htmlFor="targetValue" error={errors.targetValue?.message} required>
            <Input id="targetValue" type="number" step="0.01" invalid={!!errors.targetValue} {...register('targetValue')} />
          </Field>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Desde" htmlFor="periodStart" error={errors.periodStart?.message} required>
              <Input id="periodStart" type="date" invalid={!!errors.periodStart} {...register('periodStart')} />
            </Field>
            <Field label="Hasta" htmlFor="periodEnd" error={errors.periodEnd?.message} required>
              <Input id="periodEnd" type="date" invalid={!!errors.periodEnd} {...register('periodEnd')} />
            </Field>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="outline" onClick={() => setFormOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting}>
              Crear objetivo
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        title={`¿Eliminar "${deleting?.name}"?`}
        confirmLabel="Eliminar"
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </Card>
  )
}
