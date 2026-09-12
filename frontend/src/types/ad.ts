import type { AdFormat, AdStatus } from './common'

export interface Ad {
  id: number
  campaignId: number
  name: string
  format: AdFormat
  headline: string | null
  body: string | null
  ctaLabel: string | null
  status: AdStatus
  createdAt: string
}

export interface AdRequest {
  name: string
  format: AdFormat
  headline?: string
  body?: string
  ctaLabel?: string
  status?: AdStatus
}
