import { apiClient } from './client'
import type { Content, ContentRequest, PageResponse } from '@/types'

export interface ContentListParams {
  status?: string
  platformId?: number
  campaignId?: number
  page?: number
  size?: number
  sort?: string
}

export const contentApi = {
  list: (params: ContentListParams) => apiClient.get<PageResponse<Content>>('/content', { params }).then((r) => r.data),
  getById: (id: number) => apiClient.get<Content>(`/content/${id}`).then((r) => r.data),
  create: (payload: ContentRequest) => apiClient.post<Content>('/content', payload).then((r) => r.data),
  update: (id: number, payload: ContentRequest) => apiClient.put<Content>(`/content/${id}`, payload).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/content/${id}`),
}
