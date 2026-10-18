# Mini Store

A reduced version of [Mosh's Spring Boot store](https://github.com/mosh-hamedani/spring-api-starter).
One Java 17 / Spring Boot 3.4.1 application, MySQL 8, an explicit-Java REST API, and a React 19
storefront. There is no Spring Security, Lombok, MapStruct, or Thymeleaf.

Phase 1 implemented categories, products, customer records, and one customer-linked cart.
Phase 2 adds customer registration, login, logout, session persistence, CSRF protection, and a
React interface for the shop, cart, and account.

## What is implemented

- Category CRUD; product CRUD and category filtering.
- Customer records with unique, lower-cased email addresses and shared JSON validation errors.
- One cart per customer, cart items, quantity limits, totals, clear/remove, and optimistic locking.
- Customer authentication with PBKDF2-HMAC-SHA256 password hashes.
- Server-side `HttpSession` login state, session-ID rotation, and logout invalidation.
- A CSRF synchronizer token plus a credentialed CORS configuration for the React client.
- Session-derived `/me` cart endpoints that never trust a customer ID from the browser.
- React 19 + Vite + Tailwind CSS + shadcn/ui components: shop, category filters, cart,
  registration, login, account details, logout, skeleton loading states, and toast notifications.
- The original management API (`/users`, `/carts`) remains available for the learning project.

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
setup.sql  ->  seed.sql (optional)  ->  auth-migration.sql
```

If `seed.sql` creates the demo customers after the migration was run, run `auth-migration.sql`
again. The demo password is:

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

The `JSESSIONID` cookie is `HttpOnly`, `SameSite=Lax`, and secure when the request is HTTPS
(`SESSION_COOKIE_SECURE=true` for a deployed HTTPS backend). The CSRF cookie is intentionally
readable by JavaScript because the React client must copy it into the `X-XSRF-TOKEN` header.
Because `SameSite=Lax` cookies are site-scoped, use the same hostname (normally `localhost`) for
both the page and the API in direct mode. The Vite development proxy avoids this issue entirely by
making `/api` same-origin.

## Set up MySQL on another machine (Workbench)

Workbench is the client; MySQL Server must also be installed and running locally.

1. Connect to your local MySQL server in Workbench with an administrator account.
2. File → Open SQL Script → `database/setup.sql`, then execute the entire script using the
   lightning button.
3. Refresh Schemas. Confirm `mini_store` contains `categories`, `products`, `users`, `carts`,
   `cart_item`, and that `users` has a nullable `password_hash` column.
4. For a dedicated account, execute this SQL with your chosen password (skip `CREATE USER` if the
   account already exists):

```sql
CREATE USER 'mini_store_app'@'localhost' IDENTIFIED BY 'your_chosen_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON mini_store.* TO 'mini_store_app'@'localhost';
```

5. Copy `.env.example` to `.env`. Set `DB_USERNAME` and `DB_PASSWORD` to the account above, or
   another local account with access to `mini_store`.
6. With the application stopped, open `database/seed.sql` in Workbench and execute the entire
   script. Run it in one session at a time. On an SQL error, execute `ROLLBACK;` and resolve the
   error before retrying.
7. Run `database/auth-migration.sql` with the administrator account to add `password_hash` to an
   existing schema and, optionally, set the demo passwords.
8. From the project folder, start the application (Java 17 is the tested setup):

```powershell
.\mvnw.cmd spring-boot:run
```

Schema setup, the auth migration, and demo-data seeding are separate, manual operations.
`setup.sql` creates fresh tables; `auth-migration.sql` upgrades an existing schema;
`seed.sql` inserts missing demo records. None of them run on application startup.

The seed creates Stationery (Notebook 250.00, Pen 100.00), Books (Spring Notes 150.00),
Demo Customer (`demo@ministore.example`) with 3 notebooks and 1 pen totaling 850.00, and Team
Customer (`team@ministore.example`) with an empty cart. Load Demo Customer's cart with
`GET /users/{id}/cart` (management API) or sign in as the demo customer and use `GET /me/cart`.

Reruns reuse categories by name, products by name/category/price, customers by the reserved demo
emails, and carts by customer. Where matching category/product rows already exist, the lowest ID is
used. Names are not unique in this schema, so run the seed sequentially with the app stopped.
Existing records, cart UUIDs, prices and quantities are preserved.

Hibernate uses `ddl-auto: validate`: it checks the SQL schema and does not create or reset it.
H2 is only a test dependency; the running app uses local MySQL.

## API agreement

### Existing management endpoints

| Endpoint | Methods | Request |
|---|---|---|
| `/categories`, `/categories/{id}` | GET/POST, GET/PUT/DELETE | name |
| `/products`, `/products/{id}` | GET/POST, GET/PUT/DELETE | name, description, price, categoryId |
| `/products?categoryId=1` | GET | optional category filter |
| `/users`, `/users/{id}` | GET/POST, GET/PUT/DELETE | name, email |
| `/carts` | POST | userId |
| `/users/{id}/cart`, `/carts/{id}` | GET | no body |
| `/carts/{id}` | DELETE | no body |
| `/carts/{id}/items` | POST/DELETE | productId for POST; DELETE clears |
| `/carts/{id}/items/{productId}` | PUT/DELETE | quantity for PUT |

Creates return 201, reads/updates return 200, deletions/clear return 204. Add-to-cart always
returns 200, including the first addition. Errors: 400 invalid input, 401 unsigned, 403 CSRF,
404 missing, 409 conflict, 500 unexpected. Error body:
`{ "message": "...", "fieldErrors": { "email": "..." } }`.

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
are safe reads. The frontend always derives cart operations from `/me`; it never sends a customer
ID.

Demo data note: `POST /users` creates a legacy customer record without a password. Such a record
cannot log in until a password hash is added by an administrator. Normal customers should register
through `/auth/register`.

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
- **Management API:** `/users` and `/carts` remain open because they are the original local
  learning/management endpoints and are used by the existing integration tests. A production
  deployment should protect or remove them. The React customer application uses only `/auth` and
  `/me`.
- **Unique accounts:** email is normalized to lower case and checked before insert; the database
  unique constraint remains the final guard. Duplicate registration returns 409.
- **No Spring Security:** authentication, hashing, CSRF, and interceptors are ordinary Spring MVC
  and JDK code.

## Scope and ownership

| Member | Feature | Shared work |
|---|---|---|
| 1 | Category CRUD | SQL setup, shared API conventions |
| 2 | Product CRUD + category filter | Product/category mapping |
| 3 | Customer name/email CRUD and Phase 2 authentication | Exceptions, error DTO, shared JSON error handler |
| 4 | Customer-linked cart, `/me` cart, and Phase 2 React/configuration | Build/configuration, totals, integration tests |

Java begins with `//member N`; SQL/configuration use their valid comment syntax. New Phase 2 files
follow the same convention. Maven wrapper files are copied vendor tooling, with their original
license comments preserved.

Read the learning guides: `docs/member-1.md`, `docs/member-2.md`, `docs/member-3.md`,
`docs/member-4.md`, and the Phase 2 walkthrough in `docs/phase-2.md`.

## Frontend layout

```text
frontend/
  src/
    components/ui/          shadcn/ui Button, Input, Label, Card, Alert, Badge, Skeleton, Sonner
    components/site/        layout, product card, quantity stepper, auth guards, empty state
    context/AuthContext.jsx session-backed auth state (no localStorage)
    lib/api.js              fetch wrapper, cookies, CSRF header, one 403 retry
    lib/cart.js             get-or-create cart helper
    pages/                  Shop, Login, Register, Cart, Account, NotFound
  vite.config.js            React + Tailwind plugins and /api dev proxy
```

The UI uses functional components and hooks only. There is no Redux, Zustand, or other state
library. The server session is the source of truth after a page refresh.

## Tests

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd test
```

The final build runs 19 tests on an isolated in-memory H2 database:

- 9 original store integration tests: category/product/customer/cart CRUD, mapping, totals,
  validation, concurrency, and error responses.
- 8 customer-auth integration tests: registration, duplicate email, validation, login, logout,
  session persistence and rotation, CSRF rejection, session-derived cart access, and cart isolation.
- 2 password-hasher unit tests: salted hashes, verification, and malformed-hash rejection.

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
