import type { LucideIcon } from 'lucide-react'
import {
  LayoutDashboard,
  Megaphone,
  BarChart3,
  Globe,
  Users,
  Flame,
  CalendarDays,
  Sparkles,
  FileBarChart,
  Lightbulb,
  Settings,
  Cable,
  MessageSquareText,
} from 'lucide-react'

export interface NavLeaf {
  label: string
  to: string
  icon?: LucideIcon
  /** Set when a sibling route starts with this leaf's path (e.g. /leads vs /leads/scoring),
   * so the NavLink doesn't stay highlighted once the more specific sibling is active. */
  end?: boolean
}

export interface NavSection {
  label: string
  icon: LucideIcon
  to?: string
  children?: NavLeaf[]
}

export const navigation: NavSection[] = [
  { label: 'Dashboard', icon: LayoutDashboard, to: '/' },
  {
    label: 'Marketing',
    icon: Megaphone,
    children: [
      { label: 'Campañas', to: '/campaigns' },
      { label: 'Métricas', to: '/metrics', icon: BarChart3 },
      { label: 'Plataformas', to: '/platforms', icon: Globe },
      { label: 'Integraciones', to: '/settings/integrations', icon: Cable },
    ],
  },
  {
    label: 'Leads',
    icon: Users,
    children: [
      { label: 'Todos los leads', to: '/leads', end: true },
      { label: 'Lead Scoring', to: '/leads/scoring', icon: Flame },
    ],
  },
  {
    label: 'Contenido',
    icon: CalendarDays,
    children: [
      { label: 'Planificador', to: '/content', end: true },
      { label: 'Ideas con IA', to: '/content/ideas', icon: Sparkles },
      { label: 'Comentarios', to: '/content/comments', icon: MessageSquareText },
    ],
  },
  {
    label: 'Analítica',
    icon: FileBarChart,
    children: [
      { label: 'Reportes', to: '/reports' },
      { label: 'Recomendaciones', to: '/recommendations', icon: Lightbulb },
    ],
  },
  { label: 'Configuración', icon: Settings, to: '/settings' },
]
