import { apiClient } from './client'
import type { Lead, LeadRequest, LeadStatus, PageResponse } from '@/types'

export interface LeadListParams {
  search?: string
  status?: string
  source?: string
  campaignId?: number
  page?: number
  size?: number
  sort?: string
}

export const leadsApi = {
  list: (params: LeadListParams) => apiClient.get<PageResponse<Lead>>('/leads', { params }).then((r) => r.data),
  getById: (id: number) => apiClient.get<Lead>(`/leads/${id}`).then((r) => r.data),
  create: (payload: LeadRequest) => apiClient.post<Lead>('/leads', payload).then((r) => r.data),
  update: (id: number, payload: LeadRequest) => apiClient.put<Lead>(`/leads/${id}`, payload).then((r) => r.data),
  updateStatus: (id: number, status: LeadStatus) => apiClient.patch<Lead>(`/leads/${id}/status`, { status }).then((r) => r.data),
  registerContact: (id: number) => apiClient.post<Lead>(`/leads/${id}/contact`).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/leads/${id}`),
}
