# Product Microservice

This service is a core component of the e-commerce platform, responsible for managing all product-related data. It provides a complete RESTful API for Create, Read, Update, and Delete (CRUD) operations on products and their categories.

## Technology Stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Web**: For building RESTful APIs.
- **Spring Data JPA**: For database persistence.
- **Spring Cloud**:
  - **Config Client**: To fetch configuration from a central Config Server.
  - **Eureka Discovery Client**: To register with the Eureka discovery server.
  - **Load Balancer**: For client-side load balancing.
- **Spring Boot Actuator**: For monitoring and management endpoints.
- **PostgreSQL**: As the primary database.
- **Lombok**: To reduce boilerplate code.
- **Maven**: For dependency management.

---

## Configuration

The service is configured to run in a microservices environment and relies on a central Config Server and Eureka for service discovery.

### 1. Local Configuration (`src/main/resources/application.yml`)

This file contains the essential bootstrap configuration to connect to the Config Server.

```yaml
server:
  port: 8081 

spring:
  config:
    import: "configserver:http://localhost:8888" 
  application:
    name: product-service
```

### 2. Central Configuration (`product-service.yml`)

This file must be located in your central **config-repo** directory. It holds the environment-specific configurations, such as database credentials and Eureka settings.

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/product_db
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
  # Expose all actuator endpoints for monitoring and refresh
  management:
    endpoints:
      web:
        exposure:
          include: "*"
```

---

## How to Run

1.  **Prerequisites**: Ensure your **Config Server** (port 8888) and **Eureka Discovery Server** (port 8761) are running.
2.  **Database**: Make sure you have a PostgreSQL database named `product_db` running and accessible with the credentials specified in the central configuration.
3.  **Run**: Start the application by running the `main` method in `ServerApplication.java` or by using the Maven command: `mvn spring-boot:run`.
4.  **Verification**: Check the Eureka dashboard at `http://localhost:8761` to confirm that `PRODUCT-SERVICE` is registered and `UP`.

---

## Initial Data

On startup, the application automatically populates the database with two categories ("Electronics", "Books") and three sample products for testing purposes.

---

## Data Models and DTOs

The API uses Data Transfer Objects (DTOs) to separate the internal data model from the external representation.

### 1. `Category` Model

Represents a product category.

| Field       | Type      | Description                               |
|-------------|-----------|-------------------------------------------|
| `id`        | Long      | The unique identifier for the category.   |
| `name`      | String    | The name of the category (e.g., "Electronics"). |
| `createdAt` | Instant   | Timestamp of creation.                    |
| `updatedAt` | Instant   | Timestamp of the last update.             |

### 2. `Product` Model

Represents a product, now linked to a `Category`.

| Field           | Type      | Description                               |
|-----------------|-----------|-------------------------------------------|
| `id`            | Long      | The unique identifier for the product.    |
| `name`          | String    | The name of the product.                  |
| `description`   | String    | A detailed description.                   |
| `price`         | double    | The price of the product.                 |
| `stockQuantity` | int       | The number of units in stock.             |
| `imageUrl`      | String    | URL for the product image.                |
| `manufacturer`  | String    | The manufacturer of the product.          |
| `createdAt`     | Instant   | Timestamp of creation.                    |
| `updatedAt`     | Instant   | Timestamp of the last update.             |
| `category`      | Category  | The associated category (Many-to-One).    |

### 3. DTOs (Data Transfer Objects)

- **`ProductRequestDTO`**: Used for creating and updating products. The client only needs to send the `categoryId`.
  ```json
  {
      "name": "New Product",
      "description": "A great new product",
      "price": 199.99,
      "stockQuantity": 50,
      "imageUrl": "http://example.com/image.jpg",
      "manufacturer": "ACME Corp",
      "categoryId": 1
  }
  ```

- **`ProductResponseDTO`**: The object returned by the API for GET requests. It includes the nested category details.
  ```json
  {
      "id": 1,
      "name": "iPhone 15",
      "price": 999.99,
      "stockQuantity": 100,
      "imageUrl": "http://example.com/iphone15.jpg",
      "manufacturer": "Apple",
      "createdAt": "2023-10-27T10:00:00Z",
      "updatedAt": "2023-10-27T10:00:00Z",
      "productCategory": {
          "id": 1,
          "name": "Electronics"
      }
  }
  ```

---

## API Endpoints

All endpoints are accessed through the API Gateway. Assuming the gateway is on `http://localhost:8080`, all URLs are prefixed with `/product-service`.

**Base URL**: `http://localhost:8080/product-service/products`

---

### 1. Get All Products

- **Description**: Retrieves a list of all available products.
- **HTTP Method**: `GET`
- **Endpoint**: `/`
- **Success Response**:
  - **Code**: `200 OK`
  - **Body**: A JSON array of `ProductResponseDTO` objects.

### 2. Get a Single Product by ID

- **Description**: Retrieves a specific product by its unique ID.
- **HTTP Method**: `GET`
- **Endpoint**: `/{id}`
- **Success Response**:
  - **Code**: `200 OK`
  - **Body**: A single `ProductResponseDTO` object.
- **Error Response**:
  - **Code**: `404 NOT FOUND` if the product ID does not exist.

### 3. Create a New Product

- **Description**: Adds a new product to the database.
- **HTTP Method**: `POST`
- **Endpoint**: `/`
- **Request Body**: A `ProductRequestDTO` object.
- **Success Response**:
  - **Code**: `201 CREATED`
  - **Body**: The created `ProductResponseDTO` object.
- **Error Response**:
  - **Code**: `404 NOT FOUND` if the `categoryId` in the request does not exist.

### 4. Update an Existing Product

- **Description**: Modifies the details of an existing product.
- **HTTP Method**: `PUT`
- **Endpoint**: `/{id}`
- **Request Body**: A `ProductRequestDTO` object with updated fields.
- **Success Response**:
  - **Code**: `200 OK`
  - **Body**: The updated `ProductResponseDTO` object.
- **Error Response**:
  - **Code**: `404 NOT FOUND` if the product ID or `categoryId` does not exist.

### 5. Delete a Product

- **Description**: Removes a product from the database by its ID.
- **HTTP Method**: `DELETE`
- **Endpoint**: `/{id}`
- **Success Response**:
  - **Code**: `204 NO CONTENT`
  - **Body**: None.

### 6. Health/Status Check

- **Description**: A simple test endpoint to verify the service is running.
- **HTTP Method**: `GET`
- **Endpoint**: `/status`
- **Success Response**:
  - **Code**: `200 OK`
  - **Body**: A plain text string: `Product Service is UP and running on port 8081!`
