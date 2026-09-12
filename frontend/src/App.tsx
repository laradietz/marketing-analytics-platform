import { lazy, Suspense } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { LineChart } from 'lucide-react'
import { AuthProvider } from '@/context/AuthContext'
import { ToastProvider } from '@/context/ToastContext'
import { ProtectedRoute } from '@/components/layout/ProtectedRoute'
import { AppLayout } from '@/components/layout/AppLayout'
import { LoginPage } from '@/pages/auth/LoginPage'
import { RegisterPage } from '@/pages/auth/RegisterPage'

const DashboardPage = lazy(() => import('@/pages/dashboard/DashboardPage').then((m) => ({ default: m.DashboardPage })))
const CampaignsListPage = lazy(() => import('@/pages/campaigns/CampaignsListPage').then((m) => ({ default: m.CampaignsListPage })))
const CampaignDetailPage = lazy(() => import('@/pages/campaigns/CampaignDetailPage').then((m) => ({ default: m.CampaignDetailPage })))
const MetricsOverviewPage = lazy(() => import('@/pages/campaigns/MetricsOverviewPage').then((m) => ({ default: m.MetricsOverviewPage })))
const PlatformsPage = lazy(() => import('@/pages/campaigns/PlatformsPage').then((m) => ({ default: m.PlatformsPage })))
const LeadsListPage = lazy(() => import('@/pages/leads/LeadsListPage').then((m) => ({ default: m.LeadsListPage })))
const LeadScoringPage = lazy(() => import('@/pages/leads/LeadScoringPage').then((m) => ({ default: m.LeadScoringPage })))
const ContentPlannerPage = lazy(() => import('@/pages/content/ContentPlannerPage').then((m) => ({ default: m.ContentPlannerPage })))
const ContentIdeasPage = lazy(() => import('@/pages/content/ContentIdeasPage').then((m) => ({ default: m.ContentIdeasPage })))
const CommentsPage = lazy(() => import('@/pages/content/CommentsPage').then((m) => ({ default: m.CommentsPage })))
const ReportsPage = lazy(() => import('@/pages/analytics/ReportsPage').then((m) => ({ default: m.ReportsPage })))
const RecommendationsPage = lazy(() => import('@/pages/analytics/RecommendationsPage').then((m) => ({ default: m.RecommendationsPage })))
const SettingsPage = lazy(() => import('@/pages/settings/SettingsPage').then((m) => ({ default: m.SettingsPage })))
const IntegrationsPage = lazy(() => import('@/pages/settings/IntegrationsPage').then((m) => ({ default: m.IntegrationsPage })))

function PageFallback() {
  return (
    <div className="flex h-64 items-center justify-center">
      <span className="flex size-9 animate-pulse items-center justify-center rounded-lg bg-brand-600 text-white">
        <LineChart className="size-4" />
      </span>
    </div>
  )
}

export function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <Suspense fallback={<PageFallback />}>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />

              <Route element={<ProtectedRoute />}>
                <Route element={<AppLayout />}>
                  <Route path="/" element={<DashboardPage />} />
                  <Route path="/campaigns" element={<CampaignsListPage />} />
                  <Route path="/campaigns/:id" element={<CampaignDetailPage />} />
                  <Route path="/metrics" element={<MetricsOverviewPage />} />
                  <Route path="/platforms" element={<PlatformsPage />} />
                  <Route path="/leads" element={<LeadsListPage />} />
                  <Route path="/leads/scoring" element={<LeadScoringPage />} />
                  <Route path="/content" element={<ContentPlannerPage />} />
                  <Route path="/content/ideas" element={<ContentIdeasPage />} />
                  <Route path="/content/comments" element={<CommentsPage />} />
                  <Route path="/reports" element={<ReportsPage />} />
                  <Route path="/recommendations" element={<RecommendationsPage />} />
                  <Route path="/settings" element={<SettingsPage />} />
                  <Route path="/settings/integrations" element={<IntegrationsPage />} />
                </Route>
              </Route>

              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  )
}
