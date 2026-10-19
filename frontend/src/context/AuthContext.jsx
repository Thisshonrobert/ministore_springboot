//member 3
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, resetCsrfToken } from '@/lib/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true

    async function loadSession() {
      try {
        const customer = await api('/auth/me')
        if (active) {
          setUser(customer)
        }
      } catch {
        // A 401 simply means the visitor is anonymous. The HttpSession cookie is
        // HttpOnly, so the backend is the only source of truth after a refresh.
        if (active) {
          setUser(null)
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    loadSession()

    return () => {
      active = false
    }
  }, [])

  const login = useCallback(async (credentials) => {
    const customer = await api('/auth/login', { method: 'POST', body: credentials })
    setUser(customer)
    return customer
  }, [])

  const register = useCallback(async (details) => {
    const customer = await api('/auth/register', { method: 'POST', body: details })
    setUser(customer)
    return customer
  }, [])

  const logout = useCallback(async () => {
    try {
      await api('/auth/logout', { method: 'POST' })
    } finally {
      resetCsrfToken()
      setUser(null)
    }
  }, [])

  const refresh = useCallback(async () => {
    try {
      const customer = await api('/auth/me')
      setUser(customer)
      return customer
    } catch (error) {
      if (error.status === 401) {
        setUser(null)
        return null
      }
      throw error
    }
  }, [])

  const value = useMemo(
    () => ({
      user,
      loading,
      isAuthenticated: Boolean(user),
      login,
      register,
      logout,
      refresh,
    }),
    [user, loading, login, register, logout, refresh],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider.')
  }
  return context
}
