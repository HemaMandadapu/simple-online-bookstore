# Simple Online Bookstore

A Spring Boot REST API application that provides:

- User Registration
- User Login
- Token Based Authentication
- Book Catalog
- Shopping Cart
- Order Checkout
- Order History

---

## Technology Stack

- Java 17
- Spring Boot 3.3.x
- Spring Data JPA
- H2 Database
- Maven
- JUnit 5
- MockMvc

---

## Prerequisites

Install the following:

- Java 17+
- Maven 3.9+
- Eclipse / IntelliJ (Optional)

Verify installation:

```bash
java -version
mvn -version
```

---

## Clone Repository

```bash
git clone <repository-url>
cd backend
```

---

## Build Application

```bash
mvn clean install
```

Expected output:

```text
BUILD SUCCESS
```

---

## Run Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using Eclipse

1. Import Existing Maven Project
2. Open:

```text
BookstoreApplication.java
```

3. Right Click

```text
Run As
→ Spring Boot App
```

Application starts on:

```text
http://localhost:8080
```

---

## Access H2 Database

URL:

```text
http://localhost:8080/h2-console
```

Connection Details:

```text
JDBC URL:
jdbc:h2:mem:bookstore

Username:
sa

Password:
(blank)
```

---

## Available APIs

### Get Books

```http
GET /api/books
```

---

### Register User

```http
POST /api/auth/register
```

Request:

```json
{
  "username": "user",
  "password": "pass123"
}
```

---

### Login User

```http
POST /api/auth/login
```

Request:

```json
{
  "username": "user",
  "password": "pass123"
}
```

---

### Get Cart

```http
GET /api/cart
```

Header:

```text
Authorization: Bearer <token>
```

---

### Add To Cart

```http
POST /api/cart
```

Header:

```text
Authorization: Bearer <token>
```

Request:

```json
{
  "bookId": 1,
  "quantity": 2
}
```

---

### Update Cart Item

```http
PUT /api/cart/{cartItemId}
```

Header:

```text
Authorization: Bearer <token>
```

Request:

```json
{
  "bookId": 1,
  "quantity": 5
}
```

---

### Delete Cart Item

```http
DELETE /api/cart/{cartItemId}
```

Header:

```text
Authorization: Bearer <token>
```

---

### Checkout

```http
POST /api/orders
```

Header:

```text
Authorization: Bearer <token>
```

---

### Order History

```http
GET /api/orders
```

Header:

```text
Authorization: Bearer <token>
```

---

## Running Test Cases

Execute all tests:

```bash
mvn test
```

Or in Eclipse:

```text
Right Click BookstoreApiTest.java
→ Run As
→ JUnit Test
```

Expected:

```text
Tests run: XX
Failures: 0
Errors: 0
```

---

## Sample Flow

### 1 Register User

```http
POST /api/auth/register
```

Copy token from response.

### 2 Add Book To Cart

```http
POST /api/cart
```

```json
{
  "bookId": 1,
  "quantity": 2
}
```

### 3 View Cart

```http
GET /api/cart
```

### 4 Checkout

```http
POST /api/orders
```

### 5 View Order History

```http
GET /api/orders
```

---

## Notes

- H2 database runs in memory.
- Data is lost when the application is restarted.
- Sample books are automatically loaded during startup.
- Authentication is implemented using database-backed bearer tokens.