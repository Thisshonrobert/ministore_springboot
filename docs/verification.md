# Verification — 12 October 2026

- Java 17.0.12, Spring Boot 3.4.1, local MySQL Server 8.0.45.
- Maven package succeeded; 10 integration tests, 0 failures/errors/skips (isolated H2 test profile).
- Real MySQL startup passed Hibernate schema validation with the dedicated app account.
- 41 API requests checked CRUD, duplicate email/cart, invalid/missing records, quantity limits,
  fractional quantity rejection, simultaneous additions, blocked deletions, clear/remove persistence,
  cart deletion preserving customer/products, DTO mapping, and totals.
- Application was stopped and restarted. The same customer/cart IDs, two items and total 850.00 survived.
- Browser checks: create category; create/filter product; create customer; display duplicate-email error;
  create customer cart; add product; update quantity; remove item. Narrow-window layout inspected.
- The new SQL schema contains five tables in mini_store; the original store_api database was not modified.
- Original source repository remains unchanged.

## Local demo data

Stationery: Notebook 250.00, Pen 100.00. Demo Customer's cart: 3 notebooks + 1 pen = 850.00.
Books: Spring Notes 150.00. Team Customer has an empty cart, created through the UI checks.
These rows are in this machine's MySQL database; setup.sql on another machine starts empty.

## Limits and setup

Category ID intentionally retains the plan's Byte/TINYINT type: at most 127 generated IDs,
including IDs of deleted categories. For a larger catalog, widen entity/DTO/controller/repository IDs and SQL together.
Totals use current prices; there is no checkout, historical pricing or inventory.
The local app is for learning and has no authentication.
Credentials are in the private .env file. Workbench setup and environment instructions are in README.md.
