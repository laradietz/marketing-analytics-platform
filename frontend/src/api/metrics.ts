import { apiClient } from './client'
import type { CampaignMetric, CampaignMetricRequest } from '@/types'

export interface MetricImportResult {
  totalRows: number
  imported: number
  errors: { row: number; message: string }[]
}

export const metricsApi = {
  listByCampaign: (campaignId: number) =>
    apiClient.get<CampaignMetric[]>(`/campaigns/${campaignId}/metrics`).then((r) => r.data),
  record: (campaignId: number, payload: CampaignMetricRequest) =>
    apiClient.post<CampaignMetric>(`/campaigns/${campaignId}/metrics`, payload).then((r) => r.data),
  importCsv: (campaignId: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient
      .post<MetricImportResult>(`/campaigns/${campaignId}/metrics/import`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => r.data)
  },
  downloadTemplate: () =>
    apiClient.get(`/metrics/import/template`, { responseType: 'blob' }).then((r) => r.data as Blob),
}
