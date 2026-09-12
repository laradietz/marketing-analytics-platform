import { apiClient } from './client'
import type { Ad, AdRequest } from '@/types'

export const adsApi = {
  listByCampaign: (campaignId: number) => apiClient.get<Ad[]>(`/campaigns/${campaignId}/ads`).then((r) => r.data),
  create: (campaignId: number, payload: AdRequest) =>
    apiClient.post<Ad>(`/campaigns/${campaignId}/ads`, payload).then((r) => r.data),
  update: (campaignId: number, adId: number, payload: AdRequest) =>
    apiClient.put<Ad>(`/campaigns/${campaignId}/ads/${adId}`, payload).then((r) => r.data),
  remove: (campaignId: number, adId: number) => apiClient.delete(`/campaigns/${campaignId}/ads/${adId}`),
}
