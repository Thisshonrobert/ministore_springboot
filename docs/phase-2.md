<!-- member 3 -->
# Phase 2 — Customer authentication and the React storefront

Phase 2 keeps the Phase 1 API and adds a customer-facing authentication flow without Spring
Security. The Java code uses the JDK, Spring MVC, JPA, and `HttpSession`; the browser code uses
React function components and hooks.

## Request flows

### Registration

1. The React app calls `GET /auth/csrf`. `AuthController` starts an anonymous session, and
   `CsrfTokenService` stores a random token in the session and copies it to the `XSRF-TOKEN` cookie.
2. `POST /auth/register` carries `X-XSRF-TOKEN`. `CsrfInterceptor` compares it with the session token.
3. `AuthService.register` lower-cases the email, rejects an existing email with 409, hashes the
   password with `PasswordHasher`, saves the `User`, and creates that customer's empty `Cart` in the
   same transaction.
4. `AuthController` calls `request.changeSessionId()`, stores the new customer ID in the session,
   rotates the CSRF token, and returns 201 with the public `UserDto`. The new session ID and CSRF
   token are sent back in cookies; the CSRF token is also returned in `X-CSRF-TOKEN`.

### Login

1. The same CSRF handshake is required.
2. `AuthService.authenticate` looks the email up and calls `PasswordHasher.matches`. Unknown email,
   missing hash, and wrong password all produce the same 401 message.
3. On success the session ID is rotated, the customer ID is stored, and the CSRF token is rotated.
4. `GET /auth/me` reads only the session attribute and returns the public customer DTO, so a page
   refresh can restore the UI without trusting anything stored in the browser.

### Logout

`POST /auth/logout` is CSRF-protected. `AuthController` invalidates the session, clears the CSRF
cookie, and returns 204. `GET /auth/me` and `/me/cart` then return 401.

### Customer cart

`AuthInterceptor` protects `/me/**`. `MeController` reads `AuthService.SESSION_CUSTOMER_ID` from the
`HttpSession` and calls `CartService` with that ID. A customer ID is never accepted from the URL or
request body. The legacy `/users` and `/carts` endpoints have been removed, so `/me` is the only
cart API and a customer ID is never accepted from the client.

## Backend files to read

| File | Purpose |
|---|---|
| `entities/User.java` | Adds the nullable `password_hash` column and `@JsonIgnore` on its getter. |
| `repositories/UserRepository.java` | Adds `findByEmail`. |
| `dtos/RegisterRequest.java`, `dtos/LoginRequest.java` | Bean Validation constraints and readable messages. |
| `dtos/CsrfTokenResponse.java` | The token returned by `GET /auth/csrf`. |
| `services/PasswordHasher.java` | PBKDF2-HMAC-SHA256 hashing, random salt, constant-time comparison. |
| `services/AuthService.java` | Registration, login, current-customer lookup, email normalization. |
| `services/CsrfTokenService.java` | Session token, readable cookie, header validation. |
| `controllers/AuthController.java` | `/auth/csrf`, register, login, logout, me; session rotation. |
| `controllers/MeController.java` | Session-derived cart endpoints. |
| `config/CsrfInterceptor.java` | Rejects unsafe `/auth` and `/me` requests without a valid token. |
| `config/AuthInterceptor.java` | Rejects `/me/**` requests without a session customer. |
| `config/WebConfig.java` | Credentialed CORS and interceptor registration. |
| `controllers/GlobalExceptionHandler.java` | Adds 401/403 JSON responses to the existing error format. |
| `src/main/resources/application.yaml` | Session timeout, HttpOnly/SameSite cookie, CORS origins. |
| `database/auth-migration.sql` | One-time `password_hash` migration and optional demo passwords. |

## Frontend files to read

| File | Purpose |
|---|---|
| `frontend/src/lib/api.js` | `fetch` wrapper: `credentials: 'include'`, CSRF header, JSON errors, one retry on 403. |
| `frontend/src/context/AuthContext.jsx` | Loads `/auth/me` on mount, exposes login/register/logout, keeps state in memory. |
| `frontend/src/components/site/RequireAuth.jsx` | Redirects anonymous visitors to `/login` and signed-in visitors away from auth pages. |
| `frontend/src/pages/ShopPage.jsx` | Product/category loading, filter, add-to-cart, hero and shelf layout. |
| `frontend/src/pages/LoginPage.jsx`, `RegisterPage.jsx` | Forms, client validation, server field errors, password visibility toggle. |
| `frontend/src/pages/CartPage.jsx` | `/me/cart` loading, quantity updates, remove/clear, totals. |
| `frontend/src/pages/AccountPage.jsx` | Session-backed customer details, cart snapshot, logout. |
| `frontend/src/components/ui/*` | shadcn/ui primitives; `sonner.jsx` and `skeleton.jsx` provide toasts and loading states. |
| `frontend/src/components/site/ProductCard.jsx` | Product presentation and add-to-cart action. |
| `frontend/vite.config.js` | Tailwind/React plugins and the `/api` development proxy. |

## Security notes

- Passwords are hashed with PBKDF2-HMAC-SHA256 (210,000 iterations, 16-byte random salt, 256-bit
  key) and compared with `MessageDigest.isEqual`.
- Passwords and hashes are absent from every DTO response. `User.passwordHash` also has
  `@JsonIgnore`.
- `JSESSIONID` is HttpOnly and SameSite=Lax. CSRF tokens are stored in the session and mirrored in
  a readable cookie; no password or session ID is ever written to `localStorage`.
- Session IDs and CSRF tokens rotate on successful register/login. Logout invalidates the session.
- CORS uses explicit origins because credentialed requests are enabled.
- `/me/**` derives the customer from the session. The old `/users` and `/carts` management
  endpoints were removed, so `/me` is the only cart API. Catalog writes remain unauthenticated in
  this local learning build.

## Commands used to verify this phase

```powershell
.\mvnw.cmd clean package
cd frontend
npm run lint
npm run build
```

For live checks, the backend was started against the H2 test profile on port 18080 and the Vite
development server proxied `/api` to it. The browser flow was exercised with Playwright and Edge at
desktop and mobile widths. `docs/verification.md` records the full results.
