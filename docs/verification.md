<!-- member 4 -->
# Verification — 12 October 2026

## Previously recorded application checks

The checks below were recorded before `database/seed.sql` was added; they are not seed-script test results.

- Java 17.0.12, Spring Boot 3.4.1, local MySQL Server 8.0.45.
- Maven package succeeded; 10 integration tests, 0 failures/errors/skips (isolated H2 test profile).
- Real MySQL startup passed Hibernate schema validation with the dedicated app account.
- 41 API requests checked CRUD, duplicate email/cart, invalid/missing records, quantity limits,
  fractional quantity rejection, simultaneous additions, blocked deletions, clear/remove persistence,
  cart deletion preserving customer/products, DTO mapping, and totals.
- Application was stopped and restarted. The same customer/cart IDs, two items and total 850.00 survived.
- Browser checks: create category; create/filter product; create customer; display duplicate-email error;
  create customer cart; add product; update quantity; remove item. Narrow-window layout inspected.
- The new SQL schema contains five tables in mini_store; the original store_api database was not modified.
- Original source repository remains unchanged.

## Previously present local demo data

Stationery: Notebook 250.00, Pen 100.00. Demo Customer's cart: 3 notebooks + 1 pen = 850.00.
Books: Spring Notes 150.00. Team Customer has an empty cart, created through the UI checks.
These rows are in this machine's MySQL database; setup.sql on another machine starts empty.
The existing verification record identifies Team Customer's cart as created through UI checks.
It does not establish how the other demo records were created. Repository inspection found no prior
seed script, `data.sql`, or startup data initializer. Their presence is not evidence of scripted seeding.
The new seed verification did not modify these existing `mini_store` rows.

## Seed-script checks — 12 October 2026

- Tested on MySQL 8.0.45 in a fresh, separate `mini_store_seed_final_20261012` database.
  Only database names in copies of setup/seed were substituted for isolation; `setup.sql` is unchanged.
- Setup created all five tables. Seed created 2 categories, 3 products, 2 customers, 2 carts and 2 cart items.
  Stationery contains Notebook 250.00 and Pen 100.00; Books contains Spring Notes 150.00.
- Demo Customer (`demo@ministore.example`) has 3 notebooks and 1 pen: 850.00 using current price times quantity.
  Team Customer (`team@ministore.example`) has an empty cart.
- Running the seed a second time left every row and cart UUID unchanged.
- Added an unrelated category, differently priced Notebook, customer, cart and item, then ran the seed twice more.
  Full ordered row comparisons were unchanged; counts remained 3 categories, 4 products, 3 customers, 3 carts, 3 items.
  Relationship queries found no products without categories, carts without customers, or items without carts/products.
- In a separate test database, changing Demo Customer's email and reseeding preserved the original customer/cart IDs
  and created a replacement demo customer/cart without a UUID collision; another rerun succeeded.
- Maven package passed on Java 17.0.12: 10 existing H2 integration tests, 0 failures/errors/skips.
  These tests verify application behavior; the seed checks above used real MySQL separately.
- Started the packaged application on port 18081 against the seeded test database using environment overrides,
  without modifying `.env`. Hibernate schema validation passed. The browser customer/product selectors showed
  the seeded data, and selecting Demo Customer displayed Notebook quantity 3, Pen quantity 1 and total ₹850.00.
  Selecting Team Customer displayed an empty cart and ₹0.00. The test application was stopped afterward.

The seed is manual and insert-only. Reserve its two demo emails for demo customers, and run it sequentially
with the application stopped because category/product names have no unique constraint.
Existing prices, quantities and cart ownership are preserved; an edited demo cart is not reset to 850.00,
and a pre-existing Team cart is not cleared. No schema change or automatic startup seeding was added.

## Refactor verification — 18 October 2026

- Removed Lombok, MapStruct and Thymeleaf from `pom.xml` and the annotation-processor configuration.
  Removed all related imports/annotations, the Thymeleaf templates, the page controller and the template JavaScript/CSS.
- Replaced generated accessors, constructors and mappers with explicit Java methods. Replaced the remaining
  stream chains with ordinary loops while preserving validation, relationships, transactions and API responses.
- `.\mvnw.cmd clean test`: 9 tests, 0 failures, 0 errors, 0 skipped (H2 test profile).
  The former Thymeleaf page smoke test was removed because that UI was intentionally removed.
- `.\mvnw.cmd package -DskipTests`: BUILD SUCCESS.
- The packaged application started against local MySQL 8.0.45 and passed Hibernate schema validation.
  A temporary HTTP check created a category, product, customer and cart; added the same product twice
  (quantity 2, total 246.90), updated the quantity to 3 (total 370.35), rejected quantity 101 with 400,
  cleared the items (total 0), and deleted the cart/customer/product/category with 204 responses.
  An invalid email returned 400 with a fieldErrors entry. Temporary rows were removed; the original
  demo categories, products and customers were unchanged.
- `/`, `/ui/products` and the other former page routes now return the JSON 404 response, confirming that
  the backend is API-only and ready for a React frontend.

## Phase 2 authentication and React verification — 18 October 2026

- `.\mvnw.cmd clean package`: BUILD SUCCESS. 19 tests, 0 failures, 0 errors, 0 skipped
  (9 original store integration tests, 8 customer-auth integration tests, 2 password-hasher tests).
  `$LASTEXITCODE` was 0. The Maven JVM printed its usual CDS sharing warning to stderr; it is not a
  test failure.
- `npm run build` in `frontend/`: Vite 8.3.4 production build succeeded, 1,965 modules transformed.
  Output was `dist/index.html` 1.11 kB, CSS 50.30 kB, JS 398.93 kB (119.86 kB gzip).
- `npm run lint`: `oxlint` found 0 errors and 5 advisory warnings (React
  `set-state-in-effect`/only-export-components rules). No unused imports or type/runtime errors.
- `npm run preview` after the production build served `dist/` on port 4173: the SPA shell returned
  200 for `/` and `/cart`, and `/api/products` returned 200 through the configured preview proxy.
- Live API checks used the test H2 profile on port 18080 because the local MySQL
  `mini_store_app` account has only SELECT/INSERT/UPDATE/DELETE and cannot run the one-time
  `ALTER TABLE` auth migration. Command:
  `$env:SPRING_PROFILES_ACTIVE='test'; $env:SERVER_PORT='18080'; .\mvnw.cmd spring-boot:test-run`.
  Hibernate created the H2 schema from the updated entities and the app started normally.
- HTTP checks with a real cookie jar: `GET /auth/csrf` returned a 43-character token; registration
  without a CSRF header returned 403; registration with the token returned 201 and rotated both
  `JSESSIONID` and the CSRF token; duplicate registration returned 409 with the duplicate-email
  message; `GET /auth/me` returned the customer while public DTOs contained no password fields;
  wrong-password and unknown-email login both returned 401; logout returned 204 and subsequent
  `/auth/me` and `/me/cart` requests returned 401. Adding the same product twice produced quantity
  2 and total 499.00; `PUT` quantity 3 produced 748.50; remove, clear, and the `/me` session-derived
  cart all behaved correctly.
- CORS preflight from `http://localhost:5173` returned 200 with
  `Access-Control-Allow-Origin: http://localhost:5173`, `Access-Control-Allow-Credentials: true`,
  and the expected methods/headers.
- Browser checks used Playwright CLI with Microsoft Edge at 1280×720 and 390×844. The shop loaded
  real products/categories through the Vite `/api` proxy; registration validation, successful
  registration, add-to-cart, cart quantity update, refresh persistence, account details, logout,
  protected-route redirect, invalid login, valid login, and cart restoration all passed. The browser
  confirmed `JSESSIONID` is HttpOnly and `document.cookie` cannot read it, `XSRF-TOKEN` is readable,
  `localStorage`/`sessionStorage` remain empty, and the mobile shop has no horizontal overflow
  (0 px). 24 checks passed; 9 console 401 entries were expected from the anonymous `/auth/me` probe
  and the deliberate wrong-password attempt.
- Screenshots from the browser pass are local artifacts under `target/screenshots/` (the
  `target/` directory is ignored by Git).

### Phase 2 authentication migration

`database/auth-migration.sql` has been applied to this machine's `mini_store` database with an
administrator account. It added the nullable `users.password_hash` column and set the PBKDF2 hashes
for the two demo customers. Hibernate schema validation now passes against MySQL, and both demo
accounts can sign in.

## Phase 3 reviews and AI features - 18 October 2026

- `.\mvnw.cmd test`: 28 tests, 0 failures, 0 errors, 0 skipped (H2 test profile). Breakdown:
  9 `StoreIntegrationTests`, 8 `CustomerAuthIntegrationTests`, 9 `AiFeatureIntegrationTests`,
  2 `PasswordHasherTest`.
- The 9 AI tests replace the AI provider with a stub (`@MockitoBean AiClient`) and cover: a chat
  request that matches products and one that does not, a blank question (400) and a question longer
  than 1000 characters (400), review creation from the session customer, a duplicate review (409),
  anonymous review creation (401), an empty review list, a summary request with too few reviews, a
  successful summary plus its cache, and a missing AI key (503).
- `npm run lint` in `frontend/`: `oxlint` reported 0 errors and 7 advisory warnings (React
  `set-state-in-effect` and `only-export-components` rules, the same advisory pattern as Phase 2).
- `npm run build` in `frontend/`: Vite 8.3.4 production build succeeded, 1,971 modules transformed.
  Output: `dist/index.html` 1.11 kB, CSS 53.51 kB, JS 419.31 kB (124.01 kB gzip).
- **Live Gemini calls could not be verified.** With the key in the local `.env`, every model tried
  returned `403 PERMISSION_DENIED` ("Your project has been denied access"), `429` with a quota limit
  of 0, or `404` for models retired for new users. Therefore the real chat reply and the real review
  summary are not part of these recorded results; only the stubbed path is verified. The endpoint
  contract, the grounding queries, the caching, the validation and the 503 behaviour are all
  exercised by the tests above. The default model is `gemini-3.5-flash` and can be overridden with
  `AI_MODEL` once a permitted key is available.
- `database/ai-migration.sql` adds the `reviews` and `review_summary` tables and is idempotent; the
  same tables are present in the updated `database/setup.sql`. Hibernate `ddl-auto: validate` requires
  them before the app starts.

## Seeded reviews and a live summary check - 19 October 2026

- `database/seed.sql` now also seeds one product with reviews. The Notebook gets four reviews from
  four demo readers (`reader1@ministore.example` .. `reader4@ministore.example`), ratings 5, 5, 4 and
  2 (average 4.0), with `created_at` spread over the days before the run so "newest first" is stable.
  Four reviewers are used because a summary needs `REVIEWS_MINIMUM_FOR_SUMMARY` (3) and one customer
  may review a product only once; the comments mix praise with a durability complaint so the summary
  has both a positive point and a caveat. No `review_summary` row is seeded.
- Applied to this machine's `mini_store` (MySQL 8.0.45) with the `mini_store_app` account:
  `source database/seed.sql` exited 0. The Notebook then reported `reviewCount 4` and
  `averageRating 4.0` with four distinct reviewers. Running the seed a second time left the review
  count at 4 and the reader user count at 4, so the review inserts are idempotent like the rest of the
  script.
- `.\mvnw.cmd test`: 28 tests, 0 failures, 0 errors, 0 skipped. `AiFeatureIntegrationTests` (9 tests)
  includes `summaryIsGeneratedFromRealReviewsCachedAndInvalidatedByANewReview`, which exercises the
  whole summariser (generation from real rows, the cache and invalidation by a new review) against a
  stubbed `AiClient`.
- Live provider check against the running app on port 8080 (MySQL, `.env` with
  `AI_MODEL=gemini-3.1-flash-lite`): `GET /auth/csrf` returned a 43-character token, and
  `POST /products/1/reviews/summary` with `X-XSRF-TOKEN` returned 200 with this real Gemini summary
  of the four seeded reviews:

  > Most customers are satisfied with this notebook, which holds an average rating of 4.0 out of 5
  > across four reviews. Users appreciate the smooth paper quality and the overall value for the
  > price. However, opinions on the durability are split, as some reviewers praised the binding while
  > others reported that pages came loose after a few weeks. Additionally, one customer noted that the
  > cover is too thin for reliable protection in a backpack.

  The response carried `generatedAt` 2026-10-19T13:28:42.999 and `expiresAt` one week later. A second
  `POST` returned the same `generatedAt` (a cache hit, so no second provider call), and
  `GET /products/1/reviews` then served the same cached summary. `review_summary` held exactly one row
  for product 1. The summary is left cached so the demo product page shows it at once; a new review
  clears it and it expires after `REVIEW_SUMMARY_TTL_HOURS`.

## Limits and setup

Category ID intentionally retains the plan's Byte/TINYINT type: at most 127 generated IDs,
including IDs of deleted categories. For a larger catalog, widen entity/DTO/controller/repository IDs and SQL together.
Totals use current prices; there is no checkout, historical pricing or inventory.
The local app now has customer authentication; the legacy `/users` and `/carts` management
endpoints have been removed, so customer records and carts are only reachable through `/auth` and
`/me`. Catalog endpoints remain public.
Credentials are in the private .env file. Workbench setup and environment instructions are in README.md.
The AI features additionally need an external provider and a permitted `AI_API_KEY`; without one the
store still runs and the AI endpoints return 503. The assistant can only see the product catalogue,
and the review summary can only see a product's reviews.

## Test suite reorganisation — 19 October 2026

The single `StoreIntegrationTests` class was split into `CategoryIntegrationTests` (member 1),
`ProductIntegrationTests` (member 2), and `CartIntegrationTests` plus `ErrorHandlingIntegrationTests`
(member 3), all sharing a new `IntegrationTestSupport` base. `AiFeatureIntegrationTests` moved to
member 3, so members 1–3 now own every test and member 4 owns none. The reported earlier sections
still name the original single class on purpose, as a record of what was verified at the time. The
suite is unchanged at 28 tests: `.\mvnw.cmd test` reports 28 tests, 0 failures, 0 errors, 0 skipped.
