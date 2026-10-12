# Member 2 — Products


## First understand these

- **JPA is a specification:** it defines how Java objects interact with relational databases.
- **Hibernate implements JPA:** it performs the actual object/table mapping and SQL work.
- **Spring Data JPA** builds repository implementations using JPA. You usually write an interface.
- **Entity** = database object; **DTO** = JSON sent to/from the browser.
- **MapStruct** generates entity/DTO conversion code at compile time. It does not query the database.
- **Lombok** generates repetitive code: `@Getter/@Setter` accessors, `@Data` accessors and other methods.

Shared reading first: `README.md` → `pom.xml` (dependencies/processors) →
`src/main/resources/application.yaml` (MySQL and `.env`) →
`src/main/java/com/codewithmosh/store/StoreApplication.java` (`@SpringBootApplication` starts/configures the app).

Below, Java paths are relative to `src/main/java/com/codewithmosh/store/`.


## Read your files in this order

1. `entities/Category.java` → `entities/Product.java`: `@Entity` maps Product to a table. `@ManyToOne` means many products share one category; `@JoinColumn` identifies category_id, the foreign key. `LAZY` loads the category when accessed. No cascade means product deletion does not delete its category.
2. `repositories/ProductRepository.java` → `repositories/CategoryRepository.java` → `repositories/CartItemRepository.java`: JpaRepository supplies CRUD. Spring derives SQL from `findByCategoryId`/`existsByProductId` method names. The item check prevents deleting a product still in a cart.
3. `dtos/ProductRequest.java` → `dtos/ProductDto.java`: JSON uses categoryId instead of a whole Category object. `@NotNull` requires a value; `@NotBlank` requires text; `@DecimalMin` requires positive price; `@Digits` limits decimal precision; `@Positive` checks category ID.
4. `mappers/ProductMapper.java`: `@Mapper(componentModel="spring")` generates a Spring bean. `@Mapping(source="category.id",target="categoryId")` flattens the relation. `@MappingTarget` modifies an existing Product; id/category are ignored so the service controls them. Read generated `ProductMapperImpl.java` under target after compile.
5. `services/ProductService.java`: `@Service` marks a class as a service-layer Spring bean. Constructor injection supplies repositories/mapper. `@Transactional` keeps category lookup, change and DTO mapping inside one transaction. `readOnly=true` describes reads. Missing category → 404; used product deletion → 409.
6. `controllers/ProductController.java`: `@RestController` returns JSON. `@RequestParam(required=false)` reads the optional category filter; `@PathVariable` reads ID; `@RequestBody` reads JSON; `@Valid` applies DTO validation. POST returns 201 with a Location header.
7. `controllers/GlobalExceptionHandler.java`: Member 3 converts validation exceptions into 400 plus fieldErrors, and domain exceptions into 404/409.
8. `src/main/resources/templates/products.html`: form, category dropdown, filter, table. HTML number constraints help users; the backend still validates.
9. `src/main/resources/static/js/api.js` → `static/js/products.js`: loads categories for labels/dropdowns, sends categoryId/price as numbers, and calls the filtered products API. DOM rendering uses textContent.
10. `database/setup.sql`: products.category_id references categories.id. DECIMAL(10,2) stores money; BigDecimal performs decimal arithmetic without binary floating-point rounding.

## Trace and demo

Create Notebook at 250.00: products.js → ProductController → ProductService → category lookup + mapper → ProductRepository → MySQL → ProductDto.
Edit it, filter by category, show price 0 rejected, and show deletion blocked while it is in a cart.
Explain: why categoryId in a DTO? Why BigDecimal? Why does the service fetch a real category rather than let the mapper query it?
