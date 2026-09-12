import type { Platform } from '@/types'

export function PlatformTag({ platform }: { platform: Platform | null }) {
  if (!platform) {
    return <span className="text-xs text-ink-400">—</span>
  }
  return (
    <span className="inline-flex items-center gap-1.5 text-sm text-ink-700">
      <span
        className="size-2 shrink-0 rounded-full"
        style={{ backgroundColor: platform.colorHex ?? '#94a3b8' }}
        aria-hidden
      />
      {platform.name}
    </span>
  )
}
