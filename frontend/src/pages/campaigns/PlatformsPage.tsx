import { useEffect, useMemo, useState } from 'react'
import { campaignsApi } from '@/api/campaigns'
import { usePlatforms } from '@/hooks/usePlatforms'
import type { Campaign } from '@/types'
import { Card } from '@/components/ui/Card'
import { SkeletonCard } from '@/components/ui/Skeleton'
import { formatCurrency, formatMultiplier } from '@/utils/format'

export function PlatformsPage() {
  const { platforms, loading: platformsLoading } = usePlatforms()
  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    campaignsApi
      .list({ size: 200 })
      .then((res) => setCampaigns(res.content))
      .finally(() => setLoading(false))
  }, [])

  const aggregates = useMemo(() => {
    return platforms.map((platform) => {
      const platformCampaigns = campaigns.filter((c) => c.platform.id === platform.id)
      const spend = platformCampaigns.reduce((sum, c) => sum + c.metrics.totals.spend, 0)
      const revenue = platformCampaigns.reduce((sum, c) => sum + c.metrics.totals.revenue, 0)
      const roas = spend > 0 ? revenue / spend : 0
      const activeCount = platformCampaigns.filter((c) => c.status === 'ACTIVE').length
      return { platform, campaignCount: platformCampaigns.length, activeCount, spend, revenue, roas }
    })
  }, [platforms, campaigns])

  if (platformsLoading || loading) {
    return (
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: 5 }).map((_, i) => (
          <SkeletonCard key={i} />
        ))}
      </div>
    )
  }

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {aggregates.map(({ platform, campaignCount, activeCount, spend, roas }) => (
        <Card key={platform.id} className="flex flex-col gap-3">
          <div className="flex items-center gap-2.5">
            <span className="size-3 rounded-full" style={{ backgroundColor: platform.colorHex ?? '#94a3b8' }} />
            <h3 className="text-sm font-semibold text-ink-900">{platform.name}</h3>
          </div>
          <div className="grid grid-cols-2 gap-3 text-sm">
            <Stat label="Campañas" value={String(campaignCount)} />
            <Stat label="Activas" value={String(activeCount)} />
            <Stat label="Inversión" value={formatCurrency(spend)} />
            <Stat label="ROAS" value={formatMultiplier(roas)} />
          </div>
        </Card>
      ))}
    </div>
  )
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs text-ink-500">{label}</p>
      <p className="font-semibold text-ink-900">{value}</p>
    </div>
  )
}
