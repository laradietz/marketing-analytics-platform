export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors: { field: string; message: string }[]
}

export type CampaignObjective = 'AWARENESS' | 'TRAFFIC' | 'LEADS' | 'CONVERSIONS' | 'SALES' | 'ENGAGEMENT'
export type CampaignStatus = 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'
export type LeadStatus = 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'CONVERTED' | 'LOST'
export type LeadSource = 'ORGANIC' | 'PAID_ADS' | 'REFERRAL' | 'SOCIAL_MEDIA' | 'EMAIL' | 'EVENT' | 'OTHER'
export type LeadTemperature = 'HOT' | 'WARM' | 'COLD'
export type ContentStatus = 'IDEA' | 'DRAFT' | 'SCHEDULED' | 'PUBLISHED'
export type ContentTone = 'PROFESSIONAL' | 'CASUAL' | 'ENERGETIC' | 'INSPIRATIONAL' | 'HUMOROUS' | 'URGENT'
export type ContentIdeaStatus = 'SAVED' | 'DISCARDED' | 'CONVERTED_TO_CONTENT'
export type RecommendationType = 'BUDGET_INCREASE' | 'BUDGET_DECREASE' | 'PAUSE_CAMPAIGN' | 'PLATFORM_SHIFT' | 'CREATIVE_REFRESH' | 'GENERAL'
export type RecommendationPriority = 'LOW' | 'MEDIUM' | 'HIGH'
export type RecommendationStatus = 'NEW' | 'VIEWED' | 'APPLIED' | 'DISMISSED'
export type AdFormat = 'IMAGE' | 'VIDEO' | 'CAROUSEL' | 'TEXT'
export type AdStatus = 'ACTIVE' | 'PAUSED' | 'ARCHIVED'
export type GoalMetric = 'CONVERSIONS' | 'REVENUE' | 'ROAS' | 'LEADS' | 'CTR'
export type Role = 'ADMIN' | 'USER'
