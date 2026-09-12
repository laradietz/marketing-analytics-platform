import { apiClient } from './client'
import type { ActivityLogEntry } from '@/types'

export const activityApi = {
  recent: () => apiClient.get<ActivityLogEntry[]>('/activity').then((r) => r.data),
}
