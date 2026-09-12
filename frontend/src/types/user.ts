import type { Role } from './common'

export interface User {
  id: number
  name: string
  email: string
  role: Role
  jobTitle: string | null
  companyName: string | null
  createdAt: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  user: User
}
