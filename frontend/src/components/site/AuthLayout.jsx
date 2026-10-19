//member 3
import { Link, Outlet } from 'react-router-dom'
import { ArrowLeft, Feather, LockKeyhole, Sparkles } from 'lucide-react'

export function AuthLayout() {
  return (
    <div className="grid min-h-svh lg:grid-cols-[1.02fr_0.98fr]">
      <aside className="grain relative hidden flex-col justify-between overflow-hidden bg-primary px-10 py-12 text-primary-foreground lg:flex xl:px-16">
        <div className="pointer-events-none absolute inset-0 opacity-20">
          <div className="absolute -top-24 -left-20 size-96 rounded-full border border-primary-foreground/30" />
          <div className="absolute top-1/3 -right-28 size-[28rem] rounded-full border border-primary-foreground/20" />
          <div className="absolute bottom-10 left-12 size-52 rounded-full border border-dashed border-primary-foreground/30" />
        </div>

        <div className="relative flex items-center gap-3">
          <span className="grid size-11 place-items-center rounded-xl bg-primary-foreground text-primary">
            <span className="font-display text-2xl leading-none">M</span>
          </span>
          <div className="leading-none">
            <p className="font-display text-xl font-semibold">Mini Store</p>
            <p className="mt-1 text-[0.62rem] font-semibold tracking-[0.24em] uppercase opacity-70">
              Paper &amp; provisions
            </p>
          </div>
        </div>

        <div className="relative max-w-lg space-y-7">
          <p className="text-[0.68rem] font-semibold tracking-[0.26em] text-primary-foreground/60 uppercase">
            The shop journal · N° 02
          </p>
          <h1 className="font-display text-5xl leading-[1.02] font-light tracking-[-0.035em] xl:text-6xl">
            A small shop for paper, ink, and the ideas in between.
          </h1>
          <p className="max-w-md text-base leading-relaxed text-primary-foreground/75">
            Sign in to keep your cart, come back to it later, and see exactly what is waiting on
            the shelf.
          </p>
        </div>

        <div className="relative grid gap-3 text-sm text-primary-foreground/80 sm:grid-cols-3">
          <div className="rounded-lg border border-primary-foreground/15 bg-primary-foreground/5 p-4">
            <LockKeyhole className="mb-3 size-4 text-accent" aria-hidden="true" />
            <p className="font-semibold text-primary-foreground">Hashed, not held</p>
            <p className="mt-1 text-xs leading-relaxed text-primary-foreground/60">PBKDF2 passwords</p>
          </div>
          <div className="rounded-lg border border-primary-foreground/15 bg-primary-foreground/5 p-4">
            <Feather className="mb-3 size-4 text-accent" aria-hidden="true" />
            <p className="font-semibold text-primary-foreground">A light touch</p>
            <p className="mt-1 text-xs leading-relaxed text-primary-foreground/60">Simple, explicit code</p>
          </div>
          <div className="rounded-lg border border-primary-foreground/15 bg-primary-foreground/5 p-4">
            <Sparkles className="mb-3 size-4 text-accent" aria-hidden="true" />
            <p className="font-semibold text-primary-foreground">Your cart stays</p>
            <p className="mt-1 text-xs leading-relaxed text-primary-foreground/60">Server-side session</p>
          </div>
        </div>
      </aside>

      <div className="paper-texture flex min-h-svh flex-col">
        <div className="flex items-center justify-between border-b border-border/70 px-5 py-5 sm:px-8 lg:border-0">
          <Link
            to="/"
            className="inline-flex items-center gap-2 text-sm font-semibold text-muted-foreground transition-colors hover:text-foreground"
          >
            <ArrowLeft className="size-4" aria-hidden="true" />
            Back to the shop
          </Link>
          <Link to="/" className="flex items-center gap-2 lg:hidden">
            <span className="grid size-8 place-items-center rounded-lg bg-primary text-primary-foreground">
              <span className="font-display text-base leading-none">M</span>
            </span>
            <span className="font-display text-lg font-semibold">Mini Store</span>
          </Link>
        </div>

        <div className="flex flex-1 items-center justify-center px-5 py-10 sm:px-8 lg:py-16">
          <div className="w-full max-w-md">
            <Outlet />
          </div>
        </div>

        <div className="px-5 pb-8 text-center text-xs text-muted-foreground sm:px-8">
          <span className="mr-2 inline-block size-1.5 rounded-full bg-accent align-middle" />
          Sessions are kept in an HttpOnly cookie, never in localStorage.
        </div>
      </div>
    </div>
  )
}
