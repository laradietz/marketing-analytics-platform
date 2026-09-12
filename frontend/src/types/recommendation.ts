import type { RecommendationPriority, RecommendationStatus, RecommendationType } from './common'

export interface Recommendation {
  id: number
  title: string
  description: string
  type: RecommendationType
  priority: RecommendationPriority
  relatedMetricName: string | null
  relatedMetricValue: number | null
  campaignId: number | null
  campaignName: string | null
  platformId: number | null
  platformName: string | null
  status: RecommendationStatus
  createdAt: string
}
