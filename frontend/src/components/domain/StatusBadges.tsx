import { Badge } from '@/components/ui/Badge'
import type { CampaignStatus, ContentStatus, LeadStatus, LeadTemperature, RecommendationPriority } from '@/types'
import {
  campaignStatusLabels,
  campaignStatusTone,
  contentStatusLabels,
  contentStatusTone,
  leadStatusLabels,
  leadStatusTone,
  leadTemperatureLabels,
  leadTemperatureTone,
  recommendationPriorityLabels,
  recommendationPriorityTone,
} from '@/utils/labels'

export function CampaignStatusBadge({ status }: { status: CampaignStatus }) {
  return (
    <Badge tone={campaignStatusTone[status]} dot>
      {campaignStatusLabels[status]}
    </Badge>
  )
}

export function LeadStatusBadge({ status }: { status: LeadStatus }) {
  return <Badge tone={leadStatusTone[status]}>{leadStatusLabels[status]}</Badge>
}

export function LeadTemperatureBadge({ temperature }: { temperature: LeadTemperature }) {
  return (
    <Badge tone={leadTemperatureTone[temperature]} dot>
      {leadTemperatureLabels[temperature]}
    </Badge>
  )
}

export function ContentStatusBadge({ status }: { status: ContentStatus }) {
  return <Badge tone={contentStatusTone[status]}>{contentStatusLabels[status]}</Badge>
}

export function RecommendationPriorityBadge({ priority }: { priority: RecommendationPriority }) {
  return <Badge tone={recommendationPriorityTone[priority]}>{recommendationPriorityLabels[priority]}</Badge>
}
