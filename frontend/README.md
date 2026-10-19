<!-- member 4 -->
# Mini Store frontend

React 19 + Vite + Tailwind CSS + shadcn/ui components for the Mini Store backend.

## Run

Start the Spring Boot API first (default `http://localhost:8080`), then:

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. The Vite dev proxy sends `/api/*` to the backend and strips the `/api`
prefix, so the browser sees a same-origin `JSESSIONID` cookie.

If the API runs on another port:

```powershell
$env:VITE_API_PROXY_TARGET = 'http://localhost:18080'
npm run dev
```

## Scripts

| Command | What it does |
|---|---|
| `npm run dev` | Vite development server with `/api` proxy. |
| `npm run build` | Production build into `dist/`. |
| `npm run preview` | Serve the production build locally. |
| `npm run lint` | Run Oxlint. |

The scripts pass `--configLoader native` to Vite. The config is plain JavaScript, and the native
loader avoids a Windows subprocess edge case in Vite's default config bundler.

## Configuration

Copy `.env.example` to `.env` if you need overrides:

- `VITE_API_BASE_URL` — browser API prefix. Default `/api` (for the dev proxy).
- `VITE_API_PROXY_TARGET` — dev proxy destination. Default `http://localhost:8080`.

For a separately hosted build, set `VITE_API_BASE_URL` to the full backend URL and add the
frontend origin to the backend's `CORS_ALLOWED_ORIGINS`.

## Authentication model

- `AuthContext` calls `GET /auth/me` on mount. A 401 means anonymous; a 200 restores the customer.
- Registration and login use `POST /auth/register` and `/auth/login`; logout uses `/auth/logout`.
- The `fetch` wrapper always sends `credentials: 'include'`.
- Before an unsafe request, `lib/api.js` reads the readable `XSRF-TOKEN` cookie and sends it as
  `X-XSRF-TOKEN`. `GET /auth/csrf` seeds the token when needed. A 403 triggers one token refresh
  and retry.
- `JSESSIONID` is HttpOnly, so the UI never reads it. Passwords and session IDs are never written to
  `localStorage` or `sessionStorage`.

## Layout

```text
src/
  components/ui/        shadcn/ui primitives (Button, Input, Card, Alert, Badge, Skeleton, Sonner, ...)
  components/site/      SiteLayout, AuthLayout, ProductCard, QuantityStepper, RequireAuth, EmptyState,
                        ChatWidget, ProductReviews, StarRating, AiText
  context/AuthContext.jsx
  lib/api.js            fetch + CSRF handling
  lib/cart.js           get-or-create cart
  lib/format.js         INR price formatting and small helpers
  lib/chat.js           POST /chat wrapper (shop assistant)
  lib/reviews.js        review list, review creation and summary wrappers
  pages/                ShopPage, ProductPage, LoginPage, RegisterPage, CartPage, AccountPage, NotFoundPage
  App.jsx               routes and providers
  index.css             Tailwind v4 theme, paper textures, motion
```

## UI notes

- The shop filters through the existing `GET /products?categoryId=` endpoint and loads
  `GET /categories` for the shelf filters.
- Adding to cart uses `POST /me/cart/items`; the backend derives the customer from the session.
- The cart page updates quantities with `PUT /me/cart/items/{productId}` and removes with `DELETE`.
- Sonner provides toasts; Skeleton provides loading states for the product grid, cart, and account.
- The visual system uses Fraunces for display type and Instrument Sans for body text, with a warm
  paper/forest/terracotta palette. Motion is CSS-only and respects `prefers-reduced-motion`.
