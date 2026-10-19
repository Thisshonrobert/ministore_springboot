//member 3
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { Skeleton } from '@/components/ui/skeleton'

function SessionSkeleton() {
  return (
    <div className="mx-auto w-full max-w-6xl px-5 py-16 sm:px-8">
      <div className="space-y-4">
        <Skeleton className="h-4 w-28" />
        <Skeleton className="h-12 w-72 max-w-full" />
        <Skeleton className="h-5 w-96 max-w-full" />
      </div>
    </div>
  )
}

export function RequireAuth({ children }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return <SessionSkeleton />
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return children
}

export function RequireGuest({ children }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return <SessionSkeleton />
  }

  if (user) {
    return <Navigate to={location.state?.from || '/'} replace />
  }

  return children
}
