//member 4
import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { toast } from 'sonner'
import {
  ArrowLeft,
  BookOpen,
  Loader2,
  Package,
  PenLine,
  Plus,
  RefreshCcw,
  ShoppingBag,
} from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/site/EmptyState'
import { ProductReviews } from '@/components/site/ProductReviews'
import { useAuth } from '@/context/AuthContext'
import { api } from '@/lib/api'
import { addProductToCart } from '@/lib/cart'
import { formatPrice } from '@/lib/format'

function CategoryGlyph({ categoryName, ...props }) {
  const name = (categoryName || '').toLowerCase()
  if (name.includes('book')) return <BookOpen {...props} />
  if (name.includes('station') || name.includes('pen')) return <PenLine {...props} />
  return <Package {...props} />
}

function ProductSkeleton() {
  return (
    <div className="mx-auto w-full max-w-6xl space-y-8 px-5 py-14 sm:px-8">
      <Skeleton className="h-4 w-32" />
      <div className="grid gap-10 lg:grid-cols-[1fr_0.9fr]">
        <Skeleton className="h-72 w-full rounded-2xl" />
        <div className="space-y-5">
          <Skeleton className="h-5 w-28" />
          <Skeleton className="h-12 w-3/4" />
          <Skeleton className="h-24 w-full" />
          <Skeleton className="h-12 w-40 rounded-full" />
        </div>
      </div>
    </div>
  )
}

/**
 * One product, including its reviews and their AI summary. The product itself
 * is read from the public catalogue endpoint; adding to the cart goes through
 * the signed-in customer's own endpoint.
 */
export function ProductPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [product, setProduct] = useState(null)
  const [categoryNames, setCategoryNames] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [adding, setAdding] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [found, categories] = await Promise.all([
        api(`/products/${id}`),
        api('/categories').catch(() => []),
      ])
      setProduct(found)
      setCategoryNames(
        Object.fromEntries(categories.map((category) => [String(category.id), category.name])),
      )
    } catch (requestError) {
      setError(requestError)
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => {
    load()
  }, [load])

  async function addToCart() {
    if (!user) {
      toast.info('Sign in to start a cart.', {
        description: 'Your cart belongs to your customer account.',
      })
      navigate('/login', { state: { from: `/products/${id}` } })
      return
    }

    setAdding(true)
    try {
      await addProductToCart(product.id)
      toast.success(`${product.name} is in your cart.`, {
        description: 'Your total will be waiting on the cart page.',
        action: { label: 'View cart', onClick: () => navigate('/cart') },
      })
    } catch (requestError) {
      toast.error(requestError.message || 'Could not add that item.')
    } finally {
      setAdding(false)
    }
  }

  if (loading) {
    return <ProductSkeleton />
  }

  if (error?.status === 404) {
    return (
      <div className="mx-auto w-full max-w-3xl px-5 py-20 sm:px-8">
        <EmptyState
          icon={Package}
          title="That product is not on the shelf"
          description="It may have been removed. The rest of the shelves are still open."
          action={
            <Button asChild size="lg">
              <Link to="/">Back to the shelves</Link>
            </Button>
          }
        />
      </div>
    )
  }

  if (error) {
    return (
      <div className="mx-auto w-full max-w-3xl px-5 py-20 sm:px-8">
        <Alert variant="destructive">
          <RefreshCcw className="size-4" aria-hidden="true" />
          <AlertTitle>The product did not load</AlertTitle>
          <AlertDescription>
            <p>{error.message}</p>
            <div className="mt-4 flex flex-wrap gap-2">
              <Button variant="outline" onClick={load}>
                <RefreshCcw className="size-4" aria-hidden="true" />
                Try again
              </Button>
              <Button asChild variant="ghost">
                <Link to="/">Back to the shelves</Link>
              </Button>
            </div>
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  const categoryName = categoryNames[String(product.categoryId)]

  return (
    <div className="mx-auto w-full max-w-6xl px-5 py-14 sm:px-8 lg:py-20">
      <Button asChild variant="ghost" size="sm" className="text-muted-foreground">
        <Link to="/">
          <ArrowLeft className="size-4" aria-hidden="true" />
          Back to the shelves
        </Link>
      </Button>

      <div className="mt-8 grid gap-10 lg:grid-cols-[1.05fr_0.95fr] lg:gap-14">
        <div className="relative overflow-hidden rounded-2xl border border-border bg-primary p-8 text-primary-foreground shadow-[0_40px_80px_-45px_rgba(31,77,58,0.65)] sm:p-10">
          <div className="pointer-events-none absolute inset-0 opacity-25" aria-hidden="true">
            <div className="absolute -top-10 -right-10 size-44 rounded-full border border-current" />
            <div className="absolute -bottom-14 -left-10 size-52 rounded-full border border-current" />
            <div className="absolute top-1/2 left-1/2 h-px w-[150%] -translate-x-1/2 -translate-y-1/2 rotate-[-16deg] bg-current" />
          </div>

          <div className="relative flex h-full min-h-[18rem] flex-col justify-between gap-12">
            <div className="flex items-start justify-between gap-4">
              <span className="rounded-full border border-primary-foreground/35 px-3 py-1 text-[0.62rem] font-semibold tracking-[0.2em] uppercase">
                {categoryName || 'Goods'}
              </span>
              <span className="grid size-12 place-items-center rounded-xl border border-primary-foreground/30 bg-primary-foreground/10">
                <CategoryGlyph
                  categoryName={categoryName}
                  className="size-5 text-accent"
                  aria-hidden="true"
                />
              </span>
            </div>

            <div>
              <p className="text-[0.62rem] font-semibold tracking-[0.24em] uppercase opacity-65">
                Shelf price
              </p>
              <p className="mt-2 font-display text-5xl font-light tracking-tight">
                {formatPrice(product.price)}
              </p>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-6">
          <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-3">
              <Badge variant="secondary">{categoryName || 'Goods'}</Badge>
              <span className="font-mono text-xs text-muted-foreground">
                № {String(product.id).padStart(3, '0')}
              </span>
            </div>
            <h1 className="font-display text-4xl leading-tight font-semibold tracking-[-0.03em] sm:text-5xl">
              {product.name}
            </h1>
            <p className="max-w-xl text-lg leading-relaxed text-muted-foreground">
              {product.description || 'A useful thing, chosen for the everyday shelf.'}
            </p>
          </div>

          <Separator />

          <div className="flex flex-wrap items-center gap-3">
            <Button size="xl" onClick={addToCart} disabled={adding}>
              {adding ? (
                <Loader2 className="size-4 animate-spin" aria-hidden="true" />
              ) : (
                <Plus className="size-4" aria-hidden="true" />
              )}
              {adding ? 'Adding' : 'Add to cart'}
            </Button>
            <Button asChild variant="outline" size="xl">
              <Link to="/cart">
                <ShoppingBag className="size-4" aria-hidden="true" />
                Your cart
              </Link>
            </Button>
          </div>
        </div>
      </div>

      <section className="mt-16 scroll-mt-24">
        <div className="border-b border-border pb-6">
          <p className="eyebrow">Customer reviews</p>
          <h2 className="mt-3 font-display text-3xl font-semibold tracking-[-0.03em] sm:text-4xl">
            What people said
          </h2>
        </div>

        <div className="mt-8">
          <ProductReviews productId={product.id} />
        </div>
      </section>
    </div>
  )
}
