import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from 'react'
import type { ReactNode } from 'react'
import api, { clearAccessToken, setAccessToken } from '../api/axios'
import type { UserDetail } from '../types/user'

interface AuthContextValue {
  user: UserDetail | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (token: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDetail | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const fetchMe = useCallback(async () => {
    try {
      const { data } = await api.get<{ success: boolean; data: UserDetail }>('/api/users/me')
      setUser(data.data)
    } catch {
      setUser(null)
    }
  }, [])

  useEffect(() => {
    const init = async () => {
      try {
        const { data } = await api.post<{ success: boolean; data: { accessToken: string } }>(
          '/auth/refresh',
          {},
        )
        setAccessToken(data.data.accessToken)
        await fetchMe()
      } catch {
        // no session
      } finally {
        setIsLoading(false)
      }
    }
    init()
  }, [fetchMe])

  const login = useCallback(
    async (token: string) => {
      setAccessToken(token)
      await fetchMe()
    },
    [fetchMe],
  )

  const logout = useCallback(async () => {
    try {
      await api.post('/auth/logout')
    } finally {
      clearAccessToken()
      setUser(null)
    }
  }, [])

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: user !== null,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
