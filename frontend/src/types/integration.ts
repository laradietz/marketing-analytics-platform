export type IntegrationProviderType = 'META_ADS' | 'GOOGLE_ADS' | 'TIKTOK_ADS'
export type ConnectionStatus = 'DISCONNECTED' | 'CONNECTED' | 'ERROR'

export interface PlatformConnection {
  platformId: number
  platformName: string
  colorHex: string | null
  provider: IntegrationProviderType
  status: ConnectionStatus
  externalAccountId: string | null
  lastSyncedAt: string | null
  lastSyncMessage: string | null
  providerConfigured: boolean
}

export interface ManualConnectRequest {
  accessToken: string
  refreshToken?: string
  externalAccountId: string
}

export interface SyncResult {
  campaignsSynced: number
  metricsImported: number
  errors: string[]
}
