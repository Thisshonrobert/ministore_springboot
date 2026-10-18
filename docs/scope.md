# Implemented updated scope

Four complete backend feature slices: categories, products, customer records, and one customer-linked cart.
Includes CRUD/filtering, totals, DTOs, explicit mapper classes, validation, custom exceptions and one JSON error handler.
Retains source package names, Byte/Long/UUID IDs and the original cart total/helper approach.
Uses explicit Java accessors, constructors and mapper classes instead of Lombok, MapStruct and Thymeleaf.
Simplifies security/customer/product dependencies and replaces Flyway with local SQL setup plus Hibernate validation.
No login/JWT/passwords, profile/address/wishlist, checkout/orders/payment/inventory or bundled UI.
The REST API is ready for a separate React frontend.

Adjustments: add-to-cart consistently returns 200; setup.sql is rerunnable without resetting records;
Cart.dateCreated is initialized in Java rather than by a DB default.
The app preserves the original repository and uses a separate database.
