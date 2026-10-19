<!-- member 4 -->
# AI features — the shop assistant and the review summary

This file describes the AI additions: a shopping chatbot and a review summariser. Both call Google
Gemini from the Spring Boot backend. The browser never sees an API key and never talks to the
provider directly.

The ideas come from a small Gemini chatbot/summariser reference project, but the architecture here is
deliberately different: no vector database, no RAG framework, and no AI SDK. The backend uses Spring's
own `RestClient`, and the facts in every answer are read from the store database first.

## Two features

| Feature | What it does | Where it lives |
|---|---|---|
| Shop assistant | Answers questions about products, prices and categories using the real catalogue. | `POST /chat` |
| Review summary | Writes a short positive/negative summary of a product's actual reviews. | `POST /products/{id}/reviews/summary` |

Supporting endpoints:

| Endpoint | Method | Notes |
|---|---|---|
| `/chat` | POST | `{ message }` (1–1000 chars). Public, CSRF-protected. Returns `{ reply, products }`. |
| `/products/{id}/reviews` | GET | Public. `{ productId, productName, reviewCount, averageRating, reviews, summary, summaryMessage }`. |
| `/products/{id}/reviews/summary` | POST | Public, CSRF-protected. Generates or returns the cached summary. |
| `/me/reviews` | POST | Signed-in customer only. `{ productId, rating, comment }`. Author comes from the session. |

## How an answer stays grounded

`ChatService.chat` never lets the model invent product facts:

1. It splits the question into a few keywords (words shorter than three characters and a small stop
   word list are dropped).
2. `ProductRepository.search` looks those keywords up in the product **name, description and
   category name** in MySQL. `findCatalogue` loads a short catalogue overview.
3. Both the matching rows and the catalogue are written into the system instruction
   (`prompts/store-assistant.txt`, `{{catalog}}`).
4. `GeminiAiClient.generate` asks the provider for an answer.
5. The **same** matching rows are returned in `ChatResponse.products`, so the storefront can show the
   products the answer was based on and the customer can check them against the shelves.

The prompt tells the model it is a small shop assistant that may only use the catalogue text, that
stock is not tracked, and that it cannot change carts or place orders. A question the store cannot
answer from those instructions gets a plain "I do not have that information" style reply rather than
a guess.

There is no conversation memory: each message is answered on its own. That keeps the endpoint simple
and stateless, which is enough for a small shop.

## How a review summary stays grounded

`ReviewService.summarize`:

1. Reads the **10 newest reviews** of the product from the `reviews` table.
2. Returns a plain "not enough reviews yet" message when there are fewer than
   `REVIEWS_MINIMUM_FOR_SUMMARY` (default 3). This is a normal answer, not an error, and no AI call is
   made.
3. Otherwise sends the real review text to the model with `prompts/summarize-reviews.txt`, asking for
   the points customers liked and the complaints.
4. Stores the result in `review_summary` (one row per product) with `generatedAt` and `expiresAt`,
   and returns it.

A cached summary is used while it is fresh. It is thrown away when a customer writes a new review,
and it expires after `REVIEW_SUMMARY_TTL_HOURS` (default 168 hours = one week), so the next request
regenerates it.

## Reviews

- Only a signed-in customer can review, and the author is read from
  `AuthService.SESSION_CUSTOMER_ID` in the session. The frontend never sends a customer ID.
- One review per customer per product: the `reviews` table has a unique `(product_id, user_id)`
  constraint, and a duplicate returns 409.
- Rating is 1–5 and the comment is 1–1000 characters, both validated with Bean Validation.
- `ProductService.delete` deletes a product's reviews and its cached summary first, so deleting a
  product works on both H2 and MySQL without a database-level cascade.

## Configuration

All settings come from the environment (see `.env.example`). The key is backend-only.

| Variable | Default | Purpose |
|---|---|---|
| `AI_API_KEY` | *(empty)* | Google AI Studio key. Without it the AI endpoints return 503. |
| `AI_MODEL` | `gemini-3.5-flash` | Model name used in the request URL. |
| `AI_BASE_URL` | `https://generativelanguage.googleapis.com/v1beta` | Gemini API base URL. |
| `AI_TIMEOUT_SECONDS` | `30` | Request timeout. |
| `AI_MAX_OUTPUT_TOKENS` | `400` | Maximum answer length. |
| `AI_THINKING_BUDGET` | `-1` | `-1` omits the thinking setting; `0` turns thinking off where supported. |
| `REVIEWS_MINIMUM_FOR_SUMMARY` | `3` | Fewer reviews than this are not summarised. |
| `REVIEW_SUMMARY_TTL_HOURS` | `168` | How long a cached summary stays fresh. |

The app **starts without a key**. When the key is missing, rejected (403), or out of quota (429), the
AI endpoints return 503 with a clear message (`AiUnavailableException`) instead of crashing; the rest
of the store keeps working.

## Database

Two tables were added (see `database/ai-migration.sql`, idempotent, and the matching part of
`database/setup.sql`):

- `reviews` — one review per customer per product, with a unique constraint on `(product_id, user_id)`.
- `review_summary` — one cached summary per product, with `content`, `generated_at` and `expires_at`.

Hibernate uses `ddl-auto: validate`, so these tables must exist before the app starts. Run
`database/ai-migration.sql` once on an existing database, or use the updated `database/setup.sql` for
a fresh one.

## Backend files to read

| File | Purpose |
|---|---|
| `ai/AiClient.java` | The narrow interface the services depend on, so tests can replace the provider. |
| `ai/GeminiAiClient.java` | `RestClient` call to `generateContent`; maps provider errors to 503. |
| `ai/AiPrompts.java` | Loads and fills the two prompt templates. |
| `services/ChatService.java` | Keyword search, grounded prompt, returns the matched products. |
| `services/ReviewService.java` | Review creation, duplicate handling, summary cache and invalidation. |
| `controllers/ChatController.java`, `controllers/ReviewController.java` | The HTTP endpoints above. |
| `entities/Review.java`, `entities/ReviewSummary.java` | The two new tables. |
| `resources/prompts/store-assistant.txt`, `resources/prompts/summarize-reviews.txt` | Prompt templates. |

## Frontend files to read

| File | Purpose |
|---|---|
| `frontend/src/components/site/ChatWidget.jsx` | Floating assistant panel; sends one question and lists the returned products. |
| `frontend/src/pages/ProductPage.jsx` | Product detail page with add-to-cart and the review section. |
| `frontend/src/components/site/ProductReviews.jsx` | The review list, the AI summary card, and the signed-in review form. |
| `frontend/src/lib/chat.js`, `lib/reviews.js` | Thin wrappers over `POST /chat` and the review endpoints. |

## Why there is no AI SDK

The provider request is one JSON `POST` with a small body. `RestClient` plus a tiny client class keeps
the dependency list unchanged, keeps the code readable for a learner, and makes the seam for tests
obvious (`AiClient` is replaced with a stub in `AiFeatureIntegrationTests`).

## Limits and honest caveats

- **Live AI output.** The original key could not be used: every model returned `403 PERMISSION_DENIED`
  ("Your project has been denied access"), `429` with a quota limit of 0, or `404` for retired models.
  A later key with `AI_MODEL=gemini-3.1-flash-lite` works, and on 19 October 2026 the live review
  summary was generated for the seeded Notebook (4 reviews, average 4.0), cached and read back, so the
  real summary path is now demonstrated as well. The suite still uses a stubbed `AiClient`, so it needs
  no key and makes no network calls. See `docs/verification.md`.
- The catalogue overview is capped at 40 products and the matched products at 5, which is fine for a
  mini store and keeps the prompt small.
- The summary reads only the 10 newest reviews.
- The assistant cannot see stock, orders, accounts or carts and cannot change anything.
