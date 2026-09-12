import { apiClient } from './client'
import type { Goal, GoalRequest } from '@/types'

export const goalsApi = {
  list: () => apiClient.get<Goal[]>('/goals').then((r) => r.data),
  create: (payload: GoalRequest) => apiClient.post<Goal>('/goals', payload).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/goals/${id}`),
}
