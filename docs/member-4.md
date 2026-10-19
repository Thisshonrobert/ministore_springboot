<!-- member 4 -->
# Member 4 — Customer-linked cart and integration

> Historical walkthrough: the `/carts` and `/users/{id}/cart` endpoints and `CartController` described
> here were removed. The cart is now reachable only through `/me/cart` (Phase 2).


## First understand these

- **JPA is a specification:** it defines how Java objects interact with relational databases.
- **Hibernate implements JPA:** it performs the actual object/table mapping and SQL work.
- **Spring Data JPA** builds repository implementations using JPA. You usually write an interface.
- **Entity** = database object; **DTO** = JSON sent to/from the browser.
- **Mapper classes** convert entities to DTOs with ordinary Java assignments. They do not query the database.
- **Getters and setters** are written explicitly on entities and DTOs so the data flow is visible.

Shared reading first: `README.md` → `pom.xml` (dependencies) →
`src/main/resources/application.yaml` (MySQL and `.env`) →
`src/main/java/com/accenture/store/StoreApplication.java` (`@SpringBootApplication` starts/configures the app).

Below, Java paths are relative to `src/main/java/com/accenture/store/`.


## Read your files in this order

1. `entities/User.java` → `entities/Product.java` → `entities/CartItem.java`: an item links a cart and product through foreign keys. `@ManyToOne` means many items may reference one cart/product. quantity × current product.price produces a BigDecimal line total.
2. `entities/Cart.java`: `@Entity` maps carts; `@GeneratedValue(UUID)` generates the ID. `@OneToOne` plus unique user_id allows at most one cart per customer; no cascade to User keeps the customer safe. `@OneToMany(mappedBy="cart",cascade=ALL,orphanRemoval=true)` saves/deletes cart items with the cart and deletes removed items. addItem increments an existing item; getTotalPrice sums line totals.
3. `repositories/CartRepository.java` → `CartItemRepository.java` → `UserRepository.java` → `ProductRepository.java`: JpaRepository supplies persistence. findByUserId loads a customer's cart; existsByUserId detects duplicates; existsByProductId protects product deletion. `@Lock(PESSIMISTIC_WRITE)` locks the cart row until the mutation transaction finishes, so simultaneous additions do not lose increments. `@Query` defines the lookup in JPQL (entity names, not table names).
4. `dtos/AddItemsToCart.java` → `dtos/UpdateCartItemRequest.java`: a required product ID; quantity `@Min(1)/@Max(100)`. `@NotNull` rejects missing values.
5. `dtos/CartProductDto.java` → `CartItemDto.java` → `CartDto.java`: nested product summary, quantity, line total, cart ID, userId, items, totalPrice. DTOs avoid serializing the entity relationship graph.
6. `mappers/CartMapper.java`: a plain `@Component` class. `toCartDto` flattens `user.id` to `userId`, loops over items and calls the entity total methods; nested product summaries are mapped explicitly.
7. `services/CartService.java`: `@Service` marks a class as a service-layer Spring bean. Its explicit constructor supplies the repositories and mapper. `@Transactional` groups changes and commits managed entities; mapping inside it can access LAZY relationships even with open-in-view=false. Create checks customer existence/duplicate cart. Add checks the 100 limit before increment. Clear keeps the cart; delete removes the cart/items but preserves customer/products.
8. `controllers/MeController.java` → `GlobalExceptionHandler.java`: session-derived `/me/cart` routes, `@RequestBody`, `@Valid`, JSON responses. POST `/me/cart` returns 201; adding an item returns 200. A missing cart returns 404 so a client can offer creation, and a second cart returns 409.
9. There is no server-rendered cart page. The React storefront calls `/me/cart` and `/me/cart/items`; the backend reads the customer from the session. The Phase 1 management API (`/users/{id}/cart`, `/carts/{id}`) was removed in Phase 2, so `/me/cart` is the only cart API. MySQL remains the source of truth.
10. `database/setup.sql` → `.env.example` → `application.yaml`: BINARY(16) stores UUIDs; unique user_id enforces ownership; unique(cart_id,product_id) prevents duplicate rows. FK and CHECK constraints protect relationships/quantities. ddl-auto=validate checks the schema without changing it. Jackson's accept-float-as-int=false rejects fractional quantities instead of rounding them down.
11. `src/test/java/com/accenture/store/CartIntegrationTests.java` → `src/test/resources/application-test.yaml`: SpringBootTest runs real app components; MockMvc sends HTTP-like requests; H2 isolates test records. The cart tests live in `CartIntegrationTests`, with the shared error shape in `ErrorHandlingIntegrationTests`; both extend `IntegrationTestSupport`. Tests use separate committed requests so they catch persistence problems. `docs/verification.md` records real MySQL checks.

## Trace and demo

Add Notebook: `POST /me/cart/items` → MeController → CartService → repositories → Cart.addItem → cascade persistence → CartMapper → JSON → refreshed total.
Use price 250 × quantity 3 = 750. Show duplicate-cart 409 and quantity 101 rejected. Remove/clear items, restart, reload the customer's cart, and delete it while preserving customer/products.
Explain: why a separate CartItem? Cascade versus orphanRemoval? Why does clear differ from delete? Why transaction + DTO mapping together?
