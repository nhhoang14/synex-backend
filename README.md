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
- Product variant and variant price management for admin
- Voucher management and validation APIs
- User review APIs (create/update/delete own reviews) and admin review moderation
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

All endpoints return JSON responses. Endpoints marked with 🔐 require authentication.

### Authentication

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/logout` - Logout user
- `POST /api/auth/refresh` - Refresh JWT token

### Products (Public)

- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID

### Categories (Public)

- `GET /api/categories` - Get all categories

### Brands (Public)

- `GET /api/brands` - Get all brands

### Contact Messages

- `POST /api/contact-messages` - Create a contact message (public)

### User Profile 🔐

- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update current user profile
- `POST /api/users/me/password` - Change password

### Shipping Addresses 🔐

- `POST /api/users/me/addresses` - Add new shipping address (max 3 per user)
- `GET /api/users/me/addresses` - Get all user's shipping addresses
- `GET /api/users/me/addresses/{addressId}` - Get specific shipping address
- `PUT /api/users/me/addresses/{addressId}` - Update shipping address
- `DELETE /api/users/me/addresses/{addressId}` - Delete shipping address
- `PATCH /api/users/me/addresses/{addressId}/default` - Set address as default

#### Shipping Address Rules

- One account can have at most 3 shipping addresses
- The first address is automatically set as default
- At any time, only one address can be default
- If a default address is deleted, the system automatically assigns another existing address as default
- You cannot unset the current default directly; set another address as default first

### Cart 🔐

- `POST /api/cart` - Create or get current user's cart
- `POST /api/cart/add?productId=X&quantity=Y` - Add product to cart (supports optional `cartId`, `variantId`)
- `GET /api/cart/me` - Get current user's cart
- `GET /api/cart/{cartId}` - Get cart by ID
- `PATCH /api/cart/items/{productId}/increase?amount=1` - Increase item quantity (supports optional `variantId`, `amount` defaults to 1)
- `PATCH /api/cart/items/{productId}/decrease?amount=1` - Decrease item quantity (min quantity is 1, supports optional `variantId`, `amount` defaults to 1)
- `DELETE /api/cart/items/{productId}` - Remove product from cart (supports optional `variantId`)

### Orders 🔐

- `POST /api/orders` - Create order
- `GET /api/orders` - Get current user's orders
- `GET /api/orders/{orderId}` - Get specific order

#### Order Checkout Behavior

For `POST /api/orders`:
- If `shippingAddressId` is provided, the order uses that address (must belong to current user)
- If `shippingAddressId` is omitted or null, the system uses the user's default address
- If no default address exists, the order request is rejected

Example with default address:
```json
{
  "paymentMethod": "COD"
}
```

Example with specific address:
```json
{
  "shippingAddressId": 12,
  "paymentMethod": "CARD"
}
```

### Reviews

- `GET /api/reviews/product/{productId}` - Get all reviews for a product (public)
- `GET /api/reviews/me` - Get current user's reviews 🔐
- `POST /api/reviews` - Create a new review for a product 🔐
- `PUT /api/reviews/{reviewId}` - Update current user's review 🔐
- `DELETE /api/reviews/{reviewId}` - Delete current user's review 🔐

### Vouchers 🔐

- `GET /api/vouchers/validate?code=CODE&orderAmount=100000` - Validate voucher and calculate discount

### Admin: Users (Admin only) 🔐

- `GET /api/admin/users` - Get all users
- `GET /api/admin/users/{id}` - Get user by ID
- `PUT /api/admin/users/{id}/role` - Update user role
- `DELETE /api/admin/users/{id}` - Delete user

### Admin: Products (Admin only) 🔐

- `POST /api/admin/products` - Create product
- `PUT /api/admin/products/{id}` - Update product
- `DELETE /api/admin/products/{id}` - Delete product

### Admin: Product Variants (Admin only) 🔐

- `GET /api/admin/products/{productId}/variants` - Get all variants of a product
- `POST /api/admin/products/{productId}/variants` - Create variant for product
- `PUT /api/admin/products/{productId}/variants/{variantId}` - Update variant info
- `PATCH /api/admin/products/{productId}/variants/{variantId}/price` - Update only variant price
- `DELETE /api/admin/products/{productId}/variants/{variantId}` - Delete variant

### Admin: Categories (Admin only) 🔐

- `GET /api/admin/categories` - Get all categories
- `POST /api/admin/categories` - Create category
- `PUT /api/admin/categories/{id}` - Update category
- `DELETE /api/admin/categories/{id}` - Delete category

### Admin: Brands (Admin only) 🔐

- `GET /api/admin/brands` - Get all brands
- `POST /api/admin/brands` - Create brand
- `PUT /api/admin/brands/{id}` - Update brand
- `DELETE /api/admin/brands/{id}` - Delete brand

### Admin: Orders (Admin only) 🔐

- `GET /api/admin/orders` - Get all orders
- `GET /api/admin/orders/{orderId}` - Get order by ID
- `PATCH /api/admin/orders/{orderId}/status` - Update order status

### Admin: Vouchers (Admin only) 🔐

- `GET /api/admin/vouchers` - Get all vouchers
- `GET /api/admin/vouchers/{id}` - Get voucher by ID
- `POST /api/admin/vouchers` - Create voucher
- `PUT /api/admin/vouchers/{id}` - Update voucher
- `PATCH /api/admin/vouchers/{id}/active?active=true|false` - Enable/disable voucher
- `DELETE /api/admin/vouchers/{id}` - Delete voucher

### Admin: Reviews (Admin only) 🔐

- `GET /api/admin/reviews` - Get all reviews
- `GET /api/admin/reviews/product/{productId}` - Get all reviews by product
- `DELETE /api/admin/reviews/{reviewId}` - Delete review

### Admin: Contact Messages (Admin only) 🔐

- `GET /api/admin/contact-messages` - Get all contact messages
- `PATCH /api/admin/contact-messages/{id}/status` - Update contact message status
- `DELETE /api/admin/contact-messages/{id}` - Delete contact message

## Notes

- Admin endpoints require `ADMIN` role.
- The project uses Lombok annotation processing in Maven, so `mvn compile` works with generated getters/setters.
- `spring.jpa.hibernate.ddl-auto=update` is enabled for development.

## License

No license has been defined yet.
