export type Role = 'EMPLOYEE' | 'MANAGER' | 'CEO' | 'ADMIN'

export interface UserDetail {
  id: number
  name: string
  email: string
  department: string | null
  position: string | null
  profileImageUrl: string | null
  phone: string | null
  role: Role
  isActive: boolean
  createdAt: string
}
