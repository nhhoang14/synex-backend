# Synex Backend

Spring Boot backend for an e-commerce system with JWT authentication, role-based authorization, cart management, checkout, order history, user profile APIs, and admin APIs.

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- JWT (jjwt)
- MySQL
- Lombok

## Features

- User registration, login, logout, and token refresh
- JWT authentication filter and role-based access control
- Public product, category, and brand listing APIs
- Cart management
- Shipping address management for the logged-in user (max 3 addresses per account)
- Checkout and order creation
- Order history for customers
- Profile update and password change
- Admin APIs for managing users, products, categories, brands, and orders

## Project Structure

- `src/main/java/com/nhhoang/synexbackend/config` - security and JWT configuration
- `src/main/java/com/nhhoang/synexbackend/controller` - public and user-facing controllers
- `src/main/java/com/nhhoang/synexbackend/controller/admin` - admin controllers
- `src/main/java/com/nhhoang/synexbackend/dto` - request/response DTOs
- `src/main/java/com/nhhoang/synexbackend/model` - JPA entities
- `src/main/java/com/nhhoang/synexbackend/repository` - Spring Data repositories
- `src/main/java/com/nhhoang/synexbackend/service` - application services

## Environment Variables

The app reads configuration from `.env` via `application.properties`.

Required variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

Example:

```properties
DB_URL=jdbc:mysql://localhost:3306/synex_db
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_long_secret_key
```

## Run Locally

1. Make sure MySQL is running and the database exists.
2. Set the environment variables above.
3. Run the application:

```bash
./mvnw spring-boot:run
```

To compile only:

```bash
./mvnw -DskipTests compile
```

## API Overview

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/refresh`

### Public

- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/categories`
- `GET /api/brands`

### User

- `GET /api/users/me`
- `PUT /api/users/me`
- `POST /api/users/me/password`
- `GET /api/users/me/addresses`
- `POST /api/users/me/addresses`
- `GET /api/users/me/addresses/{addressId}`
- `PUT /api/users/me/addresses/{addressId}`
- `DELETE /api/users/me/addresses/{addressId}`
- `PATCH /api/users/me/addresses/{addressId}/default`

### Cart

- `POST /api/cart` - create or return the current user's cart
- `POST /api/cart/add?productId=1&quantity=2` - add a product to the current user's cart; `cartId` is optional
- `GET /api/cart/me` - get the current user's cart
- `PATCH /api/cart/items/{productId}/increase?amount=1` - increase quantity of a cart item
- `PATCH /api/cart/items/{productId}/decrease?amount=1` - decrease quantity of a cart item (minimum quantity is 1)
- `DELETE /api/cart/items/{productId}` - remove a product from the current user's cart
- `GET /api/cart/{cartId}` - get a cart by id

### Orders

- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/{orderId}`

## Shipping Address Rules

- One account can have at most 3 shipping addresses.
- The first address is automatically set as default.
- At any time, only one address can be default.
- If a default address is deleted, the system automatically assigns another existing address as default.
- You cannot unset the current default directly; set another address as default first.

## Checkout Address Behavior

For `POST /api/orders`:

- If `shippingAddressId` is provided, the order uses that address (must belong to current user).
- If `shippingAddressId` is omitted or null, the system uses the user's default address.
- If no default address exists, the order request is rejected.

Example using default address automatically:

```json
{
	"paymentMethod": "COD"
}
```

Example selecting a specific address:

```json
{
	"shippingAddressId": 12,
	"paymentMethod": "CARD"
}
```

Note for UI flow:

- If user does not change anything at checkout, submit order without `shippingAddressId` to use default.
- If user wants another address, user must explicitly select it.
- If user wants to edit address info or change which one is default, user must update address first via address APIs, then place order.

### Admin

- `GET /api/admin/users`
- `GET /api/admin/users/{id}`
- `PUT /api/admin/users/{id}/role`
- `DELETE /api/admin/users/{id}`
- `GET /api/admin/products`
- `POST /api/admin/products`
- `PUT /api/admin/products/{id}`
- `DELETE /api/admin/products/{id}`
- `GET /api/admin/categories`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `DELETE /api/admin/categories/{id}`
- `GET /api/admin/brands`
- `POST /api/admin/brands`
- `PUT /api/admin/brands/{id}`
- `DELETE /api/admin/brands/{id}`
- `GET /api/admin/orders`
- `GET /api/admin/orders/{orderId}`
- `PATCH /api/admin/orders/{orderId}/status`
- `GET /api/admin/contact-messages`
- `PATCH /api/admin/contact-messages/{id}/status`
- `DELETE /api/admin/contact-messages/{id}`

## Notes

- Admin endpoints require `ADMIN` role.
- The project uses Lombok annotation processing in Maven, so `mvn compile` works with generated getters/setters.
- `spring.jpa.hibernate.ddl-auto=update` is enabled for development.

## License

No license has been defined yet.
