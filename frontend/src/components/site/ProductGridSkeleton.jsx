//member 3
import { Skeleton } from '@/components/ui/skeleton'

export function ProductGridSkeleton({ count = 6 }) {
  return (
    <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3" aria-hidden="true">
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="overflow-hidden rounded-2xl border border-border bg-card">
          <Skeleton className="aspect-[5/3] rounded-none" />
          <div className="space-y-4 p-5">
            <div className="space-y-2">
              <Skeleton className="h-6 w-2/3" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-4/5" />
            </div>
            <div className="flex items-center justify-between border-t border-border/70 pt-4">
              <Skeleton className="h-5 w-20" />
              <Skeleton className="h-8 w-28 rounded-full" />
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}
