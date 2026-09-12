import { apiClient } from './client'
import type { ManualConnectRequest, PlatformConnection, SyncResult } from '@/types'

export const integrationsApi = {
  list: () => apiClient.get<PlatformConnection[]>('/integrations').then((r) => r.data),
  getAuthorizeUrl: (platformId: number) =>
    apiClient.get<{ url: string }>(`/integrations/${platformId}/authorize-url`).then((r) => r.data.url),
  connectManually: (platformId: number, payload: ManualConnectRequest) =>
    apiClient.post<PlatformConnection>(`/integrations/${platformId}/connect-manual`, payload).then((r) => r.data),
  sync: (platformId: number, from: string, to: string) =>
    apiClient.post<SyncResult>(`/integrations/${platformId}/sync`, { from, to }).then((r) => r.data),
  disconnect: (platformId: number) => apiClient.delete(`/integrations/${platformId}`),
}
