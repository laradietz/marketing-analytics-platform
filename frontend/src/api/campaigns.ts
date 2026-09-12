import { apiClient } from './client'
import type { Campaign, CampaignRequest, PageResponse } from '@/types'

export interface CampaignListParams {
  search?: string
  status?: string
  platformId?: number
  objective?: string
  ownerId?: number
  page?: number
  size?: number
  sort?: string
}

export const campaignsApi = {
  list: (params: CampaignListParams) =>
    apiClient.get<PageResponse<Campaign>>('/campaigns', { params }).then((r) => r.data),
  getById: (id: number) => apiClient.get<Campaign>(`/campaigns/${id}`).then((r) => r.data),
  create: (payload: CampaignRequest) => apiClient.post<Campaign>('/campaigns', payload).then((r) => r.data),
  update: (id: number, payload: CampaignRequest) => apiClient.put<Campaign>(`/campaigns/${id}`, payload).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/campaigns/${id}`),
  activate: (id: number) => apiClient.post<Campaign>(`/campaigns/${id}/activate`).then((r) => r.data),
  pause: (id: number) => apiClient.post<Campaign>(`/campaigns/${id}/pause`).then((r) => r.data),
  complete: (id: number) => apiClient.post<Campaign>(`/campaigns/${id}/complete`).then((r) => r.data),
  cancel: (id: number) => apiClient.post<Campaign>(`/campaigns/${id}/cancel`).then((r) => r.data),
}
