import type {
  AdFormat,
  AdStatus,
  CampaignObjective,
  CampaignStatus,
  CommentSentiment,
  CommentSource,
  ConnectionStatus,
  ContentIdeaStatus,
  ContentStatus,
  ContentTone,
  GoalMetric,
  IntegrationProviderType,
  LeadSource,
  LeadStatus,
  LeadTemperature,
  RecommendationPriority,
  RecommendationStatus,
  RecommendationType,
} from '@/types'
import type { BadgeTone } from '@/components/ui/Badge'

export const campaignStatusLabels: Record<CampaignStatus, string> = {
  DRAFT: 'Borrador',
  ACTIVE: 'Activa',
  PAUSED: 'Pausada',
  COMPLETED: 'Finalizada',
  CANCELLED: 'Cancelada',
}

export const campaignStatusTone: Record<CampaignStatus, BadgeTone> = {
  DRAFT: 'neutral',
  ACTIVE: 'success',
  PAUSED: 'warning',
  COMPLETED: 'info',
  CANCELLED: 'danger',
}

export const campaignObjectiveLabels: Record<CampaignObjective, string> = {
  AWARENESS: 'Reconocimiento',
  TRAFFIC: 'Tráfico',
  LEADS: 'Leads',
  CONVERSIONS: 'Conversiones',
  SALES: 'Ventas',
  ENGAGEMENT: 'Interacción',
}

export const leadStatusLabels: Record<LeadStatus, string> = {
  NEW: 'Nuevo',
  CONTACTED: 'Contactado',
  QUALIFIED: 'Calificado',
  CONVERTED: 'Convertido',
  LOST: 'Perdido',
}

export const leadStatusTone: Record<LeadStatus, BadgeTone> = {
  NEW: 'info',
  CONTACTED: 'warning',
  QUALIFIED: 'brand',
  CONVERTED: 'success',
  LOST: 'danger',
}

export const leadSourceLabels: Record<LeadSource, string> = {
  ORGANIC: 'Orgánico',
  PAID_ADS: 'Publicidad paga',
  REFERRAL: 'Referido',
  SOCIAL_MEDIA: 'Redes sociales',
  EMAIL: 'Email',
  EVENT: 'Evento',
  OTHER: 'Otro',
}

export const leadTemperatureLabels: Record<LeadTemperature, string> = {
  HOT: 'Caliente',
  WARM: 'Tibio',
  COLD: 'Frío',
}

export const leadTemperatureTone: Record<LeadTemperature, BadgeTone> = {
  HOT: 'danger',
  WARM: 'warning',
  COLD: 'info',
}

export const contentStatusLabels: Record<ContentStatus, string> = {
  IDEA: 'Idea',
  DRAFT: 'Borrador',
  SCHEDULED: 'Programado',
  PUBLISHED: 'Publicado',
}

export const contentStatusTone: Record<ContentStatus, BadgeTone> = {
  IDEA: 'neutral',
  DRAFT: 'warning',
  SCHEDULED: 'info',
  PUBLISHED: 'success',
}

export const contentToneLabels: Record<ContentTone, string> = {
  PROFESSIONAL: 'Profesional',
  CASUAL: 'Casual',
  ENERGETIC: 'Enérgico',
  INSPIRATIONAL: 'Inspirador',
  HUMOROUS: 'Humorístico',
  URGENT: 'Urgente',
}

export const contentIdeaStatusLabels: Record<ContentIdeaStatus, string> = {
  SAVED: 'Guardada',
  DISCARDED: 'Descartada',
  CONVERTED_TO_CONTENT: 'Convertida en contenido',
}

export const recommendationTypeLabels: Record<RecommendationType, string> = {
  BUDGET_INCREASE: 'Aumentar presupuesto',
  BUDGET_DECREASE: 'Reducir presupuesto',
  PAUSE_CAMPAIGN: 'Pausar campaña',
  PLATFORM_SHIFT: 'Redistribuir plataforma',
  CREATIVE_REFRESH: 'Renovar creatividades',
  GENERAL: 'General',
}

export const recommendationPriorityLabels: Record<RecommendationPriority, string> = {
  LOW: 'Baja',
  MEDIUM: 'Media',
  HIGH: 'Alta',
}

export const recommendationPriorityTone: Record<RecommendationPriority, BadgeTone> = {
  LOW: 'neutral',
  MEDIUM: 'warning',
  HIGH: 'danger',
}

export const recommendationStatusLabels: Record<RecommendationStatus, string> = {
  NEW: 'Nueva',
  VIEWED: 'Vista',
  APPLIED: 'Aplicada',
  DISMISSED: 'Descartada',
}

export const adFormatLabels: Record<AdFormat, string> = {
  IMAGE: 'Imagen',
  VIDEO: 'Video',
  CAROUSEL: 'Carrusel',
  TEXT: 'Texto',
}

export const adStatusLabels: Record<AdStatus, string> = {
  ACTIVE: 'Activo',
  PAUSED: 'Pausado',
  ARCHIVED: 'Archivado',
}

export const goalMetricLabels: Record<GoalMetric, string> = {
  CONVERSIONS: 'Conversiones',
  REVENUE: 'Ingresos',
  ROAS: 'ROAS',
  LEADS: 'Leads',
  CTR: 'CTR',
}

export const integrationProviderLabels: Record<IntegrationProviderType, string> = {
  META_ADS: 'Meta Ads (Facebook/Instagram)',
  GOOGLE_ADS: 'Google Ads',
  TIKTOK_ADS: 'TikTok Ads',
}

export const connectionStatusLabels: Record<ConnectionStatus, string> = {
  DISCONNECTED: 'Sin conectar',
  CONNECTED: 'Conectado',
  ERROR: 'Con errores',
}

export const connectionStatusTone: Record<ConnectionStatus, BadgeTone> = {
  DISCONNECTED: 'neutral',
  CONNECTED: 'success',
  ERROR: 'danger',
}

export const commentSentimentLabels: Record<CommentSentiment, string> = {
  POSITIVE: 'Positivo',
  NEUTRAL: 'Neutral',
  NEGATIVE: 'Negativo',
}

export const commentSentimentTone: Record<CommentSentiment, BadgeTone> = {
  POSITIVE: 'success',
  NEUTRAL: 'neutral',
  NEGATIVE: 'danger',
}

export const commentSourceLabels: Record<CommentSource, string> = {
  MANUAL: 'Manual',
  IMPORTED: 'Importado',
}
