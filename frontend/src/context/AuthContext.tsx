import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '@/api/auth'
import type { LoginPayload, RegisterPayload } from '@/api/auth'
import { registerUnauthorizedHandler, TOKEN_STORAGE_KEY } from '@/api/client'
import { usersApi } from '@/api/users'
import type { User } from '@/types'

interface AuthContextValue {
  user: User | null
  loading: boolean
  login: (payload: LoginPayload) => Promise<void>
  register: (payload: RegisterPayload) => Promise<void>
  logout: () => void
  refreshProfile: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  const clearSession = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    setUser(null)
  }, [])

  useEffect(() => {
    registerUnauthorizedHandler(clearSession)
  }, [clearSession])

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY)
    if (!token) {
      setLoading(false)
      return
    }
    usersApi
      .me()
      .then(setUser)
      .catch(() => clearSession())
      .finally(() => setLoading(false))
  }, [clearSession])

  const login = useCallback(async (payload: LoginPayload) => {
    const response = await authApi.login(payload)
    localStorage.setItem(TOKEN_STORAGE_KEY, response.token)
    setUser(response.user)
  }, [])

  const register = useCallback(async (payload: RegisterPayload) => {
    const response = await authApi.register(payload)
    localStorage.setItem(TOKEN_STORAGE_KEY, response.token)
    setUser(response.user)
  }, [])

  const logout = useCallback(() => {
    authApi.logout().catch(() => undefined)
    clearSession()
  }, [clearSession])

  const refreshProfile = useCallback(async () => {
    const profile = await usersApi.me()
    setUser(profile)
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refreshProfile }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }
  return context
}
