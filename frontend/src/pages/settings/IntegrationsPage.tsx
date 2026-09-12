import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Cable, ExternalLink, KeyRound, RefreshCw, Unplug } from 'lucide-react'
import { integrationsApi } from '@/api/integrations'
import { useToast } from '@/context/ToastContext'
import type { PlatformConnection } from '@/types'
import { Card, CardHeader } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Modal } from '@/components/ui/Modal'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { Field } from '@/components/ui/Field'
import { Input } from '@/components/ui/Input'
import { Textarea } from '@/components/ui/Textarea'
import { Badge } from '@/components/ui/Badge'
import { SkeletonCard } from '@/components/ui/Skeleton'
import { integrationProviderLabels, connectionStatusLabels, connectionStatusTone } from '@/utils/labels'
import { formatDateTime } from '@/utils/format'
import { extractErrorMessage } from '@/api/client'

const manualSchema = z.object({
  accessToken: z.string().min(1, 'El access token es obligatorio'),
  refreshToken: z.string().optional(),
  externalAccountId: z.string().min(1, 'El ID de cuenta es obligatorio'),
})
type ManualValues = z.infer<typeof manualSchema>

const syncSchema = z.object({
  from: z.string().min(1, 'La fecha de inicio es obligatoria'),
  to: z.string().min(1, 'La fecha de fin es obligatoria'),
})
type SyncValues = z.infer<typeof syncSchema>

export function IntegrationsPage() {
  const { showToast } = useToast()
  const [searchParams, setSearchParams] = useSearchParams()
  const [connections, setConnections] = useState<PlatformConnection[]>([])
  const [loading, setLoading] = useState(true)
  const [manualTarget, setManualTarget] = useState<PlatformConnection | null>(null)
  const [syncTarget, setSyncTarget] = useState<PlatformConnection | null>(null)
  const [disconnectTarget, setDisconnectTarget] = useState<PlatformConnection | null>(null)

  const load = () => {
    setLoading(true)
    integrationsApi
      .list()
      .then(setConnections)
      .catch((err) => showToast(extractErrorMessage(err), 'error'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  useEffect(() => {
    if (searchParams.get('connected')) {
      showToast('Plataforma conectada correctamente')
      setSearchParams({}, { replace: true })
      load()
    } else if (searchParams.get('error')) {
      showToast(decodeURIComponent(searchParams.get('error') ?? 'No se pudo conectar'), 'error')
      setSearchParams({}, { replace: true })
    }
  }, [])

  const handleOAuthConnect = async (connection: PlatformConnection) => {
    try {
      const url = await integrationsApi.getAuthorizeUrl(connection.platformId)
      window.location.href = url
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos iniciar la conexión.'), 'error')
    }
  }

  const handleDisconnect = async () => {
    if (!disconnectTarget) return
    try {
      await integrationsApi.disconnect(disconnectTarget.platformId)
      showToast(`${disconnectTarget.platformName} desconectado`)
      setDisconnectTarget(null)
      load()
    } catch (err) {
      showToast(extractErrorMessage(err), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-5">
      <p className="max-w-2xl text-sm text-ink-500">
        Conectá tus cuentas reales de Meta, Google o TikTok Ads para sincronizar métricas automáticamente, o pegá un
        token de acceso si ya tenés uno. Mientras gestionás el acceso a cada plataforma, seguí cargando métricas
        manualmente o por CSV desde el detalle de cada campaña.
      </p>

      {loading && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      )}

      {!loading && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          {connections.map((connection) => (
            <Card key={connection.platformId} className="flex flex-col gap-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className="size-2.5 rounded-full" style={{ backgroundColor: connection.colorHex ?? '#94a3b8' }} />
                  <h3 className="text-sm font-semibold text-ink-900">{connection.platformName}</h3>
                </div>
                <Badge tone={connectionStatusTone[connection.status]} dot>
                  {connectionStatusLabels[connection.status]}
                </Badge>
              </div>
              <p className="text-xs text-ink-500">{integrationProviderLabels[connection.provider]}</p>

              {connection.status === 'CONNECTED' && (
                <div className="text-xs text-ink-500">
                  <p>Cuenta: {connection.externalAccountId ?? '—'}</p>
                  <p>Última sync: {connection.lastSyncedAt ? formatDateTime(connection.lastSyncedAt) : 'nunca'}</p>
                  {connection.lastSyncMessage && <p className="mt-1 text-ink-400">{connection.lastSyncMessage}</p>}
                </div>
              )}

              {!connection.providerConfigured && (
                <p className="rounded-md bg-warning-50 px-2 py-1.5 text-xs text-warning-700">
                  Sin credenciales de OAuth configuradas en el servidor todavía. Podés conectar con un token manual.
                </p>
              )}

              <div className="mt-auto flex flex-wrap gap-2 pt-2">
                {connection.status !== 'CONNECTED' ? (
                  <>
                    {connection.providerConfigured && (
                      <Button size="sm" variant="outline" onClick={() => handleOAuthConnect(connection)}>
                        <ExternalLink className="size-3.5" /> Conectar con OAuth
                      </Button>
                    )}
                    <Button size="sm" variant="ghost" onClick={() => setManualTarget(connection)}>
                      <KeyRound className="size-3.5" /> Token manual
                    </Button>
                  </>
                ) : (
                  <>
                    <Button size="sm" variant="outline" onClick={() => setSyncTarget(connection)}>
                      <RefreshCw className="size-3.5" /> Sincronizar
                    </Button>
                    <Button size="sm" variant="ghost" onClick={() => setDisconnectTarget(connection)}>
                      <Unplug className="size-3.5" /> Desconectar
                    </Button>
                  </>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}

      <Card>
        <CardHeader
          title="¿Cómo vincular una campaña?"
          description="Para que la sincronización sepa qué campaña actualizar"
        />
        <div className="flex items-start gap-3 text-sm text-ink-600">
          <Cable className="mt-0.5 size-4 shrink-0 text-ink-400" />
          <p>
            Editá la campaña y completá el campo <strong>ID de campaña externa</strong> con el ID que esa campaña
            tiene en Meta/Google/TikTok Ads Manager. Sin ese ID, la sincronización no sabe qué campaña actualizar.
          </p>
        </div>
      </Card>

      {manualTarget && (
        <ManualConnectModal
          connection={manualTarget}
          onClose={() => setManualTarget(null)}
          onConnected={() => {
            setManualTarget(null)
            load()
          }}
        />
      )}

      {syncTarget && <SyncModal connection={syncTarget} onClose={() => setSyncTarget(null)} onSynced={load} />}

      <ConfirmDialog
        open={!!disconnectTarget}
        title={`¿Desconectar ${disconnectTarget?.platformName}?`}
        description="Vas a poder volver a conectarla cuando quieras."
        confirmLabel="Desconectar"
        danger
        onConfirm={handleDisconnect}
        onCancel={() => setDisconnectTarget(null)}
      />
    </div>
  )
}

function ManualConnectModal({
  connection,
  onClose,
  onConnected,
}: {
  connection: PlatformConnection
  onClose: () => void
  onConnected: () => void
}) {
  const { showToast } = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(manualSchema) })

  const onSubmit = async (values: ManualValues) => {
    try {
      await integrationsApi.connectManually(connection.platformId, values)
      showToast(`${connection.platformName} conectado`)
      onConnected()
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos conectar la cuenta.'), 'error')
    }
  }

  return (
    <Modal open onClose={onClose} title={`Conectar ${connection.platformName} con token`} size="md">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <Field label="Access token" htmlFor="accessToken" error={errors.accessToken?.message} required>
          <Textarea id="accessToken" rows={3} invalid={!!errors.accessToken} {...register('accessToken')} />
        </Field>
        <Field label="Refresh token (opcional)" htmlFor="refreshToken">
          <Input id="refreshToken" {...register('refreshToken')} />
        </Field>
        <Field
          label="ID de cuenta externa"
          htmlFor="externalAccountId"
          error={errors.externalAccountId?.message}
          hint="Ej: act_123456 (Meta), 1234567890 (Google Ads customer id), o el advertiser_id de TikTok"
          required
        >
          <Input id="externalAccountId" invalid={!!errors.externalAccountId} {...register('externalAccountId')} />
        </Field>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancelar
          </Button>
          <Button type="submit" loading={isSubmitting}>
            Conectar
          </Button>
        </div>
      </form>
    </Modal>
  )
}

function SyncModal({ connection, onClose, onSynced }: { connection: PlatformConnection; onClose: () => void; onSynced: () => void }) {
  const { showToast } = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(syncSchema),
    defaultValues: {
      from: new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10),
      to: new Date().toISOString().slice(0, 10),
    },
  })

  const onSubmit = async (values: SyncValues) => {
    try {
      const result = await integrationsApi.sync(connection.platformId, values.from, values.to)
      showToast(
        result.errors.length === 0
          ? `Se sincronizaron ${result.campaignsSynced} campañas (${result.metricsImported} métricas)`
          : `Sincronizado con ${result.errors.length} error(es): ${result.errors[0]}`,
        result.errors.length === 0 ? 'success' : 'error',
      )
      onSynced()
      onClose()
    } catch (err) {
      showToast(extractErrorMessage(err, 'No pudimos sincronizar.'), 'error')
    }
  }

  return (
    <Modal open onClose={onClose} title={`Sincronizar ${connection.platformName}`} size="sm">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <p className="text-xs text-ink-500">
          Solo se sincronizan las campañas de esta plataforma que tengan un ID de campaña externa cargado.
        </p>
        <div className="grid grid-cols-2 gap-4">
          <Field label="Desde" htmlFor="from" error={errors.from?.message} required>
            <Input id="from" type="date" invalid={!!errors.from} {...register('from')} />
          </Field>
          <Field label="Hasta" htmlFor="to" error={errors.to?.message} required>
            <Input id="to" type="date" invalid={!!errors.to} {...register('to')} />
          </Field>
        </div>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancelar
          </Button>
          <Button type="submit" loading={isSubmitting}>
            <RefreshCw className="size-4" /> Sincronizar
          </Button>
        </div>
      </form>
    </Modal>
  )
}
