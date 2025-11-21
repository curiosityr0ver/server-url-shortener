# URL Shortener Service

A Spring Boot-based URL shortener application with PostgreSQL database, containerized using Docker.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Local Development (Docker)](#local-development-docker)
  - [Local Development (Without Docker)](#local-development-without-docker)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
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
│   │   ├── java/              # Java source code
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/  # Flyway migration scripts
│   └── test/                  # Test files
├── Dockerfile                 # Application container definition
├── docker-compose.yml         # Multi-container orchestration
├── pom.xml                    # Maven dependencies
└── README.md                  # This file
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

## ⚙️ Configuration

### Environment Variables

The application supports the following environment variables (useful for deployment):

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://postgres:5432/urlshortener` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `admin` |
| `SERVER_PORT` | Application port | `8080` |

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

## 🔌 API Endpoints

### Health Check

```bash
GET /actuator/health
```

Returns the application health status.

**Response:**
```json
{
  "status": "UP"
}
```

### Additional Endpoints

*(Add your application-specific endpoints here)*

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

### Deploying with External PostgreSQL

Since you're using an external PostgreSQL service for deployment, follow these steps:

#### Step 1: Update Environment Variables

Set the following environment variables in your deployment environment:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://<your-postgres-host>:<port>/<database-name>
SPRING_DATASOURCE_USERNAME=<your-username>
SPRING_DATASOURCE_PASSWORD=<your-password>
```

#### Step 2: Build the Application

```bash
# Build the JAR file
mvn clean package -DskipTests

# The JAR will be in target/url-management-0.0.1-SNAPSHOT.jar
```

#### Step 3: Build Docker Image (Optional)

```bash
# Build the Docker image
docker build -t url-shortener:latest .

# Tag for your registry
docker tag url-shortener:latest <your-registry>/url-shortener:latest

# Push to registry
docker push <your-registry>/url-shortener:latest
```

#### Step 4: Run the Application

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

#### 3. Flyway Migration Failed

**Error:** `FlywayException: Validate failed`

**Solution:**
```bash
# Clean the database and restart
docker-compose down -v
docker-compose up --build
```

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

#### 5. Health Check Failing

**Solution:**
```bash
# Check if actuator is enabled
curl http://localhost:8081/actuator/health

# Verify application is running
docker-compose ps

# Check application logs
docker-compose logs -f app
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

```bash
# Connect to PostgreSQL container
docker exec -it urlshortener-postgres psql -U admin -d urlshortener

# Common commands:
# \dt          - List tables
# \d <table>   - Describe table
# \q           - Quit
```

## 📝 Notes

- The local Docker setup uses PostgreSQL with **no persistent volumes** since you'll be using an external database for deployment
- Data will be lost when containers are removed (`docker-compose down`)
- For production, always use an external managed PostgreSQL service
- The application uses health checks to ensure proper startup order
- Flyway migrations run automatically on application startup

## 📄 License

*(Add your license information here)*

## 👥 Contributors

*(Add contributor information here)*

---

**Need Help?** Open an issue or contact the development team.
