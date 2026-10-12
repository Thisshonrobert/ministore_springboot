# Mini Store

A reduced version of [Mosh's Spring Boot store](https://github.com/mosh-hamedani/spring-api-starter).
Adapted from the local source in `F:/mottamosh/spring-api-starter` using the updated four-member plan.
One Java 17 / Spring Boot 3.4.1 application, MySQL 8, Thymeleaf pages, vanilla JavaScript, Lombok, MapStruct 1.5.5.Final.

## Run on this machine

The project uses its own `mini_store` database. Local database initialization and verification status are in `docs/verification.md`.
A generated local password is in `.env`; the app reads it automatically when started from this folder.

```powershell
cd C:\Users\thiss\Desktop\dev\sprinboot_mini_store
.\mvnw.cmd spring-boot:run
```

The local MySQL database and dedicated app account have been created; `.env` is configured.
Demo data is available locally (see `docs/verification.md`).

Open http://localhost:8080. Start with Categories, then Products, Customers, and Cart.
To use another port, edit `SERVER_PORT` in `.env`. Stop with Ctrl+C.

## Set up MySQL on another machine (Workbench)

Workbench is the client; MySQL Server must also be installed and running locally.

1. Connect to your local MySQL server in Workbench with an administrator account.
2. File â†’ Open SQL Script â†’ `database/setup.sql`, then execute the entire script using the lightning button.
3. Refresh Schemas. Confirm `mini_store` contains `categories`, `products`, `users`, `carts`, `cart_item`.
4. For a dedicated account, execute this SQL with your chosen password (skip `CREATE USER` if the account already exists):

```sql
CREATE USER 'mini_store_app'@'localhost' IDENTIFIED BY 'your_chosen_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON mini_store.* TO 'mini_store_app'@'localhost';
```

5. Copy `.env.example` to `.env`. Set `DB_USERNAME` and `DB_PASSWORD` to the account above, or another local account with access to `mini_store`.
6. With the application stopped, open `database/seed.sql` in Workbench and execute the entire script. Run it in one session at a time. On an SQL error, execute `ROLLBACK;` and resolve the error before retrying.
7. From the project folder, start the application (Java 17 is the tested setup):

```powershell
.\mvnw.cmd spring-boot:run
```

Schema setup and demo-data seeding are separate, manual operations. `setup.sql` creates the tables;
`seed.sql` inserts missing demo records. Neither the seed script nor demo-data initialization runs on application startup.
On an empty database the seed creates Stationery (Notebook 250.00, Pen 100.00), Books (Spring Notes 150.00),
Demo Customer (`demo@ministore.example`) with 3 notebooks and 1 pen totaling 850.00, and Team Customer
(`team@ministore.example`) with an empty cart. In the UI, open Cart and select Demo Customer to load it.

Reruns reuse categories by name, products by name/category/price, customers by the reserved demo emails,
and carts by customer. Where matching category/product rows already exist, the lowest ID is used.
Names are not unique in this schema, so run the seed sequentially with the app stopped.
Existing records, cart UUIDs, prices and quantities are preserved; seeding does not reset an edited demo cart
or empty an existing Team cart. Consequently an existing edited cart may have a different total.
Products with the same name but a different price are preserved alongside the demo product.
New cart UUIDs use `UUID_TO_BIN(..., 0)` for the standard `BINARY(16)` byte order used by JPA.

`.env` uses `KEY=value` properties syntax, without surrounding quotes. Keep it private; `.gitignore` excludes it.
OS environment variables can also supply `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT`.
Hibernate uses `ddl-auto: validate`: it checks the SQL schema, and does not create or reset it.
`setup.sql` can be rerun without deleting records; it does not upgrade existing tables.
H2 is only a test dependency; the running app uses local MySQL.

## Scope and ownership

| Member | Feature | Shared work |
|---|---|---|
| 1 | Category CRUD | Navigation, CSS, HomeController, SQL setup |
| 2 | Product CRUD + category filter | Product/category mapping |
| 3 | Customer name/email CRUD | Exceptions, error DTO, shared API helper |
| 4 | Customer-linked cart | Build/configuration, totals, integration tests |

Java and JavaScript begin with `//member N`; HTML/CSS/SQL/configuration use their valid comment syntax.
Generated MapStruct implementations are build output; read them but do not edit them.
Maven wrapper files are copied vendor tooling, with their original license comments preserved.

Read your learning guide: `docs/member-1.md`, `docs/member-2.md`, `docs/member-3.md`, or `docs/member-4.md`.
Each guide gives the reading order, key annotations, and a short mentor demo.

## API agreement

| Endpoint | Methods | Request |
|---|---|---|
| `/categories`, `/categories/{id}` | GET/POST, GET/PUT/DELETE | name |
| `/products`, `/products/{id}` | GET/POST, GET/PUT/DELETE | name, description, price, categoryId |
| `/products?categoryId=1` | GET | optional category filter |
| `/users`, `/users/{id}` | GET/POST, GET/PUT/DELETE | name, email |
| `/carts` | POST | userId |
| `/users/{id}/cart`, `/carts/{id}` | GET | no body |
| `/carts/{id}` | DELETE | no body |
| `/carts/{id}/items` | POST/DELETE | productId for POST; DELETE clears |
| `/carts/{id}/items/{productId}` | PUT/DELETE | quantity for PUT |

Creates return 201, reads/updates return 200, deletions/clear return 204.
Add-to-cart always returns 200, including the first addition. Errors: 400 invalid input, 404 missing, 409 conflict, 500 unexpected.
Error body: `{ "message": "...", "fieldErrors": { "email": "..." } }`; fieldErrors is empty for non-validation errors.
One customer has at most one cart. Delete their cart before deleting the customer.
Cart UUIDs are generated by JPA; category IDs are Byte (1â€“127), other numeric IDs are Long.
Prices allow 8 integer digits and 2 decimal digits; quantity is 1â€“100. Totals use current product prices.

## Tests

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Ten integration scenarios test real controllers/services/repositories with an isolated in-memory H2 database.
They verify DTO mapping, relationships, error responses and persistence across HTTP requests.
For real MySQL/restart verification, see `docs/verification.md`.

Excluded: login/JWT/passwords, addresses, profiles, wishlist, checkout, orders, payments, inventory, uploads, pagination.
This is a local management/learning app. Customer selection is not login.
