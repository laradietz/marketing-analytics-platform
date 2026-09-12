import { apiClient } from './client'
import type { ContentGenerationRequest, ContentIdea, ContentIdeaUpdateRequest, PageResponse } from '@/types'

export const contentIdeasApi = {
  generate: (payload: ContentGenerationRequest) =>
    apiClient.post<ContentIdea[]>('/content-ideas/generate', payload).then((r) => r.data),
  list: (page = 0, size = 12) =>
    apiClient.get<PageResponse<ContentIdea>>('/content-ideas', { params: { page, size } }).then((r) => r.data),
  update: (id: number, payload: ContentIdeaUpdateRequest) =>
    apiClient.put<ContentIdea>(`/content-ideas/${id}`, payload).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/content-ideas/${id}`),
}
