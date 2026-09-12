import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Menu, ChevronDown, LogOut, UserRound } from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import { initials } from '@/utils/format'

interface TopbarProps {
  onOpenMobileMenu: () => void
  title: string
}

export function Topbar({ onOpenMobileMenu, title }: TopbarProps) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!menuOpen) return
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setMenuOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [menuOpen])

  return (
    <header className="flex h-14 shrink-0 items-center justify-between border-b border-ink-200 bg-white px-4 lg:px-6">
      <div className="flex items-center gap-3">
        <button
          onClick={onOpenMobileMenu}
          aria-label="Abrir menú"
          className="rounded-md p-1.5 text-ink-500 hover:bg-ink-100 lg:hidden"
        >
          <Menu className="size-5" />
        </button>
        <h1 className="text-sm font-semibold text-ink-900">{title}</h1>
      </div>

      <div className="relative" ref={ref}>
        <button
          onClick={() => setMenuOpen((v) => !v)}
          aria-haspopup="menu"
          aria-expanded={menuOpen}
          className="flex items-center gap-2 rounded-lg px-2 py-1.5 hover:bg-ink-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
        >
          <span className="flex size-7 items-center justify-center rounded-full bg-brand-100 text-xs font-semibold text-brand-700">
            {user ? initials(user.name) : ''}
          </span>
          <span className="hidden text-sm font-medium text-ink-700 sm:inline">{user?.name}</span>
          <ChevronDown className="size-3.5 text-ink-400" />
        </button>
        {menuOpen && (
          <div
            role="menu"
            className="absolute right-0 z-20 mt-1 w-48 animate-fade-in rounded-lg border border-ink-200 bg-white py-1 shadow-md"
          >
            <button
              role="menuitem"
              onClick={() => {
                setMenuOpen(false)
                navigate('/settings')
              }}
              className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-ink-700 hover:bg-ink-50"
            >
              <UserRound className="size-4" /> Mi perfil
            </button>
            <button
              role="menuitem"
              onClick={logout}
              className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-danger-600 hover:bg-ink-50"
            >
              <LogOut className="size-4" /> Cerrar sesión
            </button>
          </div>
        )}
      </div>
    </header>
  )
}
