//member 4
import { Star } from 'lucide-react'
import { cn } from '@/lib/utils'

/**
 * Read-only star display for one rating, from 1 to 5.
 */
export function StarRating({ value = 0, className }) {
  const rating = Math.round(Number(value) || 0)

  return (
    <span
      className={cn('inline-flex items-center gap-0.5', className)}
      role="img"
      aria-label={`${rating} out of 5 stars`}
    >
      {[1, 2, 3, 4, 5].map((star) => (
        <Star
          key={star}
          className={cn('size-3.5', star <= rating ? 'fill-accent text-accent' : 'text-border')}
          aria-hidden="true"
        />
      ))}
    </span>
  )
}
