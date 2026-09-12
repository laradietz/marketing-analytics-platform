export interface MetricTotals {
  impressions: number
  reach: number
  clicks: number
  conversions: number
  spend: number
  revenue: number
  likes: number
  comments: number
  shares: number
  saves: number
}

export interface DerivedMetrics {
  totals: MetricTotals
  ctr: number
  cpc: number
  cpm: number
  conversionRate: number
  cpa: number
  roas: number
}

export interface CampaignMetric {
  id: number
  recordedDate: string
  impressions: number
  reach: number
  clicks: number
  conversions: number
  spend: number
  revenue: number
  likes: number
  comments: number
  shares: number
  saves: number
  ctr: number
  cpc: number
  cpm: number
  conversionRate: number
  cpa: number
  roas: number
}

export interface CampaignMetricRequest {
  recordedDate: string
  impressions: number
  reach: number
  clicks: number
  conversions: number
  spend: number
  revenue: number
  likes?: number
  comments?: number
  shares?: number
  saves?: number
}
