# Member 1 — Categories


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

1. `database/setup.sql`: creates the five tables. Primary key identifies a row; foreign key references another table. Your category ID uses signed TINYINT/Java Byte (max 127).
2. `entities/Category.java`: `@Entity` maps a class to a table; `@Table` names it. `@Id` marks its key; `@GeneratedValue(IDENTITY)` lets MySQL assign it. `@OneToMany(mappedBy="category")` means Product.category owns the relationship; deleting a category does not delete products.
3. `repositories/CategoryRepository.java`: `JpaRepository<Category, Byte>` supplies findAll/findById/save/delete. The second type is the ID type.
4. `dtos/CategoryRequest.java` → `dtos/CategoryDto.java`: request contains name; response also contains ID. `@NotBlank` rejects empty/whitespace names; `@Size` limits length.
5. `mappers/CategoryMapper.java`: `@Mapper(componentModel="spring")` generates a Spring bean. `@MappingTarget` updates the existing entity; ignored ID/products stay unchanged. Read `target/generated-sources/annotations/.../CategoryMapperImpl.java` after compilation.
6. `services/CategoryService.java`: `@Service` marks a class as a service-layer Spring bean. `@RequiredArgsConstructor` creates a constructor for final dependencies; Spring injects them. `@Transactional` groups DB work; managed entity changes are saved on commit. ProductRepository prevents deleting an occupied category.
7. `controllers/CategoryController.java`: `@RestController` returns JSON. `@RequestMapping` sets the route. GET reads, POST creates, PUT replaces fields, DELETE removes. `@RequestBody` reads JSON; `@Valid` checks the request; `@PathVariable` reads the ID.
8. `controllers/GlobalExceptionHandler.java`: Member 3's `@RestControllerAdvice` translates your not-found/conflict exceptions into 404/409 responses.
9. `controllers/HomeController.java` → `templates/fragments.html` → `templates/categories.html` (resources): `@Controller` returns a Thymeleaf view name. `th:replace` includes shared navigation. Templates live in `src/main/resources/templates/`.
10. `static/js/api.js` → `static/js/categories.js` → `static/css/style.css` (resources): fetch sends HTTP; the script renders JSON into rows and submits forms. Shared helpers display server field errors. `textContent` displays user text safely. CSS handles layout and smaller screens.

## Trace and demo

Add Books: categories.js POST → CategoryController → CategoryService → mapper + repository → MySQL → DTO → table.
Create/rename/delete an unused category. Then create a product in another category and show that deleting that category returns 409.
Explain: why use a service? Why block deletion? What is the difference between the JSON DTO and the entity?
