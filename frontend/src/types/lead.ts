import type { LeadSource, LeadStatus, LeadTemperature } from './common'

export interface Lead {
  id: number
  name: string
  email: string
  phone: string | null
  company: string | null
  source: LeadSource
  status: LeadStatus
  score: number
  contactCount: number
  temperature: LeadTemperature
  campaignId: number | null
  campaignName: string | null
  assignedToId: number | null
  assignedToName: string | null
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface LeadRequest {
  name: string
  email: string
  phone?: string
  company?: string
  source: LeadSource
  campaignId?: number | null
  assignedToId?: number | null
  notes?: string
}
