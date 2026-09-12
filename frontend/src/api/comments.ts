import { apiClient } from './client'
import type {
  AudienceComment,
  CommentPlatformSummary,
  CommentRequest,
  CommentSentiment,
  ContentIdea,
  GenerateIdeasFromCommentsRequest,
  PageResponse,
} from '@/types'

export interface CommentImportResult {
  totalRows: number
  imported: number
  errors: { row: number; message: string }[]
}

export interface CommentListParams {
  platformId?: number
  sentiment?: CommentSentiment
  search?: string
  page?: number
  size?: number
}

export const commentsApi = {
  list: (params: CommentListParams) =>
    apiClient.get<PageResponse<AudienceComment>>('/comments', { params }).then((r) => r.data),
  create: (payload: CommentRequest) => apiClient.post<AudienceComment>('/comments', payload).then((r) => r.data),
  importCsv: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient
      .post<CommentImportResult>('/comments/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => r.data)
  },
  downloadTemplate: () =>
    apiClient.get('/comments/import/template', { responseType: 'blob' }).then((r) => r.data as Blob),
  remove: (id: number) => apiClient.delete(`/comments/${id}`),
  summary: (platformId: number) =>
    apiClient.get<CommentPlatformSummary>('/comments/summary', { params: { platformId } }).then((r) => r.data),
  generateIdeas: (payload: GenerateIdeasFromCommentsRequest) =>
    apiClient.post<ContentIdea[]>('/comments/generate-ideas', payload).then((r) => r.data),
}
