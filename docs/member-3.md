<!-- member 3 -->
# Member 3 — Customers and shared errors

> Historical walkthrough: the `/users` endpoints, `UserController` and `CustomerService` described
> here were removed. Customer accounts are now managed only through `/auth` (Phase 2).


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

1. `entities/User.java`: represents a customer with id/name/email and a nullable PBKDF2 password hash. `@Entity` maps a table; `@Id/@GeneratedValue` identify its generated key; `@Column(unique=true)` documents the email constraint; `@JsonIgnore` keeps the hash out of accidental JSON. The UI says Customers, and since Phase 2 the API reaches the owner only through `/auth/me` (`/users` was removed).
2. `repositories/UserRepository.java` → `repositories/CartRepository.java`: JpaRepository supplies CRUD. `existsByEmailAndIdNot` detects another customer's email while allowing an unchanged email; `existsByUserId` checks whether a customer has a cart.
3. `dtos/RegisterRequest.java` → `dtos/UserDto.java`: `@NotBlank` requires text; `@Email` validates email shape, not mailbox existence; `@Size` limits length. DTOs expose only name/email/ID.
4. `mappers/UserMapper.java`: a plain `@Component` class with explicit assignments; `toEntity` ignores the ID and `update` changes only name/email.
5. `services/AuthService.java` (formerly `CustomerService`): `@Service` marks a class as a service-layer Spring bean. Its explicit constructor enables constructor injection. `@Transactional` commits entity changes or rolls back failures. Lowercase email gives consistent duplicate checks; the unique DB constraint is the final protection.
6. `controllers/AuthController.java` (formerly `UserController`): `@RestController` sends JSON; `@RequestBody` reads JSON; `@Valid` checks constraints before the service. `POST /auth/register` and the other `/auth` routes create and read the session customer.
7. `exceptions/ResourceNotFoundException.java` → `ConflictException.java` → `BusinessRuleException.java`: custom RuntimeExceptions describe missing resources, conflicting changes, and invalid business actions. Services throw these; mappers do not.
8. `dtos/ErrorResponse.java`: a Java record holds message and fieldErrors. A record supplies constructor/accessors; HTTP status stays in the response, not duplicated in JSON.
9. `controllers/GlobalExceptionHandler.java`: `@RestControllerAdvice` shares JSON error handling across controllers. `@ExceptionHandler` chooses a handler by exception type. Validation/parsing → 400; missing → 404; duplicate/FK constraint → 409. Unexpected errors are logged and return generic 500. Map of fieldErrors identifies invalid inputs.
10. The customer pages are gone with Thymeleaf. The React storefront calls `/auth` and `/me`; the Phase 1 `/users` management API was removed, so registration validation errors still arrive as `message` plus `fieldErrors`.
11. `database/setup.sql` and `database/auth-migration.sql`: unique email, nullable `password_hash`, and restrictive carts.user_id foreign key back up service checks. Authentication is implemented with ordinary Spring MVC and the JDK, not Spring Security.

## Phase 2 — customer authentication

Read these after the Phase 1 files above:

1. `dtos/RegisterRequest.java` and `dtos/LoginRequest.java`: Bean Validation on name, email, and password. `@Size(min=8, max=128)` is the password rule; the browser mirrors it for a faster message.
2. `services/PasswordHasher.java`: PBKDF2WithHmacSHA256, a random 16-byte salt, 210,000 iterations, and a 256-bit key. The stored string is `pbkdf2_sha256$iterations$salt$hash`; `MessageDigest.isEqual` compares bytes. A malformed stored hash fails closed.
3. `services/AuthService.java`: normalizes email, checks duplicates, saves the customer and an empty cart in one transaction, verifies login credentials, and exposes the current customer by ID. Authenticate failures all return the same 401 message.
4. `controllers/AuthController.java`: `/auth/csrf`, `/auth/register`, `/auth/login`, `/auth/logout`, `/auth/me`. After successful registration/login it calls `request.changeSessionId()`, stores `customerId`, and rotates the CSRF token. Logout invalidates the session.
5. `services/CsrfTokenService.java` and `config/CsrfInterceptor.java`: a synchronizer token lives in the session and mirrors to the `XSRF-TOKEN` cookie. Unsafe `/auth` and `/me` requests must echo it in `X-XSRF-TOKEN`.
6. `config/AuthInterceptor.java` and `config/WebConfig.java`: `/me/**` requires a session customer; CORS explicitly allows the React origins with credentials.
7. `controllers/MeController.java`: the cart endpoints use the session customer, not a customer ID from the client.
8. `src/main/resources/application.yaml`: HttpOnly, SameSite=Lax session cookie, 30-minute timeout, and allowed origins.
9. `database/auth-migration.sql`: the one-time `users.password_hash` upgrade and optional demo password hashes.
10. `src/test/java/com/accenture/store/CustomerAuthIntegrationTests.java`: registration, duplicate email, validation, login/logout, session rotation, CSRF rejection, session persistence, and cart isolation. `PasswordHasherTest.java` covers the hash format and verification.

Trace registration: `POST /auth/register` → CSRF interceptor → `AuthController` → `AuthService` → `PasswordHasher` → `UserRepository` + `CartRepository` → session ID/CSRF rotation → `UserDto` → React `AuthContext`. Explain why the session ID rotates, why the session is the identity source, and why the CSRF cookie is readable while `JSESSIONID` is not.

## Trace and demo

Submit a bad email: `POST /auth/register` → AuthController @Valid → validation exception → GlobalExceptionHandler → 400/ErrorResponse → JSON fieldErrors.
Create/edit a valid customer, repeat their email to get 409, and show deletion blocked while they have a cart.
Explain: validation versus business rule? Why use one exception handler? Why allow unchanged email on update?
