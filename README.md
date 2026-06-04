# Synex Backend

Synex Backend is a Spring Boot REST API for an e-commerce application. It covers authentication, product catalog, cart management, checkout, orders, shipping addresses, reviews, vouchers, contact messages, and admin operations.

## Tech Stack

- Java 25
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- Spring Security
- JWT with `jjwt`
- MySQL
- Local file storage for media uploads
- Lombok

## Main Features

- JWT authentication and role-based authorization
- Public APIs for products, categories, and brands
- Cart management for authenticated users
- Shipping address management with default address support
- Checkout from selected cart items
- Order history and order detail APIs
- Reviews, vouchers, and contact message APIs
- Admin APIs for users, products, variants, categories, brands, orders, vouchers, reviews, and contact messages

## Project Structure

- `src/main/java/com/nhhoang/synexbackend/config` - security, JWT, and static resource configuration
- `src/main/java/com/nhhoang/synexbackend/controller` - REST controllers
- `src/main/java/com/nhhoang/synexbackend/controller/admin` - admin endpoints
- `src/main/java/com/nhhoang/synexbackend/controller/client` - authenticated user endpoints
- `src/main/java/com/nhhoang/synexbackend/dto` - request and response DTOs
- `src/main/java/com/nhhoang/synexbackend/entity` - JPA entities
- `src/main/java/com/nhhoang/synexbackend/repository` - Spring Data repositories
- `src/main/java/com/nhhoang/synexbackend/service` - business logic

## Configuration

The application reads its environment values from `application.properties`.

Required values:

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
2. Set the required environment variables.
3. Start the application:

```bash
./mvnw spring-boot:run
```

To compile only:

```bash
./mvnw -DskipTests compile
```

## API Overview

All endpoints return JSON. Endpoints marked with 🔐 require authentication.

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout
- `POST /api/auth/refresh` - Refresh access token

### Public Catalog

- `GET /api/products` - List products
- `GET /api/products/{id}` - Get product detail
- `GET /api/categories` - List categories
- `GET /api/brands` - List brands
- `POST /api/contact-messages` - Send a contact message

### User Profile 🔐

- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update current user profile
- `POST /api/users/me/password` - Change password

### Shipping Addresses 🔐

- `POST /api/users/me/addresses` - Add a new shipping address
- `GET /api/users/me/addresses` - List shipping addresses
- `GET /api/users/me/addresses/{addressId}` - Get shipping address detail
- `PUT /api/users/me/addresses/{addressId}` - Update shipping address
- `DELETE /api/users/me/addresses/{addressId}` - Delete shipping address
- `PATCH /api/users/me/addresses/{addressId}/default` - Set default address

Rules:

- One account can have up to 3 shipping addresses.
- The first address is automatically set as default.
- If a default address is deleted, another address is promoted to default.

### Cart 🔐

- `POST /api/cart` - Create or get the current user's cart
- `POST /api/cart/add?productId=X&quantity=Y` - Add item to cart
- `GET /api/cart/me` - Get the current user's cart
- `GET /api/cart/{cartId}` - Get cart by ID
- `PATCH /api/cart/items/{productId}/increase?amount=1` - Increase quantity
- `PATCH /api/cart/items/{productId}/decrease?amount=1` - Decrease quantity
- `DELETE /api/cart/items/{productId}` - Remove item from cart

### Orders 🔐

- `POST /api/orders/validate-stock` - Validate selected cart items before entering the address step
- `POST /api/orders` - Create order from selected cart items
- `GET /api/orders` - Get current user's orders
- `GET /api/orders/{orderId}` - Get current user's order detail

#### Checkout Flow

The frontend should send the selected cart item IDs when the user checks out.

First step, before moving from cart to address form:

```json
{
  "selectedCartItemIds": [12, 15, 18]
}
```

Call `POST /api/orders/validate-stock` to verify the selected items are still available.

Final step, when placing the order:

```json
{
  "shippingAddressId": 12,
  "paymentMethod": "COD",
  "selectedCartItemIds": [12, 15, 18]
}
```

Behavior:

- If `shippingAddressId` is provided, the address must belong to the current user.
- If `shippingAddressId` is omitted, the default address is used.
- Stock is checked twice: once in `POST /api/orders/validate-stock` and once again in `POST /api/orders` before creating order items.
- If stock is insufficient, the API returns a `400 Bad Request` with an error message for the frontend.

### Reviews 🔐

- `GET /api/reviews/product/{productId}` - Get all reviews for a product
- `GET /api/reviews/me` - Get current user's reviews
- `POST /api/reviews` - Create a review
- `PUT /api/reviews/{reviewId}` - Update a review
- `DELETE /api/reviews/{reviewId}` - Delete a review

### Vouchers 🔐

- `GET /api/vouchers/validate?code=CODE&orderAmount=100000` - Validate a voucher and calculate discount

### Admin APIs 🔐

- `GET /api/admin/users` - List users
- `GET /api/admin/products` - Manage products
- `GET /api/admin/orders` - Manage orders
- `GET /api/admin/categories` - Manage categories
- `GET /api/admin/brands` - Manage brands
- `GET /api/admin/vouchers` - Manage vouchers
- `GET /api/admin/reviews` - Moderate reviews
- `GET /api/admin/contact-messages` - Manage contact messages

Product images are uploaded via multipart requests (send a `product` JSON part and optional image file parts).
Uploaded images are stored on the backend file system under `app.upload.dir` (default: `uploads`) and served through `/uploads/**`.

## Notes

- Admin endpoints require the `ADMIN` role.
- Lombok annotation processing is enabled in Maven.
- `spring.jpa.hibernate.ddl-auto=update` is enabled for development.

## License

No license has been defined yet.
