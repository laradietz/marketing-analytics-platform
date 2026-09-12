import type { DerivedMetrics } from './metric'
import type { Recommendation } from './recommendation'

export interface CampaignBreakdown {
  campaignId: number
  campaignName: string
  platformName: string
  status: string
  metrics: DerivedMetrics
}

export interface ReportSummary {
  periodStart: string
  periodEnd: string
  kpis: DerivedMetrics
  campaigns: CampaignBreakdown[]
  recommendations: Recommendation[]
}

export interface Report {
  id: number
  name: string
  periodStart: string
  periodEnd: string
  generatedByName: string | null
  summary: ReportSummary
  createdAt: string
}

export interface ReportGenerateRequest {
  name: string
  periodStart: string
  periodEnd: string
  campaignIds?: number[]
  platformIds?: number[]
}
