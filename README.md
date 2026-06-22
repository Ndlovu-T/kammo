# kammobackend

A Spring Boot REST API backend.

## Tech Stack

- Java 21
- Spring Boot 3.4.5
- Spring Web
- Spring Security (Stateless / JWT-ready)
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

2. **Update credentials** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=postgres
   spring.datasource.password=your_password_here
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

The server starts on [http://localhost:8080](http://localhost:8080).

## Project Structure

```
src/main/java/com/kammo/kammobackend/
├── KammobackendApplication.java   # Entry point
└── config/
    └── SecurityConfig.java        # Security configuration
```

## Running Tests

```bash
./mvnw test
```
