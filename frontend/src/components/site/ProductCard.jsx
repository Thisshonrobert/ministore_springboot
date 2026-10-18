import { BookOpen, Loader2, Package, PenLine, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { formatPrice } from '@/lib/format'

const palettes = [
  {
    background: 'oklch(0.36 0.064 156)',
    foreground: 'oklch(0.982 0.012 85)',
    accent: 'oklch(0.62 0.128 42)',
  },
  {
    background: 'oklch(0.62 0.128 42)',
    foreground: 'oklch(0.99 0.005 90)',
    accent: 'oklch(0.36 0.064 156)',
  },
  {
    background: 'oklch(0.9 0.048 82)',
    foreground: 'oklch(0.28 0.03 70)',
    accent: 'oklch(0.55 0.09 40)',
  },
  {
    background: 'oklch(0.29 0.025 70)',
    foreground: 'oklch(0.97 0.012 85)',
    accent: 'oklch(0.72 0.12 55)',
  },
]

function CategoryGlyph({ categoryName, ...props }) {
  const name = (categoryName || '').toLowerCase()
  if (name.includes('book')) return <BookOpen {...props} />
  if (name.includes('station') || name.includes('pen')) return <PenLine {...props} />
  return <Package {...props} />
}

export function ProductCard({ product, categoryName, index = 0, onAdd, adding = false }) {
  const palette = palettes[(Number(product.categoryId) || index) % palettes.length]
  const suffix = String(index + 1).padStart(2, '0')

  return (
    <article className="group flex flex-col overflow-hidden rounded-2xl border border-border bg-card shadow-[0_1px_0_rgba(255,255,255,0.7)_inset,0_20px_50px_-36px_rgba(40,32,20,0.45)] transition-all duration-300 hover:-translate-y-1 hover:border-primary/25 hover:shadow-[0_24px_55px_-34px_rgba(31,77,58,0.45)]">
      <div
        className="relative aspect-[5/3] overflow-hidden p-5"
        style={{ backgroundColor: palette.background, color: palette.foreground }}
      >
        <div className="absolute inset-0 opacity-30">
          <div className="absolute -top-8 -right-8 size-32 rounded-full border border-current" />
          <div className="absolute -bottom-10 -left-8 size-36 rounded-full border border-current" />
          <div className="absolute top-1/2 left-1/2 h-px w-[140%] -translate-x-1/2 -translate-y-1/2 rotate-[-18deg] bg-current opacity-25" />
        </div>

        <div className="relative flex h-full flex-col justify-between">
          <div className="flex items-start justify-between gap-3">
            <span
              className="rounded-full border px-2.5 py-1 text-[0.62rem] font-semibold tracking-[0.18em] uppercase"
              style={{ borderColor: palette.foreground, color: palette.foreground }}
            >
              {categoryName || 'Goods'}
            </span>
            <span className="font-mono text-[0.68rem] opacity-70">№ {suffix}</span>
          </div>

          <div className="flex items-end justify-between gap-4">
            <div className="grid size-14 place-items-center rounded-xl border border-current/40 bg-current/10 backdrop-blur-[1px]">
              <CategoryGlyph
                categoryName={categoryName}
                className="size-6"
                aria-hidden="true"
                style={{ color: palette.accent }}
              />
            </div>
            <span
              className="font-display text-3xl leading-none font-light tracking-tight"
              style={{ color: palette.foreground }}
            >
              {formatPrice(product.price)}
            </span>
          </div>
        </div>
      </div>

      <div className="flex flex-1 flex-col gap-4 p-5">
        <div className="space-y-1.5">
          <h3 className="font-display text-xl leading-tight font-semibold tracking-tight">
            {product.name}
          </h3>
          <p className="min-h-10 text-sm leading-relaxed text-muted-foreground">
            {product.description || 'A useful thing, chosen for the everyday shelf.'}
          </p>
        </div>

        <div className="mt-auto flex items-center justify-between gap-3 border-t border-border/70 pt-4">
          <span className="text-[0.62rem] font-semibold tracking-[0.16em] text-muted-foreground uppercase">
            On the shelf
          </span>
          <Button
            size="sm"
            onClick={() => onAdd?.(product)}
            disabled={adding}
            aria-label={`Add ${product.name} to cart`}
            className="rounded-full"
          >
            {adding ? (
              <Loader2 className="size-4 animate-spin" aria-hidden="true" />
            ) : (
              <Plus className="size-4" aria-hidden="true" />
            )}
            {adding ? 'Adding' : 'Add to cart'}
          </Button>
        </div>
      </div>
    </article>
  )
}
