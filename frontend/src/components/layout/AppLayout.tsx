import { useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'
import { navigation } from './navigation'

function resolveTitle(pathname: string): string {
  let bestMatch: { label: string; to: string } | null = null

  for (const section of navigation) {
    if (section.to) {
      const candidate = { label: section.label, to: section.to }
      if (pathname === candidate.to && (!bestMatch || candidate.to.length > bestMatch.to.length)) {
        bestMatch = candidate
      }
    }
    for (const leaf of section.children ?? []) {
      if (pathname.startsWith(leaf.to) && (!bestMatch || leaf.to.length > bestMatch.to.length)) {
        bestMatch = leaf
      }
    }
  }

  return bestMatch?.label ?? 'Marketing Analytics Platform'
}

export function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false)
  const location = useLocation()

  return (
    <div className="flex h-screen overflow-hidden bg-ink-50">
      <Sidebar mobileOpen={mobileOpen} onCloseMobile={() => setMobileOpen(false)} />
      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar onOpenMobileMenu={() => setMobileOpen(true)} title={resolveTitle(location.pathname)} />
        <main className="flex-1 overflow-y-auto px-4 py-5 lg:px-6 lg:py-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
