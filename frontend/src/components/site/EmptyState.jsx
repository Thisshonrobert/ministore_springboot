import { PackageOpen } from 'lucide-react'
import { cn } from '@/lib/utils'

export function EmptyState({ icon: Icon = PackageOpen, title, description, action, className }) {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-card/60 px-6 py-16 text-center',
        className,
      )}
    >
      <span className="mb-5 grid size-14 place-items-center rounded-full bg-secondary text-secondary-foreground">
        <Icon className="size-6" aria-hidden="true" />
      </span>
      <h3 className="font-display text-2xl font-semibold tracking-tight">{title}</h3>
      {description && (
        <p className="mt-2 max-w-md text-sm leading-relaxed text-muted-foreground">{description}</p>
      )}
      {action && <div className="mt-6">{action}</div>}
    </div>
  )
}
