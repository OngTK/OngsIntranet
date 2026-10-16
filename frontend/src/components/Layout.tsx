import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import type { ReactNode } from 'react'

export function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b bg-white px-6 py-3 shadow-sm">
        <div className="mx-auto flex max-w-5xl items-center justify-between">
          <span className="text-lg font-semibold text-gray-800">OngsIntranet</span>
          <div className="flex items-center gap-4">
            {user?.profileImageUrl && (
              <img
                src={user.profileImageUrl}
                alt={user.name}
                className="h-8 w-8 rounded-full"
              />
            )}
            <span className="text-sm text-gray-600">{user?.name}</span>
            <button
              type="button"
              onClick={handleLogout}
              className="text-sm text-gray-500 hover:text-gray-800"
            >
              로그아웃
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl">{children}</main>
    </div>
  )
}
