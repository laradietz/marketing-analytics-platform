import type { DerivedMetrics } from './metric'
import type { Lead } from './lead'
import type { Recommendation } from './recommendation'

export interface TrendPoint {
  date: string
  value: number
}

/** One day's raw totals; derived ratios (CTR, ROAS, ...) are computed client-side from these. */
export interface DailyBreakdownPoint {
  date: string
  impressions: number
  clicks: number
  conversions: number
  spend: number
  revenue: number
}

export interface PlatformPerformance {
  platformId: number
  platformName: string
  colorHex: string | null
  spend: number
  revenue: number
  roas: number
  ctr: number
  conversions: number
}

export interface CampaignRanking {
  campaignId: number
  campaignName: string
  platformName: string
  roas: number
  spend: number
  revenue: number
}

export interface BudgetDistributionItem {
  platformId: number
  platformName: string
  colorHex: string | null
  spend: number
  percentage: number
}

export interface ActivityLogEntry {
  id: number
  userName: string
  action: string
  entityType: string
  entityId: number | null
  description: string
  createdAt: string
}

export interface DashboardData {
  kpis: DerivedMetrics
  dailyBreakdown: DailyBreakdownPoint[]
  platformPerformance: PlatformPerformance[]
  topCampaigns: CampaignRanking[]
  bottomCampaigns: CampaignRanking[]
  budgetDistribution: BudgetDistributionItem[]
  recentLeads: Lead[]
  recentRecommendations: Recommendation[]
  recentActivity: ActivityLogEntry[]
}

export interface DashboardFilters {
  from?: string
  to?: string
  platformId?: number
  campaignId?: number
  objective?: string
}
