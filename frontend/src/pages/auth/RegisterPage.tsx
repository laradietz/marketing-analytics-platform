import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { LineChart } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useAuth } from '@/context/AuthContext'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Field } from '@/components/ui/Field'
import { extractErrorMessage } from '@/api/client'

const schema = z.object({
  name: z.string().min(1, 'El nombre es obligatorio').max(120),
  email: z.string().min(1, 'El email es obligatorio').email('El email no es válido'),
  password: z.string().min(8, 'La contraseña debe tener al menos 8 caracteres'),
  companyName: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

export function RegisterPage() {
  const { register: registerUser } = useAuth()
  const navigate = useNavigate()
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = async (values: FormValues) => {
    setServerError(null)
    try {
      await registerUser(values)
      navigate('/', { replace: true })
    } catch (error) {
      setServerError(extractErrorMessage(error, 'No pudimos crear tu cuenta.'))
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-ink-50 px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="mb-8 flex flex-col items-center text-center">
          <span className="mb-3 flex size-10 items-center justify-center rounded-xl bg-brand-600 text-white">
            <LineChart className="size-5" />
          </span>
          <h1 className="text-lg font-semibold text-ink-900">Creá tu cuenta</h1>
          <p className="mt-1 text-sm text-ink-500">Empezá a gestionar tus campañas hoy mismo</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-xl border border-ink-200 bg-white p-6 shadow-xs">
          {serverError && (
            <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-700">{serverError}</div>
          )}
          <Field label="Nombre completo" htmlFor="name" error={errors.name?.message} required>
            <Input id="name" autoComplete="name" placeholder="Lara Dietz" invalid={!!errors.name} {...register('name')} />
          </Field>
          <Field label="Empresa" htmlFor="companyName">
            <Input id="companyName" autoComplete="organization" placeholder="Mi empresa (opcional)" {...register('companyName')} />
          </Field>
          <Field label="Email" htmlFor="email" error={errors.email?.message} required>
            <Input id="email" type="email" autoComplete="email" placeholder="tu@empresa.com" invalid={!!errors.email} {...register('email')} />
          </Field>
          <Field label="Contraseña" htmlFor="password" error={errors.password?.message} hint="Mínimo 8 caracteres" required>
            <Input id="password" type="password" autoComplete="new-password" placeholder="••••••••" invalid={!!errors.password} {...register('password')} />
          </Field>
          <Button type="submit" loading={isSubmitting} className="mt-1 w-full">
            Crear cuenta
          </Button>
        </form>

        <p className="mt-5 text-center text-sm text-ink-500">
          ¿Ya tenés cuenta?{' '}
          <Link to="/login" className="font-medium text-brand-600 hover:text-brand-700">
            Iniciá sesión
          </Link>
        </p>
      </div>
    </div>
  )
}
