# kammo

Spring Boot REST API backend for the Kammo marketplace and escrow platform.

## Tech Stack

- Java 21
- Spring Boot 3.4.5
- Spring Web
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL

## Prerequisites

- JDK 21+
- Maven 3.8+
- PostgreSQL running locally

## Getting Started

1. **Create the database:**
   ```sql
   CREATE DATABASE kammo;
   ```

2. **Configure credentials** in a local `.env` file (not committed):
   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/kammo
   DB_USERNAME=postgres
   DB_PASSWORD=your_password_here
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

The server starts on [http://localhost:8080](http://localhost:8080).

## API Testing

Import `kammobackend.postman_collection.json` into Postman to test the available endpoints.

## Running Tests

```bash
./mvnw test
```
