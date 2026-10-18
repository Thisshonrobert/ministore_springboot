# Verification â€” 12 October 2026

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

## Seed-script checks â€” 12 October 2026

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
  the seeded data, and selecting Demo Customer displayed Notebook quantity 3, Pen quantity 1 and total â‚¹850.00.
  Selecting Team Customer displayed an empty cart and â‚¹0.00. The test application was stopped afterward.

The seed is manual and insert-only. Reserve its two demo emails for demo customers, and run it sequentially
with the application stopped because category/product names have no unique constraint.
Existing prices, quantities and cart ownership are preserved; an edited demo cart is not reset to 850.00,
and a pre-existing Team cart is not cleared. No schema change or automatic startup seeding was added.

## Refactor verification â€” 18 October 2026

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

## Phase 2 authentication and React verification â€” 18 October 2026

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
- Browser checks used Playwright CLI with Microsoft Edge at 1280Ã—720 and 390Ã—844. The shop loaded
  real products/categories through the Vite `/api` proxy; registration validation, successful
  registration, add-to-cart, cart quantity update, refresh persistence, account details, logout,
  protected-route redirect, invalid login, valid login, and cart restoration all passed. The browser
  confirmed `JSESSIONID` is HttpOnly and `document.cookie` cannot read it, `XSRF-TOKEN` is readable,
  `localStorage`/`sessionStorage` remain empty, and the mobile shop has no horizontal overflow
  (0 px). 24 checks passed; 9 console 401 entries were expected from the anonymous `/auth/me` probe
  and the deliberate wrong-password attempt.
- Screenshots from the browser pass are local artifacts under `target/screenshots/` (the
  `target/` directory is ignored by Git).

### Phase 2 remaining setup step

`database/auth-migration.sql` has not been applied to this machine's real `mini_store` database
because the application account is not allowed to `ALTER` tables. An administrator must run that
script once (Workbench or `mysql -u <admin> -p mini_store < database/auth-migration.sql`) before
starting the default MySQL profile; otherwise Hibernate schema validation will report the missing
`password_hash` column. The script is idempotent and also sets the documented demo passwords when
their hashes are empty.

## Limits and setup

Category ID intentionally retains the plan's Byte/TINYINT type: at most 127 generated IDs,
including IDs of deleted categories. For a larger catalog, widen entity/DTO/controller/repository IDs and SQL together.
Totals use current prices; there is no checkout, historical pricing or inventory.
The local app now has customer authentication; the original `/users` and `/carts` management
endpoints remain open for the learning tests and should be protected or removed before deployment.
Credentials are in the private .env file. Workbench setup and environment instructions are in README.md.
