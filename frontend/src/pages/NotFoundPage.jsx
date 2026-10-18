import { Link } from 'react-router-dom'
import { ArrowLeft, Compass } from 'lucide-react'
import { Button } from '@/components/ui/button'

export function NotFoundPage() {
  return (
    <div className="mx-auto flex w-full max-w-3xl flex-col items-center px-5 py-24 text-center sm:px-8">
      <span className="grid size-16 place-items-center rounded-full bg-secondary">
        <Compass className="size-7 text-primary" aria-hidden="true" />
      </span>
      <p className="eyebrow mt-6">Page 404</p>
      <h1 className="mt-3 font-display text-5xl font-semibold tracking-[-0.04em]">
        This shelf does not exist.
      </h1>
      <p className="mt-4 max-w-md text-muted-foreground">
        The page may have moved, or the link might have a typo. The shop itself is still open.
      </p>
      <Button asChild size="lg" className="mt-8">
        <Link to="/">
          <ArrowLeft className="size-4" aria-hidden="true" />
          Back to the shop
        </Link>
      </Button>
    </div>
  )
}
