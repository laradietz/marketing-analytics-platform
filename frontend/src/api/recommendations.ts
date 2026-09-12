import { apiClient } from './client'
import type { PageResponse, Recommendation, RecommendationStatus } from '@/types'

export const recommendationsApi = {
  generate: () => apiClient.post<Recommendation[]>('/recommendations/generate').then((r) => r.data),
  list: (page = 0, size = 10) =>
    apiClient.get<PageResponse<Recommendation>>('/recommendations', { params: { page, size } }).then((r) => r.data),
  updateStatus: (id: number, status: RecommendationStatus) =>
    apiClient.patch<Recommendation>(`/recommendations/${id}/status`, null, { params: { status } }).then((r) => r.data),
}
