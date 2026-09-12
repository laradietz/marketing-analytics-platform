import type { CampaignObjective, ContentTone } from './common'

export type CommentSentiment = 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE'
export type CommentSource = 'MANUAL' | 'IMPORTED'

export interface AudienceComment {
  id: number
  platformId: number
  platformName: string
  contentId: number | null
  contentTitle: string | null
  authorName: string | null
  text: string
  sentiment: CommentSentiment
  source: CommentSource
  postedAt: string | null
  createdAt: string
}

export interface CommentRequest {
  platformId: number
  contentId?: number | null
  authorName?: string
  text: string
  postedAt?: string | null
}

export interface CommentTheme {
  keyword: string
  mentions: number
  sampleQuote: string
}

export interface CommentPlatformSummary {
  total: number
  positive: number
  neutral: number
  negative: number
  topThemes: CommentTheme[]
}

export interface GenerateIdeasFromCommentsRequest {
  platformId: number
  productOrService: string
  objective: CampaignObjective
  tone: ContentTone
  variantCount?: number
}
