<!-- member 4 -->
# Implemented updated scope

Four complete backend feature slices from Phase 1: categories, products, customer records, and one
customer-linked cart. Phase 2 adds customer registration, login, logout, current-customer lookup,
server-side session state, CSRF protection, credentialed CORS, and a React storefront.

Includes CRUD/filtering, totals, DTOs, explicit mapper classes, validation, custom exceptions, one
JSON error handler, PBKDF2 password hashing, session-derived `/me` cart operations, and session-ID
rotation. Retains source package names, Byte/Long/UUID IDs, and the original cart total/helper
approach. Uses explicit Java accessors, constructors, and mapper classes instead of Lombok, MapStruct,
and Thymeleaf.

The frontend uses React 19, Vite, Tailwind CSS, and shadcn-style components with functional
components and basic hooks. Authentication state is restored from `/auth/me` after a refresh; the
session cookie is HttpOnly and nothing is stored in `localStorage`.

Excluded: Spring Security, JWT/OAuth, password reset, email verification, roles/administration,
addresses, profiles, wishlist, checkout, orders, payment, inventory, uploads, pagination, and a
bundled production server for the React build. The legacy `/users` and `/carts` management
endpoints have been removed; the customer frontend and the tests use only `/auth` and `/me`.

Phase 3 adds customer reviews and two AI features: a grounded shopping assistant (`POST /chat`) and an
AI summary of a product's reviews (`POST /products/{id}/reviews/summary`). Both call Google Gemini from
the backend only. Products and review text are read from the database first and placed in the prompt,
and the assistant returns the same rows it was grounded in, so no product fact comes from the model.
There is no vector database, RAG framework, or AI SDK: the provider call is a plain Spring `RestClient`
request behind a small `AiClient` interface. Misconfigured, denied, or over-quota providers return 503
rather than breaking the store, and a product with too few reviews is answered with a normal message.
The React storefront gains a product detail page, a review section, and a floating assistant panel.
See `docs/ai-features.md`.
