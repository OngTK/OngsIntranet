import { useAuth } from '../contexts/AuthContext'

export function HomePage() {
  const { user } = useAuth()

  return (
    <div className="p-8">
      <h1 className="mb-2 text-2xl font-bold text-gray-800">안녕하세요, {user?.name}님</h1>
      <p className="text-gray-500">{user?.email}</p>
    </div>
  )
}
