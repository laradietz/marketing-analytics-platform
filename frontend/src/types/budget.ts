export interface Budget {
  id: number
  campaignId: number
  periodStart: string
  periodEnd: string
  plannedAmount: number
  actualSpend: number
  notes: string | null
}

export interface BudgetRequest {
  periodStart: string
  periodEnd: string
  plannedAmount: number
  notes?: string
}
