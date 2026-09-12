import { apiClient } from './client'
import type { User } from '@/types'

export interface UpdateProfilePayload {
  name: string
  jobTitle?: string
  companyName?: string
}

export interface ChangePasswordPayload {
  currentPassword: string
  newPassword: string
}

export const usersApi = {
  me: () => apiClient.get<User>('/users/me').then((r) => r.data),
  updateProfile: (payload: UpdateProfilePayload) => apiClient.put<User>('/users/me', payload).then((r) => r.data),
  changePassword: (payload: ChangePasswordPayload) => apiClient.put('/users/me/password', payload),
}
