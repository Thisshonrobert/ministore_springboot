import { useEffect } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { ArrowUpRight, ShoppingBag, Store, UserRound } from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import { cn } from '@/lib/utils'

function ScrollToTop() {
  const { pathname } = useLocation()

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'auto' })
  }, [pathname])

  return null
}

const navLinkClass = ({ isActive }) =>
  cn(
    'relative inline-flex items-center gap-2 rounded-full px-3.5 py-2 text-sm font-semibold transition-colors',
    isActive ? 'bg-secondary text-secondary-foreground' : 'text-muted-foreground hover:text-foreground',
  )

const currentYear = new Date().getFullYear()

export function SiteLayout() {
  const { user } = useAuth()

  return (
    <div className="flex min-h-svh flex-col">
      <ScrollToTop />

      <div className="overflow-hidden border-b border-primary/20 bg-primary text-primary-foreground">
        <div className="marquee-track flex w-max items-center gap-8 py-2 text-[0.65rem] font-semibold tracking-[0.28em] uppercase">
          {Array.from({ length: 2 }).map((_, group) => (
            <div key={group} className="flex items-center gap-8" aria-hidden={group === 1}>
              <span>Paper goods</span>
              <span className="text-accent">✦</span>
              <span>Everyday carry</span>
              <span className="text-accent">✦</span>
              <span>Good ideas</span>
              <span className="text-accent">✦</span>
              <span>Made for the desk</span>
              <span className="text-accent">✦</span>
              <span>Stationery & books</span>
              <span className="text-accent">✦</span>
            </div>
          ))}
        </div>
      </div>

      <header className="sticky top-0 z-40 border-b border-border/80 bg-background/88 backdrop-blur-md">
        <div className="mx-auto flex h-[4.75rem] w-full max-w-6xl items-center justify-between gap-4 px-5 sm:px-8">
          <Link to="/" className="group flex items-center gap-3" aria-label="Mini Store home">
            <span className="grid size-11 place-items-center rounded-xl bg-primary text-primary-foreground shadow-sm transition-transform duration-300 group-hover:-rotate-3">
              <span className="font-display text-2xl leading-none">M</span>
            </span>
            <span className="flex flex-col leading-none">
              <span className="font-display text-xl font-semibold tracking-tight">Mini Store</span>
              <span className="mt-1 hidden text-[0.62rem] font-semibold tracking-[0.22em] text-muted-foreground uppercase sm:block">
                Paper &amp; provisions
              </span>
            </span>
          </Link>

          <nav className="flex items-center gap-1" aria-label="Main navigation">
            <NavLink to="/" end className={navLinkClass}>
              <Store className="size-4" aria-hidden="true" />
              <span className="hidden sm:inline">Shop</span>
            </NavLink>
            <NavLink to="/cart" className={navLinkClass}>
              <ShoppingBag className="size-4" aria-hidden="true" />
              <span className="hidden sm:inline">Cart</span>
            </NavLink>
            <NavLink to={user ? '/account' : '/login'} className={navLinkClass}>
              <UserRound className="size-4" aria-hidden="true" />
              <span className="hidden sm:inline">{user ? 'Account' : 'Sign in'}</span>
            </NavLink>
            {!user && (
              <Link
                to="/register"
                className="ml-1 hidden items-center gap-1.5 rounded-full bg-accent px-4 py-2 text-sm font-semibold text-accent-foreground shadow-sm transition-transform hover:-translate-y-0.5 hover:bg-accent/90 md:inline-flex"
              >
                Join the shop
                <ArrowUpRight className="size-4" aria-hidden="true" />
              </Link>
            )}
          </nav>
        </div>
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t border-border bg-card/60">
        <div className="mx-auto grid w-full max-w-6xl gap-10 px-5 py-12 sm:px-8 md:grid-cols-[1.4fr_1fr_1fr]">
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <span className="grid size-10 place-items-center rounded-lg bg-primary text-primary-foreground">
                <span className="font-display text-xl leading-none">M</span>
              </span>
              <span className="font-display text-xl font-semibold">Mini Store</span>
            </div>
            <p className="max-w-sm text-sm leading-relaxed text-muted-foreground">
              A small learning shop for paper, ink, and good ideas. Built with a Spring Boot API,
              a React storefront, and a cookie-based session.
            </p>
          </div>

          <div className="space-y-3 text-sm">
            <p className="eyebrow">Browse</p>
            <div className="flex flex-col gap-2 text-muted-foreground">
              <Link to="/" className="transition-colors hover:text-foreground">The shelves</Link>
              <Link to="/cart" className="transition-colors hover:text-foreground">Your cart</Link>
              <Link to={user ? '/account' : '/login'} className="transition-colors hover:text-foreground">
                {user ? 'Your account' : 'Sign in'}
              </Link>
            </div>
          </div>

          <div className="space-y-3 text-sm">
            <p className="eyebrow">Good to know</p>
            <p className="leading-relaxed text-muted-foreground">
              Passwords are hashed with PBKDF2. Sessions live in an HttpOnly cookie and never in
              localStorage.
            </p>
            <p className="text-xs text-muted-foreground/80">Spring Boot 3.4 · React 19</p>
          </div>
        </div>
        <div className="border-t border-border/70">
          <div className="mx-auto flex w-full max-w-6xl flex-col gap-2 px-5 py-5 text-xs text-muted-foreground sm:flex-row sm:items-center sm:justify-between sm:px-8">
            <span>© {currentYear} Mini Store. A learning project.</span>
            <span className="font-mono tracking-tight">curl -c cookies.txt /auth/csrf</span>
          </div>
        </div>
      </footer>
    </div>
  )
}
