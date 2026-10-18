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
bundled production server for the React build. The original `/users` and `/carts` management
endpoints remain for the learning tests and are not used by the customer frontend.
