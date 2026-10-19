<!-- member 1 -->
# Member 1 — Categories


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

1. `database/setup.sql`: creates the seven tables (the five Phase 1 tables plus `reviews` and `review_summary` for Phase 3). Primary key identifies a row; foreign key references another table. Your category ID uses signed TINYINT/Java Byte (max 127).
2. `entities/Category.java`: `@Entity` maps a class to a table; `@Table` names it. `@Id` marks its key; `@GeneratedValue(IDENTITY)` lets MySQL assign it. `@OneToMany(mappedBy="category")` means Product.category owns the relationship; deleting a category does not delete products.
3. `repositories/CategoryRepository.java`: `JpaRepository<Category, Byte>` supplies findAll/findById/save/delete. The second type is the ID type.
4. `dtos/CategoryRequest.java` → `dtos/CategoryDto.java`: request contains name; response also contains ID. `@NotBlank` rejects empty/whitespace names; `@Size` limits length.
5. `mappers/CategoryMapper.java`: a plain `@Component` class with explicit assignments. `toEntity` leaves the ID/products unset; `update` changes only the name of the existing entity.
6. `services/CategoryService.java`: `@Service` marks a class as a service-layer Spring bean. The explicit constructor receives the repositories and mapper; Spring injects them. `@Transactional` groups DB work; managed entity changes are saved on commit. ProductRepository prevents deleting an occupied category.
7. `controllers/CategoryController.java`: `@RestController` returns JSON. `@RequestMapping` sets the route. GET reads, POST creates, PUT replaces fields, DELETE removes. `@RequestBody` reads JSON; `@Valid` checks the request; `@PathVariable` reads the ID.
8. `controllers/GlobalExceptionHandler.java`: Member 3's `@RestControllerAdvice` translates your not-found/conflict exceptions into 404/409 responses.
9. The backend returns JSON only. There are no Thymeleaf templates or server-rendered pages; a React frontend can call the same endpoints.
10. `src/test/java/com/accenture/store/CategoryIntegrationTests.java`: your slice's Spring `MockMvc` test. It extends `IntegrationTestSupport`, which starts a session, sends the CSRF token and builds category/product fixtures. Run it with `.\mvnw.cmd test`.

## Trace and demo

Add Books: `POST /categories` → CategoryController → CategoryService → mapper + repository → MySQL → DTO → JSON.
Create/rename/delete an unused category. Then create a product in another category and show that deleting that category returns 409.
Explain: why use a service? Why block deletion? What is the difference between the JSON DTO and the entity?
