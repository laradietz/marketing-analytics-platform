import type { CampaignObjective, CampaignStatus } from './common'
import type { DerivedMetrics } from './metric'
import type { Platform } from './platform'

export interface Campaign {
  id: number
  name: string
  description: string | null
  platform: Platform
  objective: CampaignObjective
  budget: number
  startDate: string
  endDate: string | null
  status: CampaignStatus
  targetAudience: string | null
  ownerId: number | null
  ownerName: string | null
  notes: string | null
  externalCampaignId: string | null
  metrics: DerivedMetrics
  createdAt: string
  updatedAt: string
}

export interface CampaignRequest {
  name: string
  description?: string
  platformId: number
  objective: CampaignObjective
  budget: number
  startDate: string
  endDate?: string | null
  targetAudience?: string
  ownerId?: number | null
  notes?: string
  externalCampaignId?: string
}
