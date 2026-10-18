import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import {
  ArrowRight,
  Fingerprint,
  Loader2,
  LogOut,
  Mail,
  ShieldCheck,
  ShoppingBag,
  UserRound,
} from 'lucide-react'
import { Badge } from '@/components/ui/badge'
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
import { useAuth } from '@/context/AuthContext'
import { cartItemCount, getOrCreateCart } from '@/lib/cart'
import { formatPrice, initials } from '@/lib/format'

export function AccountPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [cart, setCart] = useState(null)
  const [cartLoading, setCartLoading] = useState(true)
  const [loggingOut, setLoggingOut] = useState(false)

  useEffect(() => {
    let active = true

    getOrCreateCart()
      .then((result) => {
        if (active) {
          setCart(result)
        }
      })
      .catch(() => {
        if (active) {
          setCart(null)
        }
      })
      .finally(() => {
        if (active) {
          setCartLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [])

  async function handleLogout() {
    setLoggingOut(true)
    try {
      await logout()
      toast.success('Signed out.', { description: 'Your cart is still saved for next time.' })
    } catch (error) {
      toast.error(error.message || 'The logout request failed, but this browser is signed out.')
    } finally {
      setLoggingOut(false)
      navigate('/', { replace: true })
    }
  }

  const itemCount = cartItemCount(cart)

  return (
    <div className="mx-auto w-full max-w-6xl px-5 py-14 sm:px-8 lg:py-20">
      <div className="border-b border-border pb-8">
        <p className="eyebrow">Your account · Customer {user?.id}</p>
        <h1 className="mt-3 font-display text-4xl font-semibold tracking-[-0.03em] sm:text-5xl">
          Good to see you, {user?.name?.split(' ')[0] || 'customer'}.
        </h1>
        <p className="mt-3 max-w-2xl text-muted-foreground">
          This page is rendered from the customer stored in your server-side session. A browser
          refresh asks the backend who you are; it does not trust a customer ID kept in the page.
        </p>
      </div>

      <div className="mt-10 grid items-start gap-6 lg:grid-cols-[1.1fr_0.9fr]">
        <Card className="overflow-hidden rounded-2xl">
          <div className="grain relative bg-primary px-6 py-8 text-primary-foreground">
            <div className="pointer-events-none absolute -top-10 -right-8 size-40 rounded-full border border-primary-foreground/20" />
            <div className="relative flex items-center gap-4">
              <span className="grid size-16 place-items-center rounded-2xl border border-primary-foreground/25 bg-primary-foreground/10 font-display text-2xl font-semibold">
                {initials(user?.name)}
              </span>
              <div className="min-w-0">
                <p className="text-[0.62rem] font-semibold tracking-[0.24em] uppercase opacity-60">
                  Signed in as
                </p>
                <p className="mt-1 truncate font-display text-2xl font-semibold">{user?.name}</p>
                <p className="mt-1 truncate text-sm text-primary-foreground/70">{user?.email}</p>
              </div>
            </div>
            <div className="relative mt-6">
              <Badge className="border border-primary-foreground/20 bg-primary-foreground/10 text-primary-foreground">
                <span className="mr-1 inline-block size-1.5 rounded-full bg-accent" />
                Active session
              </Badge>
            </div>
          </div>

          <CardContent className="space-y-4 pt-6">
            <div className="flex items-start gap-3">
              <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-secondary">
                <UserRound className="size-4 text-primary" aria-hidden="true" />
              </span>
              <div>
                <p className="text-xs font-semibold tracking-[0.14em] text-muted-foreground uppercase">
                  Customer number
                </p>
                <p className="mt-1 font-mono text-sm">#{user?.id}</p>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-secondary">
                <Mail className="size-4 text-primary" aria-hidden="true" />
              </span>
              <div className="min-w-0">
                <p className="text-xs font-semibold tracking-[0.14em] text-muted-foreground uppercase">
                  Email
                </p>
                <p className="mt-1 truncate text-sm">{user?.email}</p>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-secondary">
                <Fingerprint className="size-4 text-primary" aria-hidden="true" />
              </span>
              <div>
                <p className="text-xs font-semibold tracking-[0.14em] text-muted-foreground uppercase">
                  Identity source
                </p>
                <p className="mt-1 text-sm">HttpSession attribute, set at sign-in</p>
              </div>
            </div>
          </CardContent>

          <CardFooter className="border-t border-border/70 pt-5">
            <Button
              type="button"
              variant="outline"
              className="w-full text-destructive hover:border-destructive/30 hover:bg-destructive/5 hover:text-destructive"
              onClick={handleLogout}
              disabled={loggingOut}
            >
              {loggingOut ? (
                <Loader2 className="size-4 animate-spin" aria-hidden="true" />
              ) : (
                <LogOut className="size-4" aria-hidden="true" />
              )}
              {loggingOut ? 'Signing out…' : 'Sign out'}
            </Button>
          </CardFooter>
        </Card>

        <div className="space-y-6">
          <Card className="rounded-2xl">
            <CardHeader>
              <div className="flex items-center justify-between gap-3">
                <div>
                  <p className="eyebrow">Saved cart</p>
                  <CardTitle className="mt-2 text-2xl">On the counter</CardTitle>
                </div>
                <span className="grid size-10 place-items-center rounded-full bg-secondary">
                  <ShoppingBag className="size-5 text-primary" aria-hidden="true" />
                </span>
              </div>
            </CardHeader>

            <CardContent>
              {cartLoading ? (
                <div className="space-y-3" aria-hidden="true">
                  <Skeleton className="h-5 w-28" />
                  <Skeleton className="h-5 w-20" />
                  <Skeleton className="h-9 w-36" />
                </div>
              ) : cart ? (
                <div className="space-y-3">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Items</span>
                    <span className="font-mono">{itemCount}</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Lines</span>
                    <span className="font-mono">{cart.items?.length || 0}</span>
                  </div>
                  <Separator />
                  <div className="flex items-end justify-between">
                    <span className="text-sm text-muted-foreground">Current total</span>
                    <span className="font-display text-2xl font-semibold">
                      {formatPrice(cart.totalPrice)}
                    </span>
                  </div>
                </div>
              ) : (
                <p className="text-sm leading-relaxed text-muted-foreground">
                  We could not load your cart right now. It is still attached to your account.
                </p>
              )}
            </CardContent>

            <CardFooter className="border-t border-border/70 pt-5">
              <Button asChild variant="outline" className="w-full">
                <Link to="/cart">
                  Open the cart
                  <ArrowRight className="size-4" aria-hidden="true" />
                </Link>
              </Button>
            </CardFooter>
          </Card>

          <Card className="rounded-2xl bg-secondary/45">
            <CardHeader>
              <div className="flex items-center gap-3">
                <span className="grid size-10 place-items-center rounded-full bg-card">
                  <ShieldCheck className="size-5 text-primary" aria-hidden="true" />
                </span>
                <div>
                  <CardTitle className="text-xl">How the session works</CardTitle>
                  <CardDescription>Small decisions, written down.</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <ul className="space-y-3 text-sm leading-relaxed text-muted-foreground">
                <li className="flex gap-3">
                  <span className="mt-1.5 size-1.5 shrink-0 rounded-full bg-accent" />
                  Your session ID lives in an HttpOnly cookie. JavaScript cannot read it.
                </li>
                <li className="flex gap-3">
                  <span className="mt-1.5 size-1.5 shrink-0 rounded-full bg-accent" />
                  The session ID is rotated when you sign in, and the session is invalidated on sign
                  out.
                </li>
                <li className="flex gap-3">
                  <span className="mt-1.5 size-1.5 shrink-0 rounded-full bg-accent" />
                  The backend derives your customer from the session, never from a customer ID sent
                  by the page.
                </li>
                <li className="flex gap-3">
                  <span className="mt-1.5 size-1.5 shrink-0 rounded-full bg-accent" />
                  No passwords or session identifiers are kept in localStorage.
                </li>
              </ul>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
