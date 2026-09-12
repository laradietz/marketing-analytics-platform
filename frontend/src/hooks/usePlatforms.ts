import { useEffect, useState } from 'react'
import { platformsApi } from '@/api/platforms'
import type { Platform } from '@/types'

let cache: Platform[] | null = null
let inflight: Promise<Platform[]> | null = null

function loadPlatforms(): Promise<Platform[]> {
  if (cache) return Promise.resolve(cache)
  if (!inflight) {
    inflight = platformsApi.list().then((data) => {
      cache = data
      inflight = null
      return data
    })
  }
  return inflight
}

export function usePlatforms() {
  const [platforms, setPlatforms] = useState<Platform[]>(cache ?? [])
  const [loading, setLoading] = useState(!cache)

  useEffect(() => {
    if (cache) return
    let active = true
    loadPlatforms().then((data) => {
      if (active) {
        setPlatforms(data)
        setLoading(false)
      }
    })
    return () => {
      active = false
    }
  }, [])

  return { platforms, loading }
}
