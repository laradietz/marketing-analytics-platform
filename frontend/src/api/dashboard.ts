import { apiClient } from './client'
import type { DashboardData, DashboardFilters } from '@/types'

export const dashboardApi = {
  get: (filters: DashboardFilters) => apiClient.get<DashboardData>('/dashboard', { params: filters }).then((r) => r.data),
}
