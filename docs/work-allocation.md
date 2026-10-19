<!-- member 4 -->
# Work allocation

Four members built Mini Store over three phases. This file records who owns what, how hard each
piece was, and whether it is finished. Every source, config and doc file carries the same owner in a
top-of-file comment, so this table and the code agree.

## How ownership is marked

| File type | Comment on the first line |
|---|---|
| Java, JavaScript, JSX | `//member N` |
| CSS | `/* member N */` |
| SQL | `-- member N` |
| YAML, `.env`, prompt templates | `#member N` |
| HTML, Markdown, SVG | `<!-- member N -->` |
| JSON | `"//": "member N"` as the first key, or `//member N` where the tool accepts JSONC (`.oxlintrc.json`) |

A file with a heading such as `-- member 4: ...` part-way through means a second member added a
section to a file the first member owns (for example the review tables inside `database/setup.sql`).

## Difficulty legend

| Level | Meaning |
|---|---|
| ⭐ Easy | Straight CRUD or config a fresher can follow from the guide. |
| ⭐⭐ Moderate | Real logic or security, but a single well-understood concept. |
| ⭐⭐⭐ Hard | Transactions, concurrency, session/CSRF security, or non-trivial React state. |
| ⭐⭐⭐⭐ Very hard | AI integration, caching/invalidation, and cross-cutting design decisions. |

## Roles

| Member | Role | Difficulty band | Phases | Status |
|---|---|---|---|---|
| 1 | Fresher | ⭐ Easy | Phase 1 | Done |
| 2 | Fresher | ⭐ Easy | Phase 1 | Done |
| 3 | Experienced | ⭐⭐–⭐⭐⭐ Moderate–Hard | Phase 2 + basic React (Phase 3) | Done |
| 4 | Experienced | ⭐⭐⭐–⭐⭐⭐⭐ Hard–Very hard | Phase 1 cart, Phase 2 integration, Phase 3 AI/reviews | Done |

Members 3 and 4 share the hard part of the project: member 3 took authentication and the basic React
run pages, member 4 took the cart concurrency, the reviews, the AI features (backend and React) and
the build/integration work.

## Testing

The Spring tests are owned by members 1–3; **member 4 owns none**. The former single
`StoreIntegrationTests` class was split so each member has their own test class, and all the split
classes share one `IntegrationTestSupport` base:

| Test class | Member | Tests |
|---|---|---|
| `CategoryIntegrationTests` | 1 | 1 |
| `ProductIntegrationTests` | 2 | 1 |
| `CartIntegrationTests` | 3 | 5 |
| `ErrorHandlingIntegrationTests` | 3 | 2 |
| `CustomerAuthIntegrationTests` | 3 | 8 |
| `AiFeatureIntegrationTests` | 3 | 9 |
| `PasswordHasherTest` | 3 | 2 |

28 tests in total, all green on the H2 test profile.

---

## Member 1 — categories (⭐ Easy)

| File | Difficulty | Purpose |
|---|---|---|
| `database/setup.sql` | ⭐ | Creates the tables (the review tables were added later by member 4). |
| `database/seed.sql` | ⭐ | Inserts idempotent demo rows (the review seed rows were added later by member 4). |
| `.env.example` | ⭐ | Database/port template (the AI section was added later by member 4). |
| `docs/member-1.md` | ⭐ | Learning guide. |
| `src/main/java/com/accenture/store/entities/Category.java` | ⭐ | Category entity (`TINYINT`/`Byte` id). |
| `repositories/CategoryRepository.java` | ⭐ | `JpaRepository<Category, Byte>`. |
| `dtos/CategoryRequest.java`, `dtos/CategoryDto.java` | ⭐ | Request/response JSON with Bean Validation. |
| `mappers/CategoryMapper.java` | ⭐ | Entity ⇄ DTO by hand. |
| `services/CategoryService.java` | ⭐ | CRUD plus "category in use" rule. |
| `controllers/CategoryController.java` | ⭐ | `/categories` REST endpoints. |
| `src/test/java/com/accenture/store/CategoryIntegrationTests.java` | ⭐ | 1 Spring test: category CRUD and occupied-category deletion. |

## Member 2 — products (⭐ Easy)

| File | Difficulty | Purpose |
|---|---|---|
| `docs/member-2.md` | ⭐ | Learning guide. |
| `src/main/java/com/accenture/store/entities/Product.java` | ⭐ | Product entity with a `@ManyToOne` category. |
| `repositories/ProductRepository.java` | ⭐ | CRUD; `findCatalogue`/`search` were added later by member 4 for the AI assistant. |
| `dtos/ProductRequest.java`, `dtos/ProductDto.java` | ⭐ | JSON with `categoryId` and price validation. |
| `mappers/ProductMapper.java` | ⭐ | Entity ⇄ DTO by hand. |
| `services/ProductService.java` | ⭐ | CRUD, category filter, deletion guard; review/summary cleanup was added later by member 4. |
| `controllers/ProductController.java` | ⭐ | `/products` REST endpoints and `?categoryId=` filter. |
| `src/test/java/com/accenture/store/ProductIntegrationTests.java` | ⭐ | 1 Spring test: product mapping, filter, update, validation. |

## Member 3 — authentication and the basic React pages (⭐⭐–⭐⭐⭐)

### Backend: sessions, hashing, CSRF (⭐⭐–⭐⭐⭐)

| File | Difficulty | Purpose |
|---|---|---|
| `docs/member-3.md`, `docs/phase-2.md` | ⭐⭐ | Learning guides for the customer/authentication slices. |
| `src/main/java/com/accenture/store/entities/User.java` | ⭐⭐ | Customer with a nullable PBKDF2 hash. |
| `repositories/UserRepository.java` | ⭐⭐ | Adds `findByEmail`. |
| `dtos/RegisterRequest.java`, `dtos/LoginRequest.java` | ⭐⭐ | Login/registration validation. |
| `dtos/UserDto.java`, `dtos/CsrfTokenResponse.java` | ⭐ | Public customer JSON and CSRF token JSON. |
| `mappers/UserMapper.java` | ⭐ | Entity → public DTO. |
| `exceptions/BusinessRuleException.java`, `ConflictException.java`, `ForbiddenException.java`, `ResourceNotFoundException.java`, `UnauthorizedException.java` | ⭐ | Typed domain exceptions shared by all controllers. |
| `dtos/ErrorResponse.java` | ⭐⭐ | One JSON error shape (`message` + `fieldErrors`). |
| `controllers/GlobalExceptionHandler.java` | ⭐⭐⭐ | Maps every exception to the right status (400/401/403/404/409/500). |
| `services/PasswordHasher.java` | ⭐⭐⭐ | PBKDF2-HMAC-SHA256, random salt, constant-time compare. |
| `services/AuthService.java` | ⭐⭐⭐ | Register/login/current customer, email normalisation, session customer lookup. |
| `services/CsrfTokenService.java` | ⭐⭐⭐ | Synchronizer token in the session, mirrored to a readable cookie. |
| `config/CsrfInterceptor.java` | ⭐⭐⭐ | Rejects unsafe `/auth`/`/me`/`/chat`/summary requests without a valid token. |
| `config/AuthInterceptor.java` | ⭐⭐ | Rejects `/me/**` requests without a session customer. |
| `controllers/AuthController.java` | ⭐⭐⭐ | `/auth/csrf`, register, login, logout, me; session-ID and CSRF rotation. |
| `database/auth-migration.sql` | ⭐⭐ | One-time `password_hash` migration and demo hashes. |
| `src/test/java/com/accenture/store/IntegrationTestSupport.java` | ⭐⭐ | Shared MockMvc/CSRF/session scaffolding for the Spring tests. |
| `src/test/java/com/accenture/store/CustomerAuthIntegrationTests.java` | ⭐⭐⭐ | 8 authentication integration tests. |
| `src/test/java/com/accenture/store/CartIntegrationTests.java` | ⭐⭐⭐ | 5 cart integration tests, including concurrent adds. |
| `src/test/java/com/accenture/store/ErrorHandlingIntegrationTests.java` | ⭐⭐ | 2 tests: legacy-route removal and the shared error shape. |
| `src/test/java/com/accenture/store/AiFeatureIntegrationTests.java` | ⭐⭐⭐⭐ | 9 review/AI integration tests against a stubbed `AiClient`. |
| `src/test/java/com/accenture/store/PasswordHasherTest.java` | ⭐⭐ | 2 hashing unit tests. |

### Frontend: the basic React run pages (⭐⭐–⭐⭐⭐)

The straight storefront pages and the pieces needed to run them. Member 4 added the product page,
the chat widget, the review section and the AI helpers on top of these.

| File | Difficulty | Purpose |
|---|---|---|
| `frontend/src/main.jsx`, `frontend/src/App.jsx` | ⭐⭐ | React entry point and routes/providers. |
| `frontend/src/index.css` | ⭐⭐ | Tailwind v4 theme, palette and motion. |
| `frontend/src/context/AuthContext.jsx` | ⭐⭐⭐ | Session-backed auth state; no `localStorage`. |
| `frontend/src/lib/api.js` | ⭐⭐⭐ | `fetch` wrapper: credentials, CSRF header, JSON errors, one 403 retry. |
| `frontend/src/lib/cart.js`, `lib/format.js`, `lib/utils.js` | ⭐ | Cart helpers, INR formatting and `cn()`. |
| `frontend/src/pages/ShopPage.jsx` | ⭐⭐ | Product/category loading, filter and add-to-cart. |
| `frontend/src/pages/CartPage.jsx` | ⭐⭐ | `/me/cart` loading, quantities, remove/clear, totals. |
| `frontend/src/pages/LoginPage.jsx`, `pages/RegisterPage.jsx` | ⭐⭐ | Auth forms with client + server validation. |
| `frontend/src/pages/AccountPage.jsx` | ⭐⭐ | Session-backed account details and logout. |
| `frontend/src/pages/NotFoundPage.jsx` | ⭐ | 404 page. |
| `frontend/src/components/site/SiteLayout.jsx`, `AuthLayout.jsx` | ⭐ | Shell layouts. |
| `frontend/src/components/site/RequireAuth.jsx` | ⭐⭐ | Route guards. |
| `frontend/src/components/site/ProductCard.jsx`, `ProductGridSkeleton.jsx`, `QuantityStepper.jsx`, `EmptyState.jsx` | ⭐ | Shared shop building blocks. |
| `frontend/src/components/ui/*.jsx` | ⭐ | shadcn/ui primitives (Button, Input, Label, Card, Alert, Badge, Separator, Skeleton, Sonner). |

## Member 4 — cart, reviews, AI and integration (⭐⭐⭐–⭐⭐⭐⭐)

### Backend: cart and concurrency (⭐⭐⭐)

| File | Difficulty | Purpose |
|---|---|---|
| `docs/member-4.md` | ⭐⭐ | Cart learning guide. |
| `src/main/java/com/accenture/store/entities/Cart.java`, `CartItem.java` | ⭐⭐⭐ | One cart per customer; `UUID` key; cascade + orphan removal. |
| `repositories/CartRepository.java` | ⭐⭐⭐ | `@Lock(PESSIMISTIC_WRITE)` so concurrent adds do not lose increments. |
| `repositories/CartItemRepository.java` | ⭐⭐ | Item lookups and delete guard. |
| `dtos/CartDto.java`, `CartItemDto.java`, `CartProductDto.java`, `AddItemsToCart.java`, `UpdateCartItemRequest.java` | ⭐⭐ | Nested cart JSON and quantity validation. |
| `mappers/CartMapper.java` | ⭐⭐ | Flattens the cart graph to JSON with line totals. |
| `services/CartService.java` | ⭐⭐⭐ | Get-or-create, add/update/remove/clear/delete, totals, locking. |
| `controllers/MeController.java` | ⭐⭐⭐ | Session-derived `/me/cart` endpoints (no customer ID from the client). |
| `config/WebConfig.java` | ⭐⭐⭐ | Credentialed CORS and interceptor registration. |

### Backend: reviews and the AI features (⭐⭐⭐⭐)

| File | Difficulty | Purpose |
|---|---|---|
| `docs/ai-features.md` | ⭐⭐⭐ | AI feature notes. |
| `src/main/java/com/accenture/store/entities/Review.java`, `ReviewSummary.java` | ⭐⭐⭐ | Review table and the one-row-per-product summary cache. |
| `repositories/ReviewRepository.java`, `ReviewSummaryRepository.java` | ⭐⭐⭐ | `join fetch` review reads and summary lookup. |
| `dtos/ReviewRequest.java`, `ReviewDto.java`, `ReviewSummaryDto.java`, `ProductReviewsDto.java` | ⭐⭐ | Review and summary JSON. |
| `mappers/ReviewMapper.java` | ⭐⭐ | Review → DTO. |
| `services/ReviewService.java` | ⭐⭐⭐⭐ | Create/list reviews, min-review rule, AI summary generation and cache invalidation. |
| `controllers/ReviewController.java` | ⭐⭐⭐ | `/products/{id}/reviews`, `/me/reviews`, the summary endpoint. |
| `ai/AiClient.java` | ⭐⭐⭐ | The narrow provider seam the tests replace. |
| `ai/GeminiAiClient.java` | ⭐⭐⭐⭐ | Plain `RestClient` call to Gemini; provider errors become 503. |
| `ai/AiPrompts.java` | ⭐⭐⭐ | Loads the prompt templates and strips their ownership comment. |
| `resources/prompts/store-assistant.txt`, `prompts/summarize-reviews.txt` | ⭐⭐⭐ | Prompt templates. |
| `services/ChatService.java` | ⭐⭐⭐⭐ | Keyword search, grounded prompt, returns the same rows it used. |
| `controllers/ChatController.java` | ⭐⭐ | Public `POST /chat`. |
| `exceptions/AiUnavailableException.java` | ⭐ | 503 when the provider is missing/denied/over-quota. |
| `database/ai-migration.sql` | ⭐⭐ | Adds the `reviews` and `review_summary` tables (also in `setup.sql`). |

### Frontend: the hard React and AI pieces (⭐⭐⭐)

| File | Difficulty | Purpose |
|---|---|---|
| `frontend/src/pages/ProductPage.jsx` | ⭐⭐⭐ | Product detail with add-to-cart and the review section. |
| `frontend/src/components/site/ChatWidget.jsx` | ⭐⭐⭐ | Floating shop-assistant panel. |
| `frontend/src/components/site/ProductReviews.jsx` | ⭐⭐⭐ | Review list, AI summary card, signed-in review form. |
| `frontend/src/components/site/StarRating.jsx` | ⭐⭐ | Read-only star display. |
| `frontend/src/components/site/AiText.jsx` | ⭐⭐⭐ | Renders the small Markdown subset the model returns, safely. |
| `frontend/src/lib/chat.js`, `lib/reviews.js` | ⭐ | Thin wrappers over `/chat` and the review endpoints. |

### Build, configuration and docs (⭐⭐–⭐⭐⭐)

| File | Difficulty | Purpose |
|---|---|---|
| `pom.xml` | ⭐⭐ | Maven dependencies and plugins (no Lombok/MapStruct/Thymeleaf/AI SDK). |
| `src/main/resources/application.yaml` | ⭐⭐⭐ | Datasource, session cookie, CORS, AI and review settings. |
| `src/test/resources/application-test.yaml` | ⭐⭐ | H2 test profile. |
| `src/main/java/com/accenture/store/StoreApplication.java` | ⭐ | Spring Boot entry point. |
| `frontend/vite.config.js`, `frontend/package.json` | ⭐⭐ | React/Tailwind plugins and the `/api` dev proxy. |
| `frontend/index.html`, `frontend/jsconfig.json`, `frontend/components.json`, `frontend/.oxlintrc.json`, `frontend/.env.example`, `frontend/.gitignore` | ⭐ | Frontend shell and tooling config. |
| `frontend/README.md` | ⭐⭐ | Frontend run and layout notes. |
| `README.md` | ⭐⭐ | The main run/API/security guide. |
| `docs/scope.md`, `docs/verification.md`, `docs/work-allocation.md` | ⭐⭐ | Scope, recorded verification and this allocation. |

---

## Files shared across members

A few files start with one member's comment but contain a clearly marked section from another:

| File | Primary owner | Added by | What was added |
|---|---|---|---|
| `database/setup.sql` | 1 | 4 | `reviews` and `review_summary` tables. |
| `database/seed.sql` | 1 | 4 | Four demo reviewers and their Notebook reviews. |
| `.env.example` | 1 | 4 | The AI settings block. |
| `repositories/ProductRepository.java` | 2 | 4 | `findCatalogue` and `search` for the assistant. |
| `services/ProductService.java` | 2 | 4 | Deletes a product's reviews and cached summary first. |

The Maven wrapper files (`mvnw`, `mvnw.cmd`, `.mvn/**`) are copied vendor tooling and keep their
original license comments; they are not part of the team's work.
