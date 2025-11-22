# URL Shortener Service

A Spring Boot-based URL shortener application with PostgreSQL database and containerized deployment using Docker.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Local Development (Docker)](#local-development-docker)
  - [Local Development (Without Docker)](#local-development-without-docker)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Complete API Examples](#complete-api-examples)
- [Frontend Integration](#frontend-integration)
- [Configuration](#configuration)
- [Deployment](#deployment)
  - [Render Deployment](#render-deployment)
- [Production Recommendations](#production-recommendations)
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
- **PostgreSQL 16** - Database
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
│   │   │       ├── security/        # Security config
│   │   │       └── util/            # Utilities
│   │   └── resources/
│   │       └── application.properties
│   └── test/                        # Test files
├── Dockerfile                       # Application container definition
├── docker-compose.yml               # Multi-container orchestration
├── pom.xml                          # Maven dependencies
└── README.md                        # This file
```

## 🚀 Getting Started

### Local Development (Docker)

This is the **recommended** approach for local development as it ensures consistency across environments.

#### Step 1: Clone the Repository

```bash
git clone https://github.com/curiosityr0ver/server-url-shortener
cd server-url-shortener/server
```

#### Step 2: Build and Run with Docker Compose

```bash
docker-compose up --build
```

This command will:
- Build the Spring Boot application Docker image
- Pull the PostgreSQL 16 Alpine image
- Start both containers with proper networking
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

This application uses **Spring Security** for authentication. Currently, all endpoints are publicly accessible, but authentication endpoints are available for user registration and login.

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

**Response:** `200 OK`
```
User registered successfully
```

**Step 2: Login**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

**Response:** `200 OK`
```json
{
  "message": "Login successful",
  "username": "john"
}
```

**Note:** 
- Currently, authentication is **not enforced** on API endpoints. All endpoints are publicly accessible.
- **Guest users can create URLs** without logging in by omitting the `userId` field.
- **Authenticated users** can optionally provide `userId` to associate URLs with their account.
- The authentication system is in place for future use (e.g., user-specific URL management, analytics).

## 🔌 API Endpoints

> **Base URL:** 
> - Local (Docker): `http://localhost:8081`
> - Local (Without Docker): `http://localhost:8080`
> - Production (Render): `https://your-app.onrender.com`

> **Note:** All endpoints support CORS. By default, CORS is configured to allow requests from `http://localhost:5173` (for local frontend development). This can be configured via the `CORS_ALLOWED_ORIGINS` environment variable.

### 🔓 Public Endpoints (No Authentication Required)

| Method | Endpoint | Description | Response Code |
|--------|----------|-------------|---------------|
| GET | `/actuator/health` | Application health check | 200 |
| POST | `/api/auth/register` | Register new user | 200 |
| POST | `/api/auth/login` | Login (returns success message) | 200 |
| GET | `/{shortCode}` | Redirect to original URL (tracks hits) | 302 |

### 📋 URL Management Endpoints

> **Note:** URL creation is available to **both guest users and authenticated users**. The `userId` field is **optional**. If not provided, the URL will be created as a guest URL (not associated with any user).

| Method | Endpoint | Description | Request Body | Response Code |
|--------|----------|-------------|--------------|----------------|
| POST | `/api/urls` | Create auto-generated short URL (guest or authenticated) | `{"originalUrl": "https://..."}` or `{"originalUrl": "https://...", "userId": 1, "expireAt": "2024-12-31T23:59:59Z"}` | 201 |
| POST | `/api/urls/custom` | Create custom short URL (guest or authenticated) | `{"originalUrl": "https://...", "customShortCode": "mylink"}` or `{"originalUrl": "https://...", "customShortCode": "mylink", "userId": 1, "expireAt": "2024-12-31T23:59:59Z"}` | 201 |
| GET | `/api/urls/{shortCode}` | Get URL details (without redirect, no hit tracking) | - | 200 |
| DELETE | `/api/urls/{id}` | Delete URL by ID | - | 204 |
| GET | `/api/users/{userId}/urls` | Get all URLs created by a user | - | 200 |
| GET | `/api/urls/stats/popular` | Get most popular URLs (top 10 by hits) | - | 200 |
| DELETE | `/api/urls/expired` | Delete all expired URLs | - | 200 |

### 👤 User Management Endpoints

| Method | Endpoint | Description | Request Body | Response Code |
|--------|----------|-------------|--------------|----------------|
| POST | `/api/users` | Create new user | `{"username": "...", "email": "...", "password": "..."}` | 201 |
| GET | `/api/users/{id}` | Get user by ID | - | 200 |
| GET | `/api/users/username/{username}` | Get user by username | - | 200 |
| GET | `/api/users/email/{email}` | Get user by email | - | 200 |
| PUT | `/api/users/{id}` | Update user | `{"username": "...", "email": "...", "password": "..."}` | 200 |
| DELETE | `/api/users/{id}` | Delete user | - | 204 |

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
```
User registered successfully
```

**2. Login**
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
  "message": "Login successful",
  "username": "testuser"
}
```

### URL Shortening

**1. Create Auto-Generated Short URL (Guest User - No Login Required)**
```bash
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.google.com"
  }'
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "shortCode": "aB3xY7K",
  "shortUrl": "http://localhost:8081/aB3xY7K",
  "originalUrl": "https://www.google.com",
  "createdByUserId": null,
  "createdByUsername": null,
  "createdAt": "2025-01-21T10:30:00Z",
  "lastAccessedAt": null,
  "expireAt": null,
  "hits": 0
}
```

**1b. Create Auto-Generated Short URL (Authenticated User)**
```bash
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.google.com",
    "userId": 1
  }'
```

**Response:** `201 Created`
```json
{
  "id": 2,
  "shortCode": "xY9zK2m",
  "shortUrl": "http://localhost:8081/xY9zK2m",
  "originalUrl": "https://www.google.com",
  "createdByUserId": 1,
  "createdByUsername": "testuser",
  "createdAt": "2025-01-21T10:35:00Z",
  "lastAccessedAt": null,
  "expireAt": null,
  "hits": 0
}
```

**2. Create Custom Short URL (Guest User - No Login Required)**
```bash
curl -X POST http://localhost:8081/api/urls/custom \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.github.com",
    "customShortCode": "github"
  }'
```

**Response:** `201 Created`
```json
{
  "id": 2,
  "shortCode": "github",
  "shortUrl": "http://localhost:8081/github",
  "originalUrl": "https://www.github.com",
  "createdByUserId": null,
  "createdByUsername": null,
  "createdAt": "2025-01-21T10:35:00Z",
  "lastAccessedAt": null,
  "expireAt": null,
  "hits": 0
}
```

**2b. Create Custom Short URL (Authenticated User)**
```bash
curl -X POST http://localhost:8081/api/urls/custom \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.github.com",
    "customShortCode": "github",
    "userId": 1
  }'
```

**Response:** `201 Created`
```json
{
  "id": 3,
  "shortCode": "github",
  "shortUrl": "http://localhost:8081/github",
  "originalUrl": "https://www.github.com",
  "createdByUserId": 1,
  "createdByUsername": "testuser",
  "createdAt": "2025-01-21T10:40:00Z",
  "lastAccessedAt": null,
  "expireAt": null,
  "hits": 0
}
```

**3. Create URL with Expiration (Guest or Authenticated)**
```bash
# Guest user
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.example.com",
    "expireAt": "2024-12-31T23:59:59Z"
  }'

# Authenticated user
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.example.com",
    "userId": 1,
    "expireAt": "2024-12-31T23:59:59Z"
  }'
```

**4. Get URL Details (No Redirect, No Hit Count)**
```bash
curl http://localhost:8081/api/urls/aB3xY7K
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "shortCode": "aB3xY7K",
  "shortUrl": "http://localhost:8081/aB3xY7K",
  "originalUrl": "https://www.google.com",
  "createdByUserId": null,
  "createdByUsername": null,
  "createdAt": "2025-01-21T10:30:00Z",
  "lastAccessedAt": null,
  "expireAt": null,
  "hits": 0
}
```

**5. Test Public Redirect (No Auth Required)**
```bash
# Follow redirects with -L flag
curl -L http://localhost:8081/aB3xY7K

# See redirect headers only
curl -I http://localhost:8081/aB3xY7K
```

**Response:** `302 Found` (redirects to original URL)
```
HTTP/1.1 302 Found
Location: https://www.google.com
```

**Note:** This endpoint tracks hits. Each access increments the `hits` counter and updates `lastAccessedAt`.

**6. Get All URLs for a User** (Requires userId)
```bash
curl http://localhost:8081/api/users/1/urls
```

**7. Get Most Popular URLs**
```bash
curl http://localhost:8081/api/urls/stats/popular
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "shortCode": "viral",
    "shortUrl": "http://localhost:8081/viral",
    "originalUrl": "https://popular.com",
    "createdByUserId": 1,
    "createdByUsername": "testuser",
    "createdAt": "2025-01-20T10:30:00Z",
    "lastAccessedAt": "2025-01-21T09:15:00Z",
    "expireAt": null,
    "hits": 1000
  },
  {
    "id": 2,
    "shortCode": "trend",
    "shortUrl": "http://localhost:8081/trend",
    "originalUrl": "https://trending.com",
    "createdByUserId": 2,
    "createdByUsername": "anotheruser",
    "createdAt": "2025-01-19T14:20:00Z",
    "lastAccessedAt": "2025-01-21T08:45:00Z",
    "expireAt": null,
    "hits": 500
  }
]
```

**Note:** Returns top 10 URLs ordered by hit count (descending).

**8. Delete a URL**
```bash
curl -X DELETE http://localhost:8081/api/urls/1
```

**Response:** `204 No Content`

**9. Delete All Expired URLs**
```bash
curl -X DELETE http://localhost:8081/api/urls/expired
```

**Response:** `200 OK`
```json
5
```
(The number represents the count of deleted URLs)

### User Management

**1. Create a User (Alternative to Registration)**
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
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
curl http://localhost:8081/api/users/1
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com"
}
```

**3. Get User by Username**
```bash
curl http://localhost:8081/api/users/username/john_doe
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com"
}
```

**4. Get User by Email**
```bash
curl http://localhost:8081/api/users/email/john@example.com
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com"
}
```

**5. Update User**
```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "newemail@example.com",
    "password": "newpassword123"
  }'
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "newemail@example.com"
}
```

**Note:** Password is optional in the update request. If provided, it will be hashed and updated.

**6. Delete User**
```bash
curl -X DELETE http://localhost:8081/api/users/1
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

# 3. Login (public)
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# 4. Create a short URL (as guest - no userId required)
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://www.google.com"}'

# Or create as authenticated user (with userId)
curl -X POST http://localhost:8081/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://www.google.com","userId":1}'

# 5. Test the redirect (public)
curl -L http://localhost:8081/{shortCode}

# 6. Check URL statistics
curl http://localhost:8081/api/urls/{shortCode}

# 7. View all URLs created by the user
curl http://localhost:8081/api/users/1/urls

# 8. Check popular URLs
curl http://localhost:8081/api/urls/stats/popular
```

### Testing on Windows (PowerShell)

If you are using PowerShell on Windows, use these commands:

```powershell
# 1. Login
$body = @{ username = "testuser"; password = "password123" } | ConvertTo-Json
$response = Invoke-WebRequest -Uri http://localhost:8081/api/auth/login -Method POST -Body $body -ContentType "application/json"
$response.Content

# 2. Create a short URL
$body = @{ originalUrl = "https://www.google.com" } | ConvertTo-Json
$urlResponse = Invoke-WebRequest -Uri http://localhost:8081/api/urls -Method POST -Body $body -ContentType "application/json"
$urlResponse.Content

# 3. Get URL Details
Invoke-WebRequest -Uri http://localhost:8081/api/urls/stats/popular
```

### Validation Rules

**URLs:**
- `originalUrl`: **Required**, must start with `http://` or `https://`, max 2048 characters
- `customShortCode`: **Required** for `/api/urls/custom`, 3-20 alphanumeric characters `[0-9A-Za-z]`, must be unique
- `userId`: **Optional** - If not provided, URL is created as a guest URL (not associated with any user). If provided, must be a valid user ID.
- `expireAt`: **Optional**, ISO 8601 timestamp format (e.g., `"2024-12-31T23:59:59Z"`)

> **Important:** 
> - **Guest users can create URLs** without providing `userId`
> - **Authenticated users** can optionally provide `userId` to associate URLs with their account
> - Guest URLs will have `createdByUserId` and `createdByUsername` set to `null` in the response

**Users:**
- `username`: Required, 3-150 characters, unique
- `email`: Required, valid email format, max 255 characters, unique
- `password`: Required for creation, minimum 8 characters (stored as BCrypt hash). Optional for updates.

### Error Responses

**Unauthorized (401 Unauthorized)** - Invalid credentials:
```json
{
  "error": "Incorrect username or password"
}
```

**Forbidden (403 Forbidden)** - Access denied:
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

**Bad Credentials (401 Unauthorized):**
```json
{
  "error": "Incorrect username or password"
}
```

**Registration Error (400 Bad Request):**
```json
{
  "error": "Failed to register user: Username already exists"
}
```

**Custom Short Code Conflict (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Short code already exists: github",
  "errors": null,
  "timestamp": "2025-01-21T10:30:00Z"
}
```

## ⚙️ Configuration

### Environment Variables

The application supports the following environment variables (useful for deployment):

| Variable | Description | Default | Notes |
|----------|-------------|---------|-------|
| `DATABASE_URL` | PostgreSQL connection string (Render format) | - | Format: `postgresql://user:password@host:port/database` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:h2:mem:testdb` | Used if DATABASE_URL not set |
| `SPRING_DATASOURCE_USERNAME` | Database username | `sa` | Used if DATABASE_URL not set |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` | Used if DATABASE_URL not set |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | Database driver class | `org.h2.Driver` | Auto-detected from URL |
| `PORT` | Application port (set by Render) | `8080` | Spring Boot reads PORT automatically |
| `SPRING_H2_CONSOLE_ENABLED` | Enable H2 console | `true` | Set to `false` in production |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins (comma-separated) | `http://localhost:5173` | For production, set to your frontend domain(s) |

> **Note:** 
> - The `shortUrl` field in API responses is automatically extracted from the incoming HTTP request (scheme, host, and port), so it works correctly in any environment without manual configuration.
> - When `DATABASE_URL` is provided (e.g., by Render), the `docker-entrypoint.sh` script automatically parses it and sets the individual Spring Boot datasource variables.
> - The application falls back to H2 in-memory database if no PostgreSQL connection is configured.

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
# Database - Supports multiple formats via environment variables
spring.datasource.url=${SPRING_DATASOURCE_URL:${DATABASE_URL:jdbc:h2:mem:testdb}}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:${DB_USER:sa}}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:${DB_PASSWORD:password}}
spring.datasource.driver-class-name=${SPRING_DATASOURCE_DRIVER_CLASS_NAME:org.h2.Driver}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Server - Uses PORT environment variable (set by Render) or defaults to 8080
server.port=${PORT:8080}

# H2 Console (disabled in production)
spring.h2.console.enabled=${SPRING_H2_CONSOLE_ENABLED:true}
```

### Security Features

- **Password Hashing**: All passwords are securely hashed using BCrypt before storage
- **No Password Exposure**: User responses never include password hashes
- **CORS Support**: Configured to allow cross-origin requests from `http://localhost:5173` by default (configurable via `CORS_ALLOWED_ORIGINS` environment variable)
- **CSRF Disabled**: CSRF protection is disabled for API access
- **Public Endpoints**: All endpoints are currently publicly accessible
- **Input Validation**: All inputs are validated against defined constraints
- **Error Handling**: Centralized exception handling prevents information leakage

### CORS Configuration

The application is configured to accept requests from specific origins. CORS is configured in `SecurityConfig.java`:

- **Allowed Origins**: `http://localhost:5173` (default, for local frontend development)
  - Can be configured via `CORS_ALLOWED_ORIGINS` environment variable
  - Supports multiple origins (comma-separated): `http://localhost:5173,https://yourdomain.com`
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS, PATCH
- **Allowed Headers**: All headers
- **Credentials**: Enabled (allows cookies for session-based authentication)

#### Configuring CORS

**For Local Development:**
- Default configuration allows `http://localhost:5173`
- No configuration needed if your frontend runs on port 5173

**For Production:**
Set the `CORS_ALLOWED_ORIGINS` environment variable to your frontend domain(s):

```bash
# Single origin
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# Multiple origins (comma-separated)
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

**In Render:**
Add to your `render.yaml` or set in the Render dashboard:
```yaml
envVars:
  - key: CORS_ALLOWED_ORIGINS
    value: https://your-frontend-domain.com
```

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

- **Render** - ✅ Fully configured (see [Render Deployment](#render-deployment) below)
- **AWS ECS/EKS** - Using Docker containers
- **Google Cloud Run** - Serverless container deployment
- **Azure Container Apps** - Container-based deployment
- **Heroku** - Using Heroku Postgres
- **Railway** - With Railway Postgres
- **DigitalOcean App Platform** - With managed database

### Render Deployment

This application is pre-configured for deployment to Render. The `render.yaml` file in the `server` directory defines the deployment configuration.

#### Quick Deploy to Render

1. **Push your code to Git** (GitHub, GitLab, or Bitbucket)

2. **Create a Render Blueprint**:
   - Go to [Render Dashboard](https://dashboard.render.com)
   - Click "New +" → "Blueprint"
   - Connect your Git repository
   - Render will automatically detect the `render.yaml` file in the `server` directory

3. **Review and Deploy**:
   - Render will show you the services defined in `render.yaml`:
     - PostgreSQL database (`urlshortener-db`)
     - Web service (`urlshortener-app`)
   - Click "Apply" to create the services
   - Render will automatically:
     - Create the PostgreSQL database
     - Build the Docker image from the Dockerfile
     - Deploy the application
     - Link the database to the web service

4. **Verify Deployment**:
   ```bash
   curl https://your-app.onrender.com/actuator/health
   ```

#### Render Configuration

The `render.yaml` file includes:
- PostgreSQL database service
- Web service with Docker runtime
- Environment variables configuration
- Health check endpoint configuration

The `docker-entrypoint.sh` script automatically parses Render's `DATABASE_URL` and configures Spring Boot datasource variables.

#### Render Environment Variables

Render automatically provides:
- `DATABASE_URL` - PostgreSQL connection string (parsed by `docker-entrypoint.sh`)
- `PORT` - Port the application should listen on (Spring Boot reads this automatically)

You can also set:
- `SPRING_H2_CONSOLE_ENABLED=false` - Disables H2 console in production (already set in `render.yaml`)

#### Render Free Tier Notes

- Services spin down after 15 minutes of inactivity
- First request after spin-down may take 30-60 seconds (cold start)
- Consider upgrading to paid plans for production use

### Frontend Integration

The API is fully configured for frontend integration:

1. **CORS Enabled**: All endpoints accept cross-origin requests from `http://localhost:5173` by default
2. **No Authentication Required**: Currently, all endpoints are publicly accessible
3. **Guest URL Creation**: Users can create shortened URLs without logging in (omit `userId` field)
4. **Optional User Association**: Authenticated users can optionally provide `userId` to associate URLs with their account
5. **RESTful API**: Standard REST endpoints with JSON request/response format
6. **Error Handling**: Consistent error response format
7. **Credentials Support**: CORS is configured to allow credentials (cookies) for future session-based authentication

#### Example Frontend Integration (JavaScript/TypeScript)

```javascript
// Base URL - update with your Render URL
const API_BASE_URL = 'https://your-app.onrender.com';

// Register a user
async function registerUser(username, email, password) {
  const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, email, password }),
  });
  return response.json();
}

// Create a short URL (guest user - no userId required)
async function createShortUrl(originalUrl, userId = null) {
  const body = userId ? { originalUrl, userId } : { originalUrl };
  const response = await fetch(`${API_BASE_URL}/api/urls`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });
  return response.json();
}

// Create a custom short URL (guest user)
async function createCustomShortUrl(originalUrl, customShortCode, userId = null) {
  const body = userId 
    ? { originalUrl, customShortCode, userId }
    : { originalUrl, customShortCode };
  const response = await fetch(`${API_BASE_URL}/api/urls/custom`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });
  return response.json();
}

// Get URL details
async function getUrlDetails(shortCode) {
  const response = await fetch(`${API_BASE_URL}/api/urls/${shortCode}`);
  return response.json();
}

// Get popular URLs
async function getPopularUrls() {
  const response = await fetch(`${API_BASE_URL}/api/urls/stats/popular`);
  return response.json();
}
```

### Production Recommendations

For production deployments:

1. **Use HTTPS** - Always use HTTPS for secure communication (Render provides this automatically)
2. **Configure CORS** - Update CORS settings in `SecurityConfig.java` to specific frontend domains
3. **Enable Authentication** - Consider implementing authentication for protected endpoints
4. **Enable Monitoring** - Use Spring Boot Actuator endpoints for health checks
5. **Database Backups** - Implement regular database backup strategy (Render provides automated backups on paid plans)
6. **Rate Limiting** - Add rate limiting to prevent abuse
7. **Logging** - Configure centralized logging (e.g., ELK stack)
8. **Environment-Specific Configuration** - Use environment variables for different deployment environments
9. **Upgrade Render Plan** - Consider upgrading from free tier for production workloads

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

#### 3. Unauthorized Access

**Error:** `401 Unauthorized` when logging in

**Solution:**
- Verify username and password are correct
- Ensure user exists in the database
- Check that password was hashed correctly during registration

#### 4. Application Won't Start

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


## 📝 Notes

- The local Docker setup uses PostgreSQL with **no persistent volumes** since you'll be using an external database for deployment
- Data will be lost when containers are removed (`docker-compose down`)
- For production, always use an external managed PostgreSQL service
- The application uses health checks to ensure proper startup order
- Tests are skipped by default in builds (configured in `pom.xml`)

---

**Repository:** [https://github.com/curiosityr0ver/server-url-shortener](https://github.com/curiosityr0ver/server-url-shortener)

**Need Help?** 
- Open an issue on [GitHub](https://github.com/curiosityr0ver/server-url-shortener/issues)
- Review the comprehensive documentation
