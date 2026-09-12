import type { GoalMetric } from './common'

export interface Goal {
  id: number
  name: string
  metricType: GoalMetric
  targetValue: number
  currentValue: number
  progressPercentage: number
  periodStart: string
  periodEnd: string
  campaignId: number | null
  campaignName: string | null
}

export interface GoalRequest {
  name: string
  metricType: GoalMetric
  targetValue: number
  periodStart: string
  periodEnd: string
  campaignId?: number | null
}
