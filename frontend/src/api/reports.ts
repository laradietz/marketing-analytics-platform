import { apiClient } from './client'
import type { PageResponse, Report, ReportGenerateRequest } from '@/types'

export const reportsApi = {
  generate: (payload: ReportGenerateRequest) => apiClient.post<Report>('/reports', payload).then((r) => r.data),
  list: (page = 0, size = 10) => apiClient.get<PageResponse<Report>>('/reports', { params: { page, size } }).then((r) => r.data),
  getById: (id: number) => apiClient.get<Report>(`/reports/${id}`).then((r) => r.data),
  exportCsvUrl: (id: number) => `${apiClient.defaults.baseURL}/reports/${id}/export`,
  exportCsv: (id: number) => apiClient.get(`/reports/${id}/export`, { responseType: 'blob' }).then((r) => r.data as Blob),
}
