import { apiClient } from './client'
import type { Budget, BudgetRequest } from '@/types'

export const budgetsApi = {
  listByCampaign: (campaignId: number) => apiClient.get<Budget[]>(`/campaigns/${campaignId}/budgets`).then((r) => r.data),
  create: (campaignId: number, payload: BudgetRequest) =>
    apiClient.post<Budget>(`/campaigns/${campaignId}/budgets`, payload).then((r) => r.data),
  remove: (campaignId: number, budgetId: number) => apiClient.delete(`/campaigns/${campaignId}/budgets/${budgetId}`),
}
