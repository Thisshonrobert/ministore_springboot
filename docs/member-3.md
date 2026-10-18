# Member 3 — Customers and shared errors


## First understand these

- **JPA is a specification:** it defines how Java objects interact with relational databases.
- **Hibernate implements JPA:** it performs the actual object/table mapping and SQL work.
- **Spring Data JPA** builds repository implementations using JPA. You usually write an interface.
- **Entity** = database object; **DTO** = JSON sent to/from the browser.
- **Mapper classes** convert entities to DTOs with ordinary Java assignments. They do not query the database.
- **Getters and setters** are written explicitly on entities and DTOs so the data flow is visible.

Shared reading first: `README.md` → `pom.xml` (dependencies) →
`src/main/resources/application.yaml` (MySQL and `.env`) →
`src/main/java/com/codewithmosh/store/StoreApplication.java` (`@SpringBootApplication` starts/configures the app).

Below, Java paths are relative to `src/main/java/com/codewithmosh/store/`.


## Read your files in this order

1. `entities/User.java`: represents a customer with id/name/email only. `@Entity` maps a table; `@Id/@GeneratedValue` identify its generated key; `@Column(unique=true)` documents the email constraint. The UI says Customers; Java/API retain User and /users.
2. `repositories/UserRepository.java` → `repositories/CartRepository.java`: JpaRepository supplies CRUD. `existsByEmailAndIdNot` detects another customer's email while allowing an unchanged email; `existsByUserId` checks whether a customer has a cart.
3. `dtos/CreateCustomerRequest.java` → `dtos/UserDto.java`: `@NotBlank` requires text; `@Email` validates email shape, not mailbox existence; `@Size` limits length. DTOs expose only name/email/ID.
4. `mappers/UserMapper.java`: a plain `@Component` class with explicit assignments; `toEntity` ignores the ID and `update` changes only name/email.
5. `services/CustomerService.java`: `@Service` marks a class as a service-layer Spring bean. Its explicit constructor enables constructor injection. `@Transactional` commits entity changes or rolls back failures. Lowercase email gives consistent duplicate checks; the unique DB constraint is the final protection. Customer deletion requires deleting their cart first.
6. `controllers/UserController.java`: `@RestController` sends JSON; `@RequestBody` reads JSON; `@Valid` checks constraints before the service. POST/PUT/DELETE create/update/delete records.
7. `exceptions/ResourceNotFoundException.java` → `ConflictException.java` → `BusinessRuleException.java`: custom RuntimeExceptions describe missing resources, conflicting changes, and invalid business actions. Services throw these; mappers do not.
8. `dtos/ErrorResponse.java`: a Java record holds message and fieldErrors. A record supplies constructor/accessors; HTTP status stays in the response, not duplicated in JSON.
9. `controllers/GlobalExceptionHandler.java`: `@RestControllerAdvice` shares JSON error handling across controllers. `@ExceptionHandler` chooses a handler by exception type. Validation/parsing → 400; missing → 404; duplicate/FK constraint → 409. Unexpected errors are logged and return generic 500. Map of fieldErrors identifies invalid inputs.
10. The customer pages are gone with Thymeleaf. A client calls `/users` and renders the returned JSON; validation errors still arrive as `message` plus `fieldErrors`.
11. `database/setup.sql`: unique email and restrictive carts.user_id foreign key back up service checks. This customer module has no login or passwords.

## Trace and demo

Submit a bad email: `POST /users` → UserController @Valid → validation exception → GlobalExceptionHandler → 400/ErrorResponse → JSON fieldErrors.
Create/edit a valid customer, repeat their email to get 409, and show deletion blocked while they have a cart.
Explain: validation versus business rule? Why use one exception handler? Why allow unchanged email on update?
