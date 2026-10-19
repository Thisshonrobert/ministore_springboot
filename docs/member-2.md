<!-- member 2 -->
# Member 2 — Products


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

1. `entities/Category.java` → `entities/Product.java`: `@Entity` maps Product to a table. `@ManyToOne` means many products share one category; `@JoinColumn` identifies category_id, the foreign key. `LAZY` loads the category when accessed. No cascade means product deletion does not delete its category.
2. `repositories/ProductRepository.java` → `repositories/CategoryRepository.java` → `repositories/CartItemRepository.java`: JpaRepository supplies CRUD. Spring derives SQL from `findByCategoryId`/`existsByProductId` method names. The item check prevents deleting a product still in a cart.
3. `dtos/ProductRequest.java` → `dtos/ProductDto.java`: JSON uses categoryId instead of a whole Category object. `@NotNull` requires a value; `@NotBlank` requires text; `@DecimalMin` requires positive price; `@Digits` limits decimal precision; `@Positive` checks category ID.
4. `mappers/ProductMapper.java`: a plain `@Component` class. `toDto` reads `category.id` into `categoryId`; `toEntity` and `update` copy only name, description and price, so the service controls id/category.
5. `services/ProductService.java`: `@Service` marks a class as a service-layer Spring bean. The explicit constructor supplies repositories/mapper. `@Transactional` keeps category lookup, change and DTO mapping inside one transaction. `readOnly=true` describes reads. Missing category → 404; used product deletion → 409.
6. `controllers/ProductController.java`: `@RestController` returns JSON. `@RequestParam(required=false)` reads the optional category filter; `@PathVariable` reads ID; `@RequestBody` reads JSON; `@Valid` applies DTO validation. POST returns 201 with a Location header.
7. `controllers/GlobalExceptionHandler.java`: Member 3 converts validation exceptions into 400 plus fieldErrors, and domain exceptions into 404/409.
8. `src/main/resources/application.yaml`: Jackson rejects a fractional value for an integer field, so quantity `1.9` becomes a 400 instead of being silently rounded.
9. `database/setup.sql`: products.category_id references categories.id. DECIMAL(10,2) stores money; BigDecimal performs decimal arithmetic without binary floating-point rounding.
10. `src/test/java/com/accenture/store/ProductIntegrationTests.java`: your slice's Spring `MockMvc` test for the category mapping, filter, update and validation. It extends `IntegrationTestSupport` (shared session, CSRF and fixture helpers). Run it with `.\mvnw.cmd test`.

## Trace and demo

Create Notebook at 250.00: `POST /products` → ProductController → ProductService → category lookup + mapper → ProductRepository → MySQL → ProductDto.
Edit it, filter by category, show price 0 rejected, and show deletion blocked while it is in a cart.
Explain: why categoryId in a DTO? Why BigDecimal? Why does the service fetch a real category rather than let the mapper query it?
