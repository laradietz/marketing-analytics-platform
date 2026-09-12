import { useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { ChevronDown, LineChart, X } from 'lucide-react'
import { cn } from '@/lib/cn'
import { navigation } from './navigation'
import type { NavSection } from './navigation'

interface SidebarProps {
  mobileOpen: boolean
  onCloseMobile: () => void
}

export function Sidebar({ mobileOpen, onCloseMobile }: SidebarProps) {
  return (
    <>
      {mobileOpen && (
        <div className="fixed inset-0 z-40 bg-ink-900/40 lg:hidden" onClick={onCloseMobile} aria-hidden />
      )}
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-ink-200 bg-white transition-transform duration-200 lg:static lg:z-auto lg:translate-x-0',
          mobileOpen ? 'translate-x-0' : '-translate-x-full',
        )}
      >
        <div className="flex h-14 shrink-0 items-center justify-between border-b border-ink-100 px-4">
          <div className="flex items-center gap-2">
            <span className="flex size-7 items-center justify-center rounded-lg bg-brand-600 text-white">
              <LineChart className="size-4" aria-hidden />
            </span>
            <span className="text-sm font-semibold text-ink-900">Marketing Analytics</span>
          </div>
          <button
            onClick={onCloseMobile}
            aria-label="Cerrar menú"
            className="rounded-md p-1 text-ink-400 hover:bg-ink-100 lg:hidden"
          >
            <X className="size-4" />
          </button>
        </div>
        <nav className="flex-1 overflow-y-auto px-3 py-4">
          <ul className="flex flex-col gap-1">
            {navigation.map((section) => (
              <NavItem key={section.label} section={section} onNavigate={onCloseMobile} />
            ))}
          </ul>
        </nav>
      </aside>
    </>
  )
}

function NavItem({ section, onNavigate }: { section: NavSection; onNavigate: () => void }) {
  const location = useLocation()
  const hasActiveChild = section.children?.some((c) => location.pathname.startsWith(c.to))
  const [expanded, setExpanded] = useState(Boolean(hasActiveChild))

  if (!section.children) {
    return (
      <li>
        <NavLink
          to={section.to!}
          end={section.to === '/'}
          onClick={onNavigate}
          className={({ isActive }) =>
            cn(
              'flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
              isActive ? 'bg-brand-50 text-brand-700' : 'text-ink-600 hover:bg-ink-100 hover:text-ink-900',
            )
          }
        >
          <section.icon className="size-4 shrink-0" aria-hidden />
          {section.label}
        </NavLink>
      </li>
    )
  }

  return (
    <li>
      <button
        type="button"
        onClick={() => setExpanded((v) => !v)}
        aria-expanded={expanded}
        className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-ink-600 hover:bg-ink-100 hover:text-ink-900"
      >
        <section.icon className="size-4 shrink-0" aria-hidden />
        <span className="flex-1 text-left">{section.label}</span>
        <ChevronDown className={cn('size-3.5 transition-transform', expanded && 'rotate-180')} aria-hidden />
      </button>
      {expanded && (
        <ul className="mt-0.5 flex flex-col gap-0.5 border-l border-ink-100 pl-4">
          {section.children.map((leaf) => (
            <li key={leaf.to}>
              <NavLink
                to={leaf.to}
                end={leaf.end}
                onClick={onNavigate}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm transition-colors',
                    isActive ? 'bg-brand-50 font-medium text-brand-700' : 'text-ink-500 hover:bg-ink-100 hover:text-ink-800',
                  )
                }
              >
                {leaf.icon && <leaf.icon className="size-3.5" aria-hidden />}
                {leaf.label}
              </NavLink>
            </li>
          ))}
        </ul>
      )}
    </li>
  )
}
