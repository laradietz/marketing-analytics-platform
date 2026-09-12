import { apiClient } from './client'
import type { AuthResponse } from '@/types'

export interface LoginPayload {
  email: string
  password: string
}

export interface RegisterPayload {
  name: string
  email: string
  password: string
  companyName?: string
}

export const authApi = {
  login: (payload: LoginPayload) => apiClient.post<AuthResponse>('/auth/login', payload).then((r) => r.data),
  register: (payload: RegisterPayload) => apiClient.post<AuthResponse>('/auth/register', payload).then((r) => r.data),
  logout: () => apiClient.post('/auth/logout'),
}
