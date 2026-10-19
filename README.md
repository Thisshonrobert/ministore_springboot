<!-- member 4 -->
# Mini Store

A four-member learning project: one Java 17 / Spring Boot 3.4.1 application, MySQL 8, an
explicit-Java REST API, and a React 19 storefront. There is no Spring Security, Lombok, MapStruct,
or Thymeleaf.

Phase 1 implemented categories, products, customer records, and one customer-linked cart.
Phase 2 adds customer registration, login, logout, session persistence, CSRF protection, and a
React interface for the shop, cart, and account.
Phase 3 adds customer reviews and two AI features: a shopping assistant and an AI summary of a
product's reviews. Both call Google Gemini from the backend only; the browser never sees a key.
See `docs/ai-features.md`.

## What is implemented

- Category CRUD; product CRUD and category filtering.
- Customer records with unique, lower-cased email addresses and shared JSON validation errors.
- One cart per customer, cart items, quantity limits, totals, clear/remove, and pessimistic row
  locking on cart mutations.
- Customer authentication with PBKDF2-HMAC-SHA256 password hashes.
- Server-side `HttpSession` login state, session-ID rotation, and logout invalidation.
- A CSRF synchronizer token plus a credentialed CORS configuration for the React client.
- Session-derived `/me` cart endpoints that never trust a customer ID from the browser.
- React 19 + Vite + Tailwind CSS + shadcn/ui components: shop, category filters, product detail page,
  cart, registration, login, account details, logout, skeleton loading states, and toast
  notifications.
- Customer reviews: one rating and written review per signed-in customer per product, listed on the
  product page.
- A grounded AI shop assistant (`POST /chat`) that answers from products read out of the database and
  returns those same products to the storefront.
- An AI summary of a product's real reviews (`POST /products/{id}/reviews/summary`), cached per
  product and cleared whenever a new review arrives.
- The customer API is session-based: `/auth/*` handles registration, login and logout, `/me/cart`
  exposes the signed-in customer's cart, and `/me/reviews` writes reviews for the session customer.
  The legacy `/users` and `/carts` management endpoints were removed.

## Run on this machine

### 1. Database

The project uses its own `mini_store` database. The dedicated `mini_store_app` account needs
`SELECT, INSERT, UPDATE, DELETE` on `mini_store.*`; it does not need `ALTER` after the one-time
authentication migration. Local database details are in `docs/verification.md`; `.env` is private
and already ignored by Git.

For an existing database created before Phase 2, run `database/auth-migration.sql` **once** with a
MySQL administrator account (for example, in MySQL Workbench: File → Open SQL Script →
`database/auth-migration.sql` → execute). It is idempotent and adds the nullable
`users.password_hash` column. The same script also gives the two seeded demo customers a password
only when their hash is still empty.

For a fresh database, `database/setup.sql` already includes `password_hash`; the migration remains
safe to run. Recommended order:

```text
setup.sql  ->  ai-migration.sql  ->  seed.sql (optional)  ->  auth-migration.sql
```

`database/ai-migration.sql` adds the `reviews` and `review_summary` tables used by the review and AI
summary features. It is idempotent, so it is safe to run more than once. It runs before `seed.sql`
because the seed now inserts demo reviews, which need those tables.

If `seed.sql` creates the demo customers after `auth-migration.sql` was run, run the migration again;
it only sets a password while a demo hash is still empty. The demo password is:

| Customer | Email | Password |
|---|---|---|
| Demo Customer | `demo@ministore.example` | `StoreDemo123!` |
| Team Customer | `team@ministore.example` | `StoreDemo123!` |

These are local demo credentials only. The migration stores them as salted PBKDF2 hashes, never as
plaintext. New registrations from the UI choose their own password.

### 2. Backend

```powershell
cd C:\Users\thiss\Desktop\dev\sprinboot_mini_store
.\mvnw.cmd spring-boot:run
```

The REST API is at http://localhost:8080. The app reads `.env` automatically. To use another port,
edit `SERVER_PORT` in `.env`. Stop with Ctrl+C.

### 3. Frontend

Node.js 20.19+ (or 22.12+) and npm are required. This project was verified with Node.js 24.20.0.

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Vite proxies `/api` to `http://localhost:8080`, so the session cookie
is same-origin during development. If the backend runs on another port:

```powershell
$env:VITE_API_PROXY_TARGET = 'http://localhost:18080'
npm run dev
```

Production build and preview:

```powershell
npm run build
npm run preview
```

`npm run build` uses Vite's native config loader (`--configLoader native`) to avoid a Windows
subprocess edge case in the config bundler. To host the built frontend separately, set
`VITE_API_BASE_URL` to the backend URL, add the frontend origin to `CORS_ALLOWED_ORIGINS` on the
backend, and serve the SPA with a fallback to `index.html`.

### 4. CORS and cookies

Defaults allow `http://localhost:5173`, `http://127.0.0.1:5173`, and the corresponding port 4173
preview origins. Override with a comma-separated `CORS_ALLOWED_ORIGINS` environment variable.
`allowCredentials` is enabled, so wildcard origins are deliberately not used.

The `JSESSIONID` cookie is `HttpOnly` and `SameSite=Lax`, and is marked `Secure` only when
`SESSION_COOKIE_SECURE=true`; a deployed HTTPS backend must set that variable. The CSRF cookie is
intentionally readable by JavaScript because the React client must copy it into the `X-XSRF-TOKEN`
header.
Because `SameSite=Lax` cookies are site-scoped, use the same hostname (normally `localhost`) for
both the page and the API in direct mode. The Vite development proxy avoids this issue entirely by
making `/api` same-origin.

### 5. AI features

The shop assistant and the review summary call Google Gemini from the backend. Set `AI_API_KEY` in
`.env` (from Google AI Studio); it is read by the server only and is never sent to the browser.
Optional overrides: `AI_MODEL` (default `gemini-3.5-flash`), `AI_BASE_URL`, `AI_TIMEOUT_SECONDS`,
`AI_MAX_OUTPUT_TOKENS`, `AI_THINKING_BUDGET`, plus `REVIEWS_MINIMUM_FOR_SUMMARY` and
`REVIEW_SUMMARY_TTL_HOURS` for summaries.

The app starts without a key. When the key is missing, rejected, or out of quota, `POST /chat` and
the summary endpoint return 503 with a clear message while the rest of the store keeps working.
`frontend/.env.example` intentionally has no AI setting: all provider calls go through the backend.

With the key that was available while this feature was built, every live Gemini call returned 403
"project denied access", 429 with a quota limit of 0, or 404 for retired models, so live answers could
not be recorded. A later key with `AI_MODEL=gemini-3.1-flash-lite` now works: on 19 October 2026 the
live review summary was generated, cached and read back for the seeded Notebook (4 reviews, average
4.0). The endpoint behaviour is also covered by tests against a stubbed provider, so the suite never
needs a key. Details are in `docs/ai-features.md` and `docs/verification.md`.

## Set up MySQL on another machine (Workbench)

Workbench is the client; MySQL Server must also be installed and running locally.

1. Connect to your local MySQL server in Workbench with an administrator account.
2. File → Open SQL Script → `database/setup.sql`, then execute the entire script using the
   lightning button.
3. Refresh Schemas. Confirm `mini_store` contains `categories`, `products`, `users`, `carts`,
   `cart_item`, `reviews`, `review_summary`, and that `users` has a nullable `password_hash` column.
4. For a dedicated account, execute this SQL with your chosen password (skip `CREATE USER` if the
   account already exists):

```sql
CREATE USER 'mini_store_app'@'localhost' IDENTIFIED BY 'your_chosen_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON mini_store.* TO 'mini_store_app'@'localhost';
```

5. Copy `.env.example` to `.env`. Set `DB_USERNAME` and `DB_PASSWORD` to the account above, or
   another local account with access to `mini_store`.
6. Run `database/ai-migration.sql` with the administrator account to add the `reviews` and
   `review_summary` tables. They are needed before the seed inserts demo reviews.
7. With the application stopped, open `database/seed.sql` in Workbench and execute the entire
   script. Run it in one session at a time. On an SQL error, execute `ROLLBACK;` and resolve the
   error before retrying.
8. Run `database/auth-migration.sql` with the administrator account to add `password_hash` to an
   existing schema and, optionally, set the demo passwords for the customers the seed created.
9. From the project folder, start the application (Java 17 is the tested setup):

```powershell
.\mvnw.cmd spring-boot:run
```

Schema setup, the auth migration, and demo-data seeding are separate, manual operations.
`setup.sql` creates fresh tables; `auth-migration.sql` upgrades an existing schema;
`seed.sql` inserts missing demo records. None of them run on application startup.

The seed creates Stationery (Notebook 250.00, Pen 100.00), Books (Spring Notes 150.00),
Demo Customer (`demo@ministore.example`) with 3 notebooks and 1 pen totaling 850.00, and Team
Customer (`team@ministore.example`) with an empty cart. Load Demo Customer's cart by signing in as
the demo customer and calling `GET /me/cart`.

For the AI summary, the seed also gives the Notebook four reviews from four separate demo readers
(`reader1@ministore.example` .. `reader4@ministore.example`), with ratings 5, 5, 4 and 2 so the
average is 4.0 and the summary has both praise and a durability complaint. Four reviewers are used
because a summary needs `REVIEWS_MINIMUM_FOR_SUMMARY` (default 3) and one customer may review a
product only once. The summary itself is not seeded: open the Notebook's product page and request
the summary (or `POST /products/1/reviews/summary`) to generate and cache it with the live provider.
The demo readers have no password, so they cannot sign in; they only exist to carry the reviews.

Reruns reuse categories by name, products by name/category/price, customers by the reserved demo
emails, carts by customer, and reviews by product/reviewer. Where matching category/product rows
already exist, the lowest ID is used. Names are not unique in this schema, so run the seed
sequentially with the app stopped. Existing records, cart UUIDs, prices and quantities are
preserved.

Hibernate uses `ddl-auto: validate`: it checks the SQL schema and does not create or reset it.
H2 is only a test dependency; the running app uses local MySQL.

## API agreement

### Catalog endpoints (public)

| Endpoint | Methods | Request |
|---|---|---|
| `/categories`, `/categories/{id}` | GET/POST, GET/PUT/DELETE | name |
| `/products`, `/products/{id}` | GET/POST, GET/PUT/DELETE | name, description, price, categoryId |
| `/products?categoryId=1` | GET | optional category filter |

Creates return 201, reads/updates return 200, deletions return 204. Errors: 400 invalid input,
401 unsigned, 403 CSRF, 404 missing, 409 conflict, 500 unexpected, 503 AI provider unavailable. Error
body: `{ "message": "...", "fieldErrors": { "email": "..." } }`.

Customer and cart endpoints live under `/auth` and `/me` (below). Every `/me` route derives the
customer from the session instead of the URL, which is what keeps one customer's cart private.
The former `/users` and `/carts` management routes no longer exist; they now return 404.

### Customer authentication

| Endpoint | Method | Notes |
|---|---|---|
| `/auth/csrf` | GET | Starts an anonymous session and returns `{ "token": "..." }` plus the `XSRF-TOKEN` cookie. |
| `/auth/register` | POST | `{ name, email, password }`; 201, creates the customer and an empty cart, signs in. |
| `/auth/login` | POST | `{ email, password }`; 200; rotates the session ID and CSRF token. |
| `/auth/logout` | POST | 204; invalidates the session and clears the CSRF cookie. |
| `/auth/me` | GET | 200 with `{ id, name, email }`, or 401 when anonymous. |
| `/me/cart` | GET/POST | The current customer's cart; POST creates it if an older account has none. |
| `/me/cart/items` | POST/DELETE | Add `{ productId }`; DELETE clears. |
| `/me/cart/items/{productId}` | PUT/DELETE | Update `{ quantity }` (1–100) or remove. |
| `/me/cart` | DELETE | Deletes the customer's cart. |

Unsafe `/auth/*` requests (register, login, logout) and every unsafe `/me/*` request require
`X-XSRF-TOKEN` matching the session token. `GET /auth/me`, `GET /me/cart`, products, and categories
are safe reads. The frontend always derives cart operations from `/me`; it never sends a customer ID.

### Reviews and the AI features

| Endpoint | Method | Notes |
|---|---|---|
| `/products/{id}/reviews` | GET | Public. Reviews, average rating, the cached AI summary and a message when a summary is not possible. |
| `/products/{id}/reviews/summary` | POST | Public, CSRF-protected. Generates or returns the cached review summary. |
| `/me/reviews` | POST | Signed-in customer only, CSRF-protected. `{ productId, rating, comment }`; the author is the session customer. 409 on a second review of the same product. |
| `/chat` | POST | Public, CSRF-protected. `{ message }` (1–1000 chars); returns `{ reply, products }` grounded in the database. |

`POST /chat` and the summary endpoint return 503 when the AI provider is missing, denied, or out of
quota. A product with too few reviews is answered with a normal message, never an error. See
`docs/ai-features.md` for the grounding and caching rules.

## Security decisions

- **Password hashing:** PBKDF2-HMAC-SHA256 with a random 16-byte salt, 210,000 iterations, and a
  256-bit derived key. The stored format is
  `pbkdf2_sha256$iterations$saltBase64$hashBase64`. Comparison uses `MessageDigest.isEqual`.
  Plaintext passwords and hashes are never returned; `User.passwordHash` is also `@JsonIgnore` as a
  second line of defence.
- **Passwords in transit:** the client sends them only in the JSON login/registration request and
  keeps them in React component state for the duration of the submit. They are never written to
  `localStorage`, `sessionStorage`, a URL, or an application log.
- **Sessions:** Spring MVC `HttpSession` with an `HttpOnly`, `SameSite=Lax` `JSESSIONID` cookie and a
  30-minute idle timeout. Successful registration/login calls `HttpServletRequest.changeSessionId()`
  before storing the customer ID. Logout invalidates the session and expires the CSRF cookie.
- **CSRF:** a random 256-bit synchronizer token is stored in the session and mirrored in a readable
  `XSRF-TOKEN` cookie. The React client sends it as `X-XSRF-TOKEN`; a `HandlerInterceptor` rejects
  unsafe `/auth` and `/me` requests without a matching token. Because credentials are used, CORS
  lists explicit origins rather than `*`.
- **Authorization:** `/me/**` is protected by an `AuthInterceptor`. Controllers read the customer ID
  from the session attribute, never from a path variable or request body. The only customer-facing
  cart API is `/me/cart`.
- **No anonymous management API:** the legacy `/users` and `/carts` routes were removed. A customer
  can only be reached through `/auth` (their own record) and `/me` (their own cart); no endpoint
  accepts another customer's ID. Catalog writes remain unauthenticated, which is acceptable only
  for this local learning project.
- **Unique accounts:** email is normalized to lower case and checked before insert; the database
  unique constraint remains the final guard. Duplicate registration returns 409.
- **No Spring Security:** authentication, hashing, CSRF, and interceptors are ordinary Spring MVC
  and JDK code.

## Scope and ownership

Every source, config, and doc file starts with a member comment (`//member N` in Java/JavaScript,
`/* member N */` in CSS, `-- member N` in SQL, `#member N` in YAML/env/prompts, `<!-- member N -->`
in HTML/Markdown/SVG, and a `"//"` key or `//` comment in JSON) so each part of the project is
attributable. Members 1 and 2 are freshers who completed the easy Phase 1 slices. Member 3 is
experienced; members 3 and 4 share the hard backend, the React storefront, and the AI features.

| Member | Difficulty | Owns | Status |
|---|---|---|---|
| 1 | Easy | Category CRUD; `database/setup.sql` and the Phase 1 part of `database/seed.sql` | Done (Phase 1) |
| 2 | Easy | Product CRUD + category filter; product/category mapping | Done (Phase 1) |
| 3 | Moderate–Hard | Phase 2 authentication (session login, PBKDF2, CSRF, interceptors, shared exceptions and error DTO); the basic React run pages (shop, cart, login, register, account, layouts, guards, UI primitives, and `lib/api.js`, `lib/cart.js`, `lib/format.js`, `lib/utils.js`) | Done (Phase 2 + Phase 3 pages) |
| 4 | Hard–Very hard | Customer-linked cart and the `/me` cart (concurrency); reviews and the AI shop assistant + review summary (backend and React); the hard React pieces (product page, chat widget, review section, star rating, AI text); build/configuration; integration tests | Done (Phase 1–3) |

`docs/work-allocation.md` lists every file with its owner, difficulty, and one-line purpose.

Maven wrapper files are copied vendor tooling, with their original license comments preserved.

Read the learning guides: `docs/member-1.md`, `docs/member-2.md`, `docs/member-3.md`,
`docs/member-4.md`, the Phase 2 walkthrough in `docs/phase-2.md`, the AI feature notes in
`docs/ai-features.md`, and the work allocation in `docs/work-allocation.md`.

## Frontend layout

```text
frontend/
  src/
    components/ui/          shadcn/ui Button, Input, Label, Card, Alert, Badge, Separator, Skeleton, Sonner
    components/site/        layout, chat widget, product card, reviews, quantity stepper, auth guards
    context/AuthContext.jsx session-backed auth state (no localStorage)
    lib/api.js              fetch wrapper, cookies, CSRF header, one 403 retry
    lib/cart.js             get-or-create cart and add-to-cart helpers
    lib/chat.js             POST /chat wrapper
    lib/reviews.js          review list, review creation and summary wrappers
    pages/                  Shop, Product, Login, Register, Cart, Account, NotFound
  vite.config.js            React + Tailwind plugins and /api dev proxy
```

The UI uses functional components and hooks only. There is no Redux, Zustand, or other state
library. The server session is the source of truth after a page refresh.

## Tests

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd test
```

The final build runs 28 Spring tests on an isolated in-memory H2 database. Members 1–3 own every
test; member 4 owns none. The former single `StoreIntegrationTests` class was split per member.

| Test class | Member | Tests | Covers |
|---|---|---|---|
| `CategoryIntegrationTests` | 1 | 1 | Category CRUD and occupied-category deletion. |
| `ProductIntegrationTests` | 2 | 1 | Product/category mapping, category filter, update, price/category validation. |
| `CartIntegrationTests` | 3 | 5 | Session cart lifecycle, items/totals/clear, quantity limits, fractional rejection, concurrent adds. |
| `ErrorHandlingIntegrationTests` | 3 | 2 | Removed legacy `/users`,`/carts` routes and the shared error shape. |
| `CustomerAuthIntegrationTests` | 3 | 8 | Registration, duplicate email, validation, login, logout, session persistence and rotation, CSRF rejection, session-derived cart access, cart isolation. |
| `AiFeatureIntegrationTests` | 3 | 9 | Chat with/without matching products, blank/over-long questions, review submission and duplicates, review access control, empty review list, too-few-reviews message, summary generation plus caching, missing AI key → 503. |
| `PasswordHasherTest` | 3 | 2 | Salted hashes, verification, malformed-hash rejection. |

`IntegrationTestSupport` holds the shared MockMvc/CSRF session scaffolding. The AI provider is
replaced with a stub (`@MockitoBean AiClient`), so the AI tests need no key and make no network
calls.

Frontend checks:

```powershell
cd frontend
npm run lint
npm run build
```

The browser flow is verified separately against a running backend: registration with client
validation, add-to-cart, quantity update, refresh persistence, account rendering, logout, protected
route redirect, invalid and valid login, cart restoration after login, mobile layout, and checks
that no data is written to `localStorage`/`sessionStorage` and that `JSESSIONID` is not readable from
JavaScript. See `docs/verification.md` for the recorded results.

## Excluded from this phase

JWT/OAuth, password reset, email verification, roles/administration, addresses, profiles, wishlist,
checkout, orders, payments, inventory, uploads, pagination, and a bundled production web server for
the React build. This is a local management/learning app. The React frontend consumes the same REST
API and the backend remains the source of truth.
