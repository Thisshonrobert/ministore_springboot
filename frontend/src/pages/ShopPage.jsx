//member 3
import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import {
  ArrowDownRight,
  ArrowRight,
  BookOpen,
  RefreshCcw,
  ShoppingBag,
  Sparkles,
} from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { ProductCard } from '@/components/site/ProductCard'
import { ProductGridSkeleton } from '@/components/site/ProductGridSkeleton'
import { EmptyState } from '@/components/site/EmptyState'
import { useAuth } from '@/context/AuthContext'
import { api } from '@/lib/api'
import { addProductToCart } from '@/lib/cart'
import { pluralize } from '@/lib/format'
import { cn } from '@/lib/utils'

export function ShopPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [selectedCategory, setSelectedCategory] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [addingId, setAddingId] = useState(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true

    api('/categories')
      .then((result) => {
        if (active) {
          setCategories(result)
        }
      })
      .catch((requestError) => {
        if (active) {
          toast.error('Could not load the category shelves.', {
            description: requestError.message,
          })
        }
      })

    return () => {
      active = false
    }
  }, [reloadKey])

  useEffect(() => {
    const controller = new AbortController()
    let active = true

    setLoading(true)
    setError(null)

    const query = selectedCategory ? `?categoryId=${selectedCategory}` : ''
    api(`/products${query}`, { signal: controller.signal })
      .then((result) => {
        if (active) {
          setProducts(result)
        }
      })
      .catch((requestError) => {
        if (active && requestError.name !== 'AbortError') {
          setError(requestError)
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
      controller.abort()
    }
  }, [selectedCategory, reloadKey])

  const categoryNames = useMemo(() => {
    const map = new Map()
    for (const category of categories) {
      map.set(String(category.id), category.name)
    }
    return map
  }, [categories])

  function retry() {
    setReloadKey((key) => key + 1)
  }

  async function addToCart(product) {
    if (!user) {
      toast.info('Sign in to start a cart.', {
        description: 'Your cart belongs to your customer account.',
      })
      navigate('/login', { state: { from: '/' } })
      return
    }

    setAddingId(product.id)
    try {
      await addProductToCart(product.id)
      toast.success(`${product.name} is in your cart.`, {
        description: 'Your total will be waiting on the cart page.',
        action: { label: 'View cart', onClick: () => navigate('/cart') },
      })
    } catch (requestError) {
      toast.error(requestError.message || 'Could not add that item.')
    } finally {
      setAddingId(null)
    }
  }

  return (
    <div>
      <section className="paper-texture relative overflow-hidden border-b border-border">
        <div className="pointer-events-none absolute inset-0 ledger-lines opacity-30" aria-hidden="true" />
        <div className="relative mx-auto grid w-full max-w-6xl gap-12 px-5 py-16 sm:px-8 lg:grid-cols-[1.12fr_0.88fr] lg:py-24">
          <div className="max-w-2xl">
            <div className="animate-rise flex items-center gap-3">
              <span className="grid size-8 place-items-center rounded-full border border-border bg-card">
                <Sparkles className="size-3.5 text-accent" aria-hidden="true" />
              </span>
              <p className="eyebrow">The everyday edit · 2026</p>
            </div>

            <h1 className="animate-rise delay-1 mt-7 font-display text-[clamp(2.9rem,7vw,5.3rem)] leading-[0.95] font-light tracking-[-0.045em]">
              Good tools for
              <span className="relative mx-2 inline-block italic text-primary">
                ordinary
                <svg
                  viewBox="0 0 220 12"
                  className="absolute -bottom-2 left-0 h-3 w-full text-accent"
                  aria-hidden="true"
                  preserveAspectRatio="none"
                >
                  <path
                    d="M2 8.5C45 3.5 88 3 120 4.5c35 1.7 66 2 98-1"
                    fill="none"
                    stroke="currentColor"
                    strokeLinecap="round"
                    strokeWidth="2.5"
                  />
                </svg>
              </span>
              days.
            </h1>

            <p className="animate-rise delay-2 mt-7 max-w-xl text-lg leading-relaxed text-muted-foreground">
              A small, deliberate selection of stationery and books from the Mini Store shelves.
              Pick up what you need, keep a cart, and come back to it whenever you like.
            </p>

            <div className="animate-rise delay-3 mt-9 flex flex-wrap items-center gap-3">
              <Button asChild size="xl">
                <a href="#shelves">
                  Browse the shelves
                  <ArrowDownRight className="size-4" aria-hidden="true" />
                </a>
              </Button>
              {user ? (
                <Button asChild variant="outline" size="xl">
                  <Link to="/account">
                    Your account
                    <ArrowRight className="size-4" aria-hidden="true" />
                  </Link>
                </Button>
              ) : (
                <Button asChild variant="outline" size="xl">
                  <Link to="/register">
                    Create an account
                    <ArrowRight className="size-4" aria-hidden="true" />
                  </Link>
                </Button>
              )}
            </div>

            <div className="animate-rise delay-4 mt-12 flex flex-wrap gap-x-8 gap-y-3 border-t border-border pt-6 text-sm text-muted-foreground">
              <span className="inline-flex items-center gap-2">
                <BookOpen className="size-4 text-primary" aria-hidden="true" />
                {pluralize(categories.length, 'category', 'categories')}
              </span>
              <span className="inline-flex items-center gap-2">
                <ShoppingBag className="size-4 text-primary" aria-hidden="true" />
                {loading ? 'Loading the shelf…' : pluralize(products.length, 'thing', 'things')} on the shelf
              </span>
            </div>
          </div>

          <div className="animate-rise delay-3 relative hidden min-h-[24rem] items-center justify-center lg:flex">
            <div className="absolute top-4 left-2 h-[21rem] w-[17rem] -rotate-6 rounded-2xl border border-border bg-card/80 shadow-lg" />
            <div className="absolute top-8 right-2 h-[20rem] w-[16rem] rotate-3 rounded-2xl border border-border bg-secondary/80" />

            <div className="relative w-full max-w-sm rotate-[-1.5deg] rounded-2xl border border-primary/20 bg-primary p-8 text-primary-foreground shadow-[0_40px_80px_-45px_rgba(31,77,58,0.65)]">
              <div className="flex items-center justify-between">
                <span className="text-[0.62rem] font-semibold tracking-[0.24em] uppercase opacity-70">
                  Shelf note · 01
                </span>
                <span className="grid size-9 place-items-center rounded-full border border-primary-foreground/30">
                  <ArrowDownRight className="size-4" aria-hidden="true" />
                </span>
              </div>

              <p className="mt-10 font-display text-[2.1rem] leading-[1.06] font-light tracking-[-0.03em]">
                “Buy less.
                <br />
                Choose well.
                <br />
                Keep it close.”
              </p>

              <div className="mt-10 flex items-end justify-between border-t border-primary-foreground/20 pt-5">
                <div>
                  <p className="text-[0.62rem] font-semibold tracking-[0.2em] uppercase opacity-60">
                    Mini Store
                  </p>
                  <p className="mt-1 text-xs opacity-80">Paper, ink &amp; good ideas</p>
                </div>
                <div className="grid size-16 rotate-[-10deg] place-items-center rounded-full border border-dashed border-accent text-center text-[0.55rem] font-semibold tracking-[0.14em] text-accent uppercase">
                  Est.
                  <br />
                  2026
                </div>
              </div>
            </div>

            <div className="absolute -right-3 bottom-2 rotate-6 rounded-xl border border-border bg-card px-5 py-4 shadow-md">
              <p className="text-[0.6rem] font-semibold tracking-[0.2em] text-muted-foreground uppercase">
                In your cart
              </p>
              <p className="mt-1 font-display text-lg font-semibold">
                {user ? 'A saved session' : 'Sign in to save'}
              </p>
            </div>
          </div>
        </div>
      </section>

      <section id="shelves" className="mx-auto w-full max-w-6xl scroll-mt-24 px-5 py-16 sm:px-8 lg:py-20">
        <div className="flex flex-col gap-6 border-b border-border pb-8 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="eyebrow">The shelves</p>
            <h2 className="mt-3 font-display text-4xl font-semibold tracking-[-0.03em] sm:text-5xl">
              What is on offer
            </h2>
            <p className="mt-3 max-w-xl text-muted-foreground">
              Two small shelves, changing slowly. Every price is shown in Indian rupees and every
              cart is tied to a signed-in customer.
            </p>
          </div>

          <div className="-mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
            <button
              type="button"
              onClick={() => setSelectedCategory('')}
              aria-pressed={selectedCategory === ''}
              className={cn(
                'shrink-0 rounded-full border px-4 py-2 text-sm font-semibold transition-colors',
                selectedCategory === ''
                  ? 'border-primary bg-primary text-primary-foreground'
                  : 'border-border bg-card text-muted-foreground hover:border-primary/30 hover:text-foreground',
              )}
            >
              Everything
            </button>
            {categories.map((category) => (
              <button
                key={category.id}
                type="button"
                onClick={() => setSelectedCategory(String(category.id))}
                aria-pressed={selectedCategory === String(category.id)}
                className={cn(
                  'shrink-0 rounded-full border px-4 py-2 text-sm font-semibold transition-colors',
                  selectedCategory === String(category.id)
                    ? 'border-primary bg-primary text-primary-foreground'
                    : 'border-border bg-card text-muted-foreground hover:border-primary/30 hover:text-foreground',
                )}
              >
                {category.name}
              </button>
            ))}
          </div>
        </div>

        {error ? (
          <div className="mt-8">
            <Alert variant="destructive">
              <RefreshCcw className="size-4" aria-hidden="true" />
              <AlertTitle>The shelf did not load</AlertTitle>
              <AlertDescription>
                <p>{error.message}</p>
                <Button variant="outline" size="sm" className="mt-3" onClick={retry}>
                  <RefreshCcw className="size-4" aria-hidden="true" />
                  Try again
                </Button>
              </AlertDescription>
            </Alert>
          </div>
        ) : loading ? (
          <div className="mt-10">
            <ProductGridSkeleton />
          </div>
        ) : products.length === 0 ? (
          <EmptyState
            className="mt-10"
            title="This shelf is empty"
            description="Try another category, or check back after the next delivery."
            action={
              <Button variant="outline" onClick={() => setSelectedCategory('')}>
                Show everything
              </Button>
            }
          />
        ) : (
          <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((product, index) => (
              <ProductCard
                key={product.id}
                product={product}
                index={index}
                categoryName={categoryNames.get(String(product.categoryId))}
                onAdd={addToCart}
                adding={addingId === product.id}
              />
            ))}
          </div>
        )}

        <div className="mt-14 flex flex-col items-start justify-between gap-5 rounded-2xl border border-border bg-card/70 p-6 sm:flex-row sm:items-center sm:p-8">
          <div className="flex items-start gap-4">
            <span className="grid size-11 shrink-0 place-items-center rounded-full bg-secondary">
              <Sparkles className="size-5 text-accent" aria-hidden="true" />
            </span>
            <div>
              <p className="font-display text-xl font-semibold">A cart that waits for you</p>
              <p className="mt-1 max-w-xl text-sm leading-relaxed text-muted-foreground">
                Registration creates your cart automatically. Sign in from another tab or after a
                refresh and the same server-side session is still there.
              </p>
            </div>
          </div>
          {user ? (
            <Button asChild variant="outline">
              <Link to="/cart">
                Open your cart
                <ArrowRight className="size-4" aria-hidden="true" />
              </Link>
            </Button>
          ) : (
            <Button asChild>
              <Link to="/register">
                Make an account
                <ArrowRight className="size-4" aria-hidden="true" />
              </Link>
            </Button>
          )}
        </div>
      </section>
    </div>
  )
}
