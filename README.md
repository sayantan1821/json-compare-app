# JSON Compare Application

Spring Boot backend application for JSON comparison tool.

## Prerequisites

- Java 17 or higher
- Gradle 7.6+ (or use included Gradle wrapper)
- PostgreSQL 14+ (installed and running)

## Setup

### 1. Database Setup

Create the database in PostgreSQL:
```sql
CREATE DATABASE json_compare_db;
```

### 2. Environment Variables (Optional)

The application uses default values, but you can override them with environment variables:

- `DATABASE_URL` - Default: `jdbc:postgresql://localhost:5432/json_compare_db`
- `DB_USERNAME` - Default: `postgres`
- `DB_PASSWORD` - Default: `postgres`
- `JWT_SECRET` - Default: `your-secret-key-change-this-in-production-minimum-256-bits`
- `JWT_EXPIRATION` - Default: `3600000` (1 hour in milliseconds)
- `JSON_MAX_SIZE` - Default: `10485760` (10MB in bytes)

**Note:** If environment variables are set in Windows, make sure to restart your IDE/terminal after setting them for Spring Boot to pick them up.

### 3. Run the Application

```bash
./gradlew bootRun
```

Or build and run:
```bash
./gradlew build
java -jar build/libs/json-compare-app-0.0.1-SNAPSHOT.jar
```

## API Documentation

Once running, access Swagger UI at:
- http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## Default Configuration

- Server Port: 8080
- Database: PostgreSQL on localhost:5432
- Database Name: json_compare_db
- JWT Expiration: 1 hour
- Max JSON Size: 10MB
