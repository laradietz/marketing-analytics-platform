import type { CampaignObjective, ContentIdeaStatus, ContentStatus, ContentTone } from './common'
import type { Platform } from './platform'

export interface Content {
  id: number
  title: string
  copyText: string | null
  platform: Platform
  objective: CampaignObjective | null
  status: ContentStatus
  scheduledDate: string | null
  publishedDate: string | null
  hashtags: string | null
  ctaText: string | null
  campaignId: number | null
  campaignName: string | null
  createdByName: string | null
  createdAt: string
  updatedAt: string
}

export interface ContentRequest {
  title: string
  copyText?: string
  platformId: number
  objective?: CampaignObjective
  status: ContentStatus
  scheduledDate?: string | null
  hashtags?: string
  ctaText?: string
  campaignId?: number | null
}

export interface ContentGenerationRequest {
  productOrService: string
  targetAudience: string
  platformId: number
  objective: CampaignObjective
  tone: ContentTone
  callToAction?: string
  variantCount?: number
}

export interface ContentIdea {
  id: number
  productOrService: string
  targetAudience: string | null
  platform: Platform | null
  objective: CampaignObjective | null
  tone: ContentTone
  generatedTitle: string | null
  generatedCopy: string | null
  generatedCta: string | null
  generatedHashtags: string | null
  status: ContentIdeaStatus
  createdAt: string
}

export interface ContentIdeaUpdateRequest {
  generatedTitle: string
  generatedCopy: string
  generatedCta?: string
  generatedHashtags?: string
  status?: ContentIdeaStatus
}
