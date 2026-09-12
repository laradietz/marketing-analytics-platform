import { apiClient } from './client'
import type { Platform } from '@/types'

export const platformsApi = {
  list: () => apiClient.get<Platform[]>('/platforms').then((r) => r.data),
}
