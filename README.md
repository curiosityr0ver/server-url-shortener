# URL Shortener Service

A Spring Boot-based URL shortener application with JWT authentication, PostgreSQL database, and containerized deployment using Docker.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Local Development (Docker)](#local-development-docker)
  - [Local Development (Without Docker)](#local-development-without-docker)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Database Migrations](#database-migrations)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## 🔧 Prerequisites

Before running this application, ensure you have the following installed:

- **Java 21** or higher ([Download](https://adoptium.net/))
- **Maven 3.9+** (or use the included Maven wrapper `mvnw`)
- **Docker** and **Docker Compose** ([Download](https://www.docker.com/products/docker-desktop))
- **PostgreSQL 16** (only if running without Docker)

## 🛠 Technology Stack

- **Spring Boot 3.5.7** - Application framework
- **Java 21** - Programming language
- **Spring Security** - Authentication and authorization
- **JWT (JSON Web Tokens)** - Stateless authentication
- **PostgreSQL 16** - Database
- **Flyway** - Database migration tool
- **Spring Data JPA** - Data persistence
- **Spring Boot Actuator** - Health checks and monitoring
- **Docker** - Containerization
- **Maven** - Build tool

## 📁 Project Structure

```
server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/_cortex/url_management/
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── service/         # Business logic
│   │   │       ├── repository/      # Data access
│   │   │       ├── model/           # Entity models
│   │   │       ├── dto/             # Data transfer objects
│   │   │       ├── security/        # JWT & Security config
│   │   │       └── util/            # Utilities
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Flyway migration scripts
│   └── test/                        # Test files
├── Dockerfile                       # Application container definition
├── docker-compose.yml               # Multi-container orchestration
├── pom.xml                          # Maven dependencies
├── AUTHENTICATION.md                # Detailed auth guide
├── JWT_QUICK_REFERENCE.md          # JWT quick reference
└── README.md                        # This file
```

## 🚀 Getting Started

### Local Development (Docker)

This is the **recommended** approach for local development as it ensures consistency across environments.

#### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd server
```

#### Step 2: Build and Run with Docker Compose

```bash
docker-compose up --build
```

This command will:
- Build the Spring Boot application Docker image
- Pull the PostgreSQL 16 Alpine image
- Start both containers with proper networking
- Run Flyway migrations automatically
- Expose the application on port **8081**
- Expose PostgreSQL on port **5434**

#### Step 3: Verify the Application

Once the containers are running, verify the application is healthy:

```bash
# Check application health
curl http://localhost:8081/actuator/health

# Expected response:
# {"status":"UP"}
```

#### Step 4: Stop the Application

```bash
# Stop and remove containers
docker-compose down

# Stop, remove containers, and clean up images
docker-compose down --rmi all
```

### Local Development (Without Docker)

If you prefer to run the application locally without Docker:

#### Step 1: Start PostgreSQL

Ensure PostgreSQL is running locally on port **5432** with the following configuration:

- **Database**: `urlshortener`
- **Username**: `admin`
- **Password**: `admin`

Create the database:

```sql
CREATE DATABASE urlshortener;
```

#### Step 2: Update Application Configuration

Edit `src/main/resources/application.properties` and update the database URL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortener
```

#### Step 3: Build and Run

Using Maven wrapper (recommended):

```bash
# Windows
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw clean package
./mvnw spring-boot:run
```

Or using installed Maven:

```bash
mvn clean package
mvn spring-boot:run
```

The application will start on port **8080**.

## 🔐 Authentication

This application uses **JWT (JSON Web Token)** based authentication for securing API endpoints.

### Quick Start with Authentication

**Step 1: Register a User**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Step 2: Login and Get Token**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIiwiaWF0IjoxNzM..."
}
```

**Step 3: Use Token for Protected Endpoints**
```bash
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "originalUrl": "https://www.google.com",
    "userId": 1
  }'
```

### Token Details
- **Expiration**: 10 hours
- **Algorithm**: HS256 (HMAC-SHA256)
- **Header Format**: `Authorization: Bearer <token>`

For complete authentication documentation, see [AUTHENTICATION.md](AUTHENTICATION.md).

## 🔌 API Endpoints

> **Base URL:** `http://localhost:8081` (Docker) or `http://localhost:8080` (Local)

### 🔓 Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Application health check |
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and receive JWT token |
| GET | `/{shortCode}` | Redirect to original URL (tracks hits) |

### 🔒 Protected Endpoints (Requires JWT Token)

All protected endpoints require the `Authorization` header:
```
Authorization: Bearer <your-jwt-token>
```

#### URL Management

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/urls` | Create auto-generated short URL | `{"originalUrl": "https://...", "userId": 1, "expireAt": "2024-12-31T23:59:59Z"}` |
| POST | `/api/urls/custom` | Create custom short URL | `{"originalUrl": "https://...", "customShortCode": "mylink", "userId": 1}` |
| GET | `/api/urls/{shortCode}` | Get URL details (without redirect) | - |
| DELETE | `/api/urls/{id}` | Delete URL by ID | - |
| GET | `/api/users/{userId}/urls` | Get all URLs created by a user | - |
| GET | `/api/urls/stats/popular` | Get most popular URLs (top 10 by hits) | - |
| DELETE | `/api/urls/expired` | Delete all expired URLs | - |

#### User Management

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/users` | Create new user | `{"username": "...", "email": "...", "password": "..."}` |
| GET | `/api/users/{id}` | Get user by ID | - |
| GET | `/api/users/username/{username}` | Get user by username | - |
| GET | `/api/users/email/{email}` | Get user by email | - |
| PUT | `/api/users/{id}` | Update user | `{"username": "...", "email": "...", "password": "..."}` |
| DELETE | `/api/users/{id}` | Delete user | - |

## 📝 Complete API Examples

### Authentication Flow

**1. Register a New User**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "securepass123"
  }'
```

**Response:** `200 OK`
```json
{
  "message": "User registered successfully"
}
```

**2. Login to Get JWT Token**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "securepass123"
  }'
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTczNzQ3MDQwMCwiZXhwIjoxNzM3NTA2NDAwfQ.xxx"
}
```

### URL Shortening (with Authentication)

**1. Create Auto-Generated Short URL**
```bash
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "originalUrl": "https://www.google.com",
    "userId": 1
  }'
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "shortCode": "aB3xY7K",
  "originalUrl": "https://www.google.com",
  "createdByUserId": 1,
  "createdByUsername": "testuser",
  "createdAt": "2025-01-21T10:30:00Z",
  "lastAccessedAt": null,
  "expireAt": null,
  "hits": 0
}
```

**2. Create Custom Short URL**
```bash
curl -X POST http://localhost:8081/api/urls/custom \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "originalUrl": "https://www.github.com",
    "customShortCode": "github",
    "userId": 1
  }'
```

**3. Create URL with Expiration**
```bash
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "originalUrl": "https://www.example.com",
    "userId": 1,
    "expireAt": "2024-12-31T23:59:59Z"
  }'
```

**4. Get URL Details (No Redirect, No Hit Count)**
```bash
curl http://localhost:8081/api/urls/aB3xY7K \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**5. Test Public Redirect (No Auth Required)**
```bash
# Follow redirects with -L flag
curl -L http://localhost:8081/aB3xY7K

# See redirect headers only
curl -I http://localhost:8081/aB3xY7K
```

**6. Get All URLs for a User**
```bash
curl http://localhost:8081/api/users/1/urls \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**7. Get Most Popular URLs**
```bash
curl http://localhost:8081/api/urls/stats/popular \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**Response:**
```json
[
  {
    "id": 1,
    "shortCode": "viral",
    "originalUrl": "https://popular.com",
    "hits": 1000,
    "createdByUsername": "testuser"
  },
  {
    "id": 2,
    "shortCode": "trend",
    "originalUrl": "https://trending.com",
    "hits": 500,
    "createdByUsername": "anotheruser"
  }
]
```

**8. Delete a URL**
```bash
curl -X DELETE http://localhost:8081/api/urls/1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**Response:** `204 No Content`

**9. Delete All Expired URLs**
```bash
curl -X DELETE http://localhost:8081/api/urls/expired \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### User Management (with Authentication)

**1. Create a User (Alternative to Registration)**
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securepass123"
  }'
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com"
}
```

**2. Get User by ID**
```bash
curl http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**3. Get User by Username**
```bash
curl http://localhost:8081/api/users/username/john_doe \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**4. Get User by Email**
```bash
curl http://localhost:8081/api/users/email/john@example.com \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**5. Update User**
```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "username": "john_doe",
    "email": "newemail@example.com",
    "password": "newpassword123"
  }'
```

**6. Delete User**
```bash
curl -X DELETE http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

**Response:** `204 No Content`

### Complete Testing Workflow

Here's a complete workflow to test the application with authentication:

```bash
# 1. Check application health (public)
curl http://localhost:8081/actuator/health

# 2. Register a new user (public)
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# 3. Login and save the token (public)
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}' | jq -r '.token')

echo "Token: $TOKEN"

# 4. Create a short URL (protected - needs token)
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"originalUrl":"https://www.google.com","userId":1}'

# 5. Test the redirect (public - no token needed)
curl -L http://localhost:8081/{shortCode}

# 6. Check URL statistics (protected)
curl http://localhost:8081/api/urls/{shortCode} \
  -H "Authorization: Bearer $TOKEN"

# 7. View all URLs created by the user (protected)
curl http://localhost:8081/api/users/1/urls \
  -H "Authorization: Bearer $TOKEN"

# 8. Check popular URLs (protected)
curl http://localhost:8081/api/urls/stats/popular \
  -H "Authorization: Bearer $TOKEN"
```

### Validation Rules

**URLs:**
- `originalUrl`: Required, must start with `http://` or `https://`, max 2048 characters
- `customShortCode`: 3-20 alphanumeric characters `[0-9A-Za-z]`
- `userId`: Optional, must be a valid user ID
- `expireAt`: Optional, ISO 8601 timestamp format

**Users:**
- `username`: Required, 3-150 characters, unique
- `email`: Required, valid email format, max 255 characters, unique
- `password`: Required, minimum 8 characters (stored as BCrypt hash)

### Error Responses

**Unauthorized (401 Unauthorized)** - Missing or invalid JWT token:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/urls"
}
```

**Forbidden (403 Forbidden)** - No token provided:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/urls"
}
```

**Validation Error (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "originalUrl": "Original URL is required",
    "customShortCode": "Short code must contain only alphanumeric characters"
  },
  "timestamp": "2025-01-21T10:30:00Z"
}
```

**Resource Not Found (400 Bad Request):**
```json
{
  "status": 400,
  "message": "URL not found with short code: xyz123",
  "errors": null,
  "timestamp": "2025-01-21T10:30:00Z"
}
```

**Bad Credentials (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Bad credentials",
  "timestamp": "2025-01-21T10:30:00Z"
}
```

## ⚙️ Configuration

### Environment Variables

The application supports the following environment variables (useful for deployment):

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://postgres:5432/urlshortener` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `admin` |
| `SERVER_PORT` | Application port | `8080` |
| `JWT_SECRET` | JWT signing secret (recommended for production) | Auto-generated |

### Docker Compose Configuration

The `docker-compose.yml` file defines two services:

#### PostgreSQL Service
- **Image**: `postgres:16-alpine`
- **Container Name**: `urlshortener-postgres`
- **Port**: `5434:5432` (host:container)
- **Health Check**: Checks database readiness every 10 seconds

#### Application Service
- **Build**: Uses multi-stage Dockerfile
- **Container Name**: `urlshortener-app`
- **Port**: `8081:8080` (host:container)
- **Depends On**: PostgreSQL (waits for healthy status)
- **Health Check**: Checks `/actuator/health` endpoint every 30 seconds

### Application Properties

Key configurations in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://postgres:5432/urlshortener
spring.datasource.username=admin
spring.datasource.password=admin

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Server
server.port=8080
```

### Security Features

- **JWT Authentication**: Stateless token-based authentication
- **Password Hashing**: All passwords are securely hashed using BCrypt before storage
- **No Password Exposure**: User responses never include password hashes
- **Token Expiration**: JWT tokens expire after 10 hours
- **Public Endpoints**: Strategic endpoints (auth, redirect) remain public
- **Input Validation**: All inputs are validated against defined constraints
- **Error Handling**: Centralized exception handling prevents information leakage

## 🗄️ Database Migrations

This project uses **Flyway** for database version control. Migration scripts are located in:

```
src/main/resources/db/migration/
```

### Migration Naming Convention

Flyway migrations follow the pattern: `V{version}__{description}.sql`

Example: `V1__create_tables.sql`

### Running Migrations

Migrations run automatically when the application starts. To run migrations manually:

```bash
mvn flyway:migrate
```

### Creating New Migrations

1. Create a new SQL file in `src/main/resources/db/migration/`
2. Follow the naming convention: `V{next_version}__{description}.sql`
3. Write your DDL/DML statements
4. Restart the application (migrations will run automatically)

## 🚢 Deployment

### Building the Application

```bash
# Build the JAR file (tests are skipped by default)
./mvnw clean package

# The JAR will be in target/url-management-0.0.1-SNAPSHOT.jar
```

To run tests manually:
```bash
./mvnw test -DskipTests=false
```

### Deploying with External PostgreSQL

Since you're using an external PostgreSQL service for deployment, follow these steps:

#### Step 1: Update Environment Variables

Set the following environment variables in your deployment environment:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://<your-postgres-host>:<port>/<database-name>
SPRING_DATASOURCE_USERNAME=<your-username>
SPRING_DATASOURCE_PASSWORD=<your-password>
```

#### Step 2: Build Docker Image (Optional)

```bash
# Build the Docker image
docker build -t url-shortener:latest .

# Tag for your registry
docker tag url-shortener:latest <your-registry>/url-shortener:latest

# Push to registry
docker push <your-registry>/url-shortener:latest
```

#### Step 3: Run the Application

**Using JAR:**
```bash
java -jar target/url-management-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://<host>:<port>/<db> \
  --spring.datasource.username=<username> \
  --spring.datasource.password=<password>
```

**Using Docker:**
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<db> \
  -e SPRING_DATASOURCE_USERNAME=<username> \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  url-shortener:latest
```

### Deployment Platforms

This application can be deployed to:

- **AWS ECS/EKS** - Using Docker containers
- **Google Cloud Run** - Serverless container deployment
- **Azure Container Apps** - Container-based deployment
- **Heroku** - Using Heroku Postgres
- **Railway** - With Railway Postgres
- **Render** - With managed PostgreSQL
- **DigitalOcean App Platform** - With managed database

### Production Recommendations

For production deployments:

1. **Use HTTPS** - Always use HTTPS for secure token transmission
2. **Set JWT Secret** - Use environment variable `JWT_SECRET` for consistent signing
3. **Configure CORS** - Update CORS settings in `SecurityConfig.java` to specific domains
4. **Enable Monitoring** - Use Spring Boot Actuator endpoints for health checks
5. **Database Backups** - Implement regular database backup strategy
6. **Rate Limiting** - Add rate limiting to prevent abuse
7. **Logging** - Configure centralized logging (e.g., ELK stack)
8. **Refresh Tokens** - Consider implementing refresh tokens for better UX

## 🔍 Troubleshooting

### Common Issues

#### 1. Port Already in Use

**Error:** `Bind for 0.0.0.0:8081 failed: port is already allocated`

**Solution:**
```bash
# Find and kill the process using the port (Windows)
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Or change the port in docker-compose.yml
ports:
  - "8082:8080"  # Change 8081 to 8082
```

#### 2. Database Connection Failed

**Error:** `Connection refused` or `password authentication failed`

**Solution:**
- Verify PostgreSQL is running: `docker-compose ps`
- Check credentials in `docker-compose.yml` match `application.properties`
- Ensure the database exists: `urlshortener`

#### 3. JWT Token Expired

**Error:** `401 Unauthorized` after some time

**Solution:**
- Tokens expire after 10 hours
- Re-authenticate using `/api/auth/login` to get a new token

#### 4. Unauthorized Access

**Error:** `403 Forbidden` when accessing protected endpoints

**Solution:**
- Ensure you're including the `Authorization` header
- Format: `Authorization: Bearer <token>`
- Verify token is valid and not expired

#### 5. Flyway Migration Failed

**Error:** `FlywayException: Validate failed`

**Solution:**
```bash
# Clean the database and restart
docker-compose down -v
docker-compose up --build
```

#### 6. Application Won't Start

**Solution:**
```bash
# Check logs
docker-compose logs app

# Check PostgreSQL logs
docker-compose logs postgres

# Rebuild from scratch
docker-compose down --rmi all -v
docker-compose up --build
```

### Viewing Logs

```bash
# All services
docker-compose logs -f

# Application only
docker-compose logs -f app

# PostgreSQL only
docker-compose logs -f postgres

# Last 100 lines
docker-compose logs --tail=100
```

### Accessing PostgreSQL

You can connect to the PostgreSQL database using `psql`:

```bash
# Connect to PostgreSQL container
docker exec -it urlshortener-postgres psql -U admin -d urlshortener
```

Common `psql` commands:
```sql
-- List all tables
\dt

-- Describe table structure
\d users
\d urls

-- Run queries
SELECT * FROM users;
SELECT * FROM urls;

-- Exit psql
\q
```

## 📚 Additional Documentation

- **[AUTHENTICATION.md](AUTHENTICATION.md)** - Comprehensive JWT authentication guide
- **[JWT_QUICK_REFERENCE.md](JWT_QUICK_REFERENCE.md)** - Quick JWT reference
- **[JWT_EXAMPLES.md](JWT_EXAMPLES.md)** - Code examples in multiple languages
- **[JWT_FLOW_DIAGRAM.md](JWT_FLOW_DIAGRAM.md)** - Visual authentication flows
- **[JWT_STATUS.md](JWT_STATUS.md)** - Implementation status

## 📝 Notes

- The local Docker setup uses PostgreSQL with **no persistent volumes** since you'll be using an external database for deployment
- Data will be lost when containers are removed (`docker-compose down`)
- For production, always use an external managed PostgreSQL service
- The application uses health checks to ensure proper startup order
- Flyway migrations run automatically on application startup
- Tests are skipped by default in builds (configured in `pom.xml`)

## 📄 License

*(Add your license information here)*

## 👥 Contributors

*(Add contributor information here)*

---

**Need Help?** 
- Check the [AUTHENTICATION.md](AUTHENTICATION.md) for auth-related questions
- Open an issue or contact the development team
- Review the comprehensive documentation in the `docs/` directory
