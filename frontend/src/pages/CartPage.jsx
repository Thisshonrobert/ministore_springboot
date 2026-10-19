//member 3
import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import {
  ArrowLeft,
  ArrowRight,
  Loader2,
  RefreshCcw,
  ShoppingBag,
  Trash2,
} from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/site/EmptyState'
import { QuantityStepper } from '@/components/site/QuantityStepper'
import { useAuth } from '@/context/AuthContext'
import { api } from '@/lib/api'
import { cartItemCount, getOrCreateCart } from '@/lib/cart'
import { formatPrice, pluralize } from '@/lib/format'

function CartSkeleton() {
  return (
    <div className="mx-auto grid w-full max-w-6xl gap-8 px-5 py-14 sm:px-8 lg:grid-cols-[1.55fr_0.85fr]">
      <div className="space-y-5">
        <Skeleton className="h-4 w-24" />
        <Skeleton className="h-12 w-64" />
        <Skeleton className="h-72 w-full rounded-2xl" />
      </div>
      <Skeleton className="h-72 w-full rounded-2xl" />
    </div>
  )
}

export function CartPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [busyItem, setBusyItem] = useState(null)
  const [clearing, setClearing] = useState(false)

  const loadCart = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await getOrCreateCart()
      setCart(result)
    } catch (requestError) {
      setError(requestError)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadCart()
  }, [loadCart])

  async function refreshCart() {
    const result = await api('/me/cart')
    setCart(result)
    return result
  }

  async function updateQuantity(item, quantity) {
    const productId = item.product.id
    if (quantity < 1 || quantity > 100 || quantity === item.quantity) {
      return
    }

    setBusyItem(productId)
    try {
      await api(`/me/cart/items/${productId}`, { method: 'PUT', body: { quantity } })
      await refreshCart()
    } catch (requestError) {
      toast.error(requestError.message || 'Could not update that quantity.')
    } finally {
      setBusyItem(null)
    }
  }

  async function removeItem(item) {
    const productId = item.product.id
    setBusyItem(productId)
    try {
      await api(`/me/cart/items/${productId}`, { method: 'DELETE' })
      await refreshCart()
      toast.success(`${item.product.name} removed from your cart.`)
    } catch (requestError) {
      toast.error(requestError.message || 'Could not remove that item.')
    } finally {
      setBusyItem(null)
    }
  }

  async function clearCart() {
    setClearing(true)
    try {
      await api('/me/cart/items', { method: 'DELETE' })
      await refreshCart()
      toast.success('Your cart is empty again.')
    } catch (requestError) {
      toast.error(requestError.message || 'Could not clear your cart.')
    } finally {
      setClearing(false)
    }
  }

  async function handleExpiredSession() {
    try {
      await logout()
    } catch {
      // The local auth state is cleared even when the session was already gone.
    }
    navigate('/login', { replace: true, state: { from: '/cart' } })
  }

  if (loading) {
    return <CartSkeleton />
  }

  if (error) {
    const expired = error.status === 401 || error.status === 403
    return (
      <div className="mx-auto w-full max-w-3xl px-5 py-20 sm:px-8">
        <Alert variant="destructive">
          <RefreshCcw className="size-4" aria-hidden="true" />
          <AlertTitle>{expired ? 'Your session ended' : 'The cart did not load'}</AlertTitle>
          <AlertDescription>
            <p>{expired ? 'Please sign in again to reach your saved cart.' : error.message}</p>
            <div className="mt-4 flex flex-wrap gap-2">
              {expired ? (
                <Button onClick={handleExpiredSession}>Sign in again</Button>
              ) : (
                <Button variant="outline" onClick={loadCart}>
                  <RefreshCcw className="size-4" aria-hidden="true" />
                  Try again
                </Button>
              )}
              <Button asChild variant="ghost">
                <Link to="/">Back to the shelves</Link>
              </Button>
            </div>
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  const items = cart?.items || []
  const itemCount = cartItemCount(cart)

  return (
    <div className="mx-auto w-full max-w-6xl px-5 py-14 sm:px-8 lg:py-20">
      <div className="flex flex-col gap-5 border-b border-border pb-8 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="eyebrow">Your cart · {user?.name}</p>
          <h1 className="mt-3 font-display text-4xl font-semibold tracking-[-0.03em] sm:text-5xl">
            A little haul.
          </h1>
          <p className="mt-3 max-w-xl text-muted-foreground">
            {items.length === 0
              ? 'Nothing on the counter yet. The shelves are just a short walk away.'
              : `${pluralize(items.length, 'line', 'lines')} and ${pluralize(itemCount, 'item')} in your cart. Prices use the current store shelf.`}
          </p>
        </div>
        <Button asChild variant="outline">
          <Link to="/">
            <ArrowLeft className="size-4" aria-hidden="true" />
            Keep browsing
          </Link>
        </Button>
      </div>

      {items.length === 0 ? (
        <EmptyState
          className="mt-10"
          icon={ShoppingBag}
          title="The cart is empty"
          description="Add a notebook, a pen, or something to read. Your cart is saved to your account."
          action={
            <Button asChild size="lg">
              <Link to="/">
                Browse the shelves
                <ArrowRight className="size-4" aria-hidden="true" />
              </Link>
            </Button>
          }
        />
      ) : (
        <div className="mt-10 grid items-start gap-8 lg:grid-cols-[1.55fr_0.85fr]">
          <Card className="overflow-hidden rounded-2xl">
            <CardHeader className="border-b border-border/70 pb-5">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <CardTitle className="text-2xl">On the counter</CardTitle>
                  <CardDescription className="mt-1">
                    Quantities can be changed from 1 to 100.
                  </CardDescription>
                </div>
                <span className="font-mono text-xs text-muted-foreground">
                  {String(itemCount).padStart(2, '0')} {itemCount === 1 ? 'item' : 'items'}
                </span>
              </div>
            </CardHeader>

            <CardContent className="p-0">
              {items.map((item, index) => {
                const busy = busyItem === item.product.id
                return (
                  <div key={item.product.id}>
                    {index > 0 && <div className="dashed-rule mx-6 h-px" aria-hidden="true" />}
                    <div className="flex flex-col gap-4 p-6 sm:flex-row sm:items-center sm:justify-between">
                      <div className="flex min-w-0 items-start gap-4">
                        <span className="grid size-11 shrink-0 place-items-center rounded-lg bg-secondary font-display text-lg font-semibold">
                          {item.product.name.slice(0, 1).toUpperCase()}
                        </span>
                        <div className="min-w-0">
                          <p className="truncate font-display text-lg font-semibold">
                            {item.product.name}
                          </p>
                          <p className="mt-0.5 text-sm text-muted-foreground">
                            {formatPrice(item.product.price)} each
                          </p>
                        </div>
                      </div>

                      <div className="flex flex-wrap items-center justify-between gap-4 sm:justify-end">
                        <QuantityStepper
                          value={item.quantity}
                          onChange={(quantity) => updateQuantity(item, quantity)}
                          disabled={busy}
                        />
                        <span className="min-w-24 text-right font-mono text-sm font-semibold">
                          {busy ? (
                            <Loader2 className="ml-auto size-4 animate-spin" aria-label="Updating" />
                          ) : (
                            formatPrice(item.totalPrice)
                          )}
                        </span>
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon"
                          className="text-muted-foreground hover:text-destructive"
                          onClick={() => removeItem(item)}
                          disabled={busy}
                          aria-label={`Remove ${item.product.name}`}
                        >
                          <Trash2 className="size-4" aria-hidden="true" />
                        </Button>
                      </div>
                    </div>
                  </div>
                )
              })}
            </CardContent>

            <CardFooter className="justify-between gap-3 border-t border-border/70 pt-5">
              <Button
                type="button"
                variant="ghost"
                size="sm"
                className="text-muted-foreground hover:text-destructive"
                onClick={clearCart}
                disabled={clearing}
              >
                {clearing ? (
                  <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                ) : (
                  <Trash2 className="size-4" aria-hidden="true" />
                )}
                Clear the cart
              </Button>
              <span className="text-xs text-muted-foreground">Items are saved to your account.</span>
            </CardFooter>
          </Card>

          <Card className="sticky top-24 rounded-2xl border-primary/15 bg-primary text-primary-foreground">
            <CardHeader>
              <p className="text-[0.62rem] font-semibold tracking-[0.24em] uppercase opacity-65">
                Counter slip · Mini Store
              </p>
              <CardTitle className="mt-2 text-3xl text-primary-foreground">Your total</CardTitle>
              <CardDescription className="text-primary-foreground/65">
                A simple sum of the current shelf prices.
              </CardDescription>
            </CardHeader>

            <CardContent className="space-y-4 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-primary-foreground/70">Items</span>
                <span className="font-mono">{itemCount}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-primary-foreground/70">Lines</span>
                <span className="font-mono">{items.length}</span>
              </div>
              <Separator className="bg-primary-foreground/20" />
              <div className="flex items-end justify-between">
                <span className="text-primary-foreground/70">Total</span>
                <span className="font-display text-3xl font-light">
                  {formatPrice(cart.totalPrice)}
                </span>
              </div>
              <p className="rounded-lg border border-primary-foreground/15 bg-primary-foreground/5 p-3 text-xs leading-relaxed text-primary-foreground/65">
                Checkout is not part of this learning build. Your cart stays here until you remove
                an item or clear it.
              </p>
            </CardContent>

            <CardFooter className="flex flex-col gap-2">
              <Button asChild variant="secondary" className="w-full">
                <Link to="/">
                  Add something else
                  <ArrowRight className="size-4" aria-hidden="true" />
                </Link>
              </Button>
              <Button
                type="button"
                variant="ghost"
                className="w-full text-primary-foreground/70 hover:bg-primary-foreground/10 hover:text-primary-foreground"
                onClick={clearCart}
                disabled={clearing}
              >
                Empty the cart
              </Button>
            </CardFooter>
          </Card>
        </div>
      )}
    </div>
  )
}
