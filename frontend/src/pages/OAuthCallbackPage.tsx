import { useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export function OAuthCallbackPage() {
  const [searchParams] = useSearchParams()
  const { login } = useAuth()
  const navigate = useNavigate()
  const called = useRef(false)

  useEffect(() => {
    if (called.current) return
    called.current = true

    const token = searchParams.get('token')
    const error = searchParams.get('error')

    window.history.replaceState({}, '', '/auth/callback')

    if (error) {
      navigate(`/login?error=${error}`, { replace: true })
      return
    }

    if (!token) {
      navigate('/login', { replace: true })
      return
    }

    login(token).then(() => {
      navigate('/', { replace: true })
    })
  }, [searchParams, login, navigate])

  return (
    <div className="flex min-h-screen items-center justify-center">
      <p className="text-gray-500">로그인 처리 중...</p>
    </div>
  )
}
