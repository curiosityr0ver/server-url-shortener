# JWT Authentication Flow Diagram

## Registration Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/auth/register
       │ { username, email, password }
       │
       ▼
┌─────────────────────┐
│  AuthController     │
└──────┬──────────────┘
       │
       │ createUser()
       │
       ▼
┌─────────────────────┐
│   UserService       │
└──────┬──────────────┘
       │
       │ 1. Check if username exists
       │ 2. Check if email exists
       │ 3. Hash password (BCrypt)
       │ 4. Save to database
       │
       ▼
┌─────────────────────┐
│  UserRepository     │
│   (PostgreSQL)      │
└──────┬──────────────┘
       │
       │ User saved successfully
       │
       ▼
┌─────────────┐
│   Client    │
│  "Success"  │
└─────────────┘
```

---

## Login Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/auth/login
       │ { username, password }
       │
       ▼
┌─────────────────────────────┐
│      AuthController         │
└──────┬──────────────────────┘
       │
       │ 1. Authenticate credentials
       │
       ▼
┌─────────────────────────────┐
│  AuthenticationManager      │
└──────┬──────────────────────┘
       │
       │ loadUserByUsername()
       │
       ▼
┌─────────────────────────────┐
│ CustomUserDetailsService    │
└──────┬──────────────────────┘
       │
       │ findByUsername()
       │
       ▼
┌─────────────────────────────┐
│     UserRepository          │
│      (PostgreSQL)           │
└──────┬──────────────────────┘
       │
       │ User found
       │
       ▼
┌─────────────────────────────┐
│  PasswordEncoder (BCrypt)   │
│  Compare passwords          │
└──────┬──────────────────────┘
       │
       │ ✅ Password matches
       │
       ▼
┌─────────────────────────────┐
│         JwtUtil             │
│    generateToken()          │
└──────┬──────────────────────┘
       │
       │ Create JWT token:
       │ - Subject: username
       │ - IssuedAt: now
       │ - Expiration: now + 10 hours
       │ - Signature: HS256
       │
       ▼
┌─────────────┐
│   Client    │
│ { token }   │
└─────────────┘
```

---

## Authenticated Request Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/urls
       │ Authorization: Bearer eyJhbGc...
       │ { originalUrl, userId }
       │
       ▼
┌──────────────────────────────┐
│  JwtAuthenticationFilter     │
│  (OncePerRequestFilter)      │
└──────┬───────────────────────┘
       │
       │ 1. Extract "Bearer" token
       │ 2. Extract username from token
       │
       ▼
┌──────────────────────────────┐
│         JwtUtil              │
│    extractUsername()         │
└──────┬───────────────────────┘
       │
       │ username: "john_doe"
       │
       ▼
┌──────────────────────────────┐
│ CustomUserDetailsService     │
│  loadUserByUsername()        │
└──────┬───────────────────────┘
       │
       │ Load user details
       │
       ▼
┌──────────────────────────────┐
│         JwtUtil              │
│     validateToken()          │
└──────┬───────────────────────┘
       │
       │ ✅ Token valid:
       │ - Username matches
       │ - Not expired
       │ - Signature valid
       │
       ▼
┌──────────────────────────────┐
│  SecurityContextHolder       │
│  Set authentication          │
└──────┬───────────────────────┘
       │
       │ User authenticated
       │
       ▼
┌──────────────────────────────┐
│      UrlController           │
│    createShortUrl()          │
└──────┬───────────────────────┘
       │
       │ Business logic executes
       │
       ▼
┌──────────────────────────────┐
│       UrlService             │
└──────┬───────────────────────┘
       │
       │ Create and save URL
       │
       ▼
┌──────────────────────────────┐
│     UrlRepository            │
│      (PostgreSQL)            │
└──────┬───────────────────────┘
       │
       │ URL saved successfully
       │
       ▼
┌─────────────┐
│   Client    │
│ { shortUrl }│
└─────────────┘
```

---

## Unauthorized Request Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/urls
       │ NO Authorization header
       │ { originalUrl, userId }
       │
       ▼
┌──────────────────────────────┐
│  JwtAuthenticationFilter     │
└──────┬───────────────────────┘
       │
       │ No token found
       │ Skip authentication
       │
       ▼
┌──────────────────────────────┐
│     SecurityConfig           │
│   Check authorization        │
└──────┬───────────────────────┘
       │
       │ ❌ Endpoint requires auth
       │ ❌ No authentication in context
       │
       ▼
┌──────────────────────────────┐
│  Spring Security             │
│  Access Denied Handler       │
└──────┬───────────────────────┘
       │
       │ HTTP 403 Forbidden
       │
       ▼
┌─────────────┐
│   Client    │
│  403 Error  │
└─────────────┘
```

---

## Component Interaction Diagram

```
┌────────────────────────────────────────────────────────────────┐
│                        Spring Boot Application                  │
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    │
│  │    Client    │───▶│    Filter    │───▶│  Controller  │    │
│  │   Request    │    │    Chain     │    │              │    │
│  └──────────────┘    └──────┬───────┘    └──────┬───────┘    │
│                              │                    │             │
│                              │                    │             │
│  ┌──────────────────────────▼────────┐          │             │
│  │  JwtAuthenticationFilter          │          │             │
│  │  - Extract token                  │          │             │
│  │  - Validate token                 │          │             │
│  │  - Set authentication             │          │             │
│  └──────────┬────────────────────────┘          │             │
│             │                                    │             │
│             │                                    │             │
│  ┌──────────▼────────────┐          ┌───────────▼───────────┐ │
│  │      JwtUtil          │          │   AuthController      │ │
│  │  - generateToken()    │◀─────────│   - register()        │ │
│  │  - validateToken()    │          │   - login()           │ │
│  │  - extractUsername()  │          └───────────┬───────────┘ │
│  └───────────────────────┘                      │             │
│                                                  │             │
│  ┌──────────────────────────────────────────────▼───────────┐ │
│  │            CustomUserDetailsService                        │ │
│  │            - loadUserByUsername()                          │ │
│  └──────────┬─────────────────────────────────────────────────┘ │
│             │                                                  │
│             │                                                  │
│  ┌──────────▼────────────┐          ┌─────────────────────┐  │
│  │    UserService        │─────────▶│   PasswordEncoder   │  │
│  │  - createUser()       │          │   (BCrypt)          │  │
│  │  - findByUsername()   │          └─────────────────────┘  │
│  └──────────┬────────────┘                                   │
│             │                                                  │
│             │                                                  │
│  ┌──────────▼────────────┐          ┌─────────────────────┐  │
│  │   UserRepository      │          │   UrlRepository     │  │
│  │   (Spring Data JPA)   │          │  (Spring Data JPA)  │  │
│  └──────────┬────────────┘          └─────────────────────┘  │
│             │                                                  │
│             │                                                  │
│  ┌──────────▼───────────────────────────────────────────────┐ │
│  │                   PostgreSQL Database                     │ │
│  │                   - users table                           │ │
│  │                   - urls table                            │ │
│  └───────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────┘
```

---

## JWT Token Structure

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTczNzQ3ODcwNCwiZXhwIjoxNzM3NTE0NzA0fQ.Xy7vF8K9mN3pQ2rS5tU6vW7xY8zA

│                      │                                                                          │                                  │
│                      │                                                                          │                                  │
▼                      ▼                                                                          ▼                                  ▼

┌──────────────────┐   ┌────────────────────────────────────────────────────────────┐   ┌──────────────────────────────┐
│     HEADER       │ . │                       PAYLOAD                              │ . │         SIGNATURE            │
├──────────────────┤   ├────────────────────────────────────────────────────────────┤   ├──────────────────────────────┤
│ {                │   │ {                                                          │   │ HMACSHA256(                  │
│   "alg": "HS256",│   │   "sub": "john_doe",        // Username                   │   │   base64UrlEncode(header) +  │
│   "typ": "JWT"   │   │   "iat": 1737478704,        // Issued at (timestamp)      │   │   "." +                      │
│ }                │   │   "exp": 1737514704         // Expiration (timestamp)     │   │   base64UrlEncode(payload),  │
│                  │   │ }                                                          │   │   secret_key                 │
└──────────────────┘   └────────────────────────────────────────────────────────────┘   │ )                            │
                                                                                         └──────────────────────────────┘

Base64 Encoded         Base64 Encoded                                                    Cryptographic Signature
```

---

## Security Configuration Matrix

| Configuration | Value | Purpose |
|---------------|-------|---------|
| **Algorithm** | HS256 | HMAC with SHA-256 for token signing |
| **Secret Key** | Auto-generated | 256-bit key for signing/verifying tokens |
| **Token Expiration** | 10 hours | How long token remains valid |
| **Session Management** | Stateless | No server-side sessions |
| **Password Encoding** | BCrypt | Secure password hashing |
| **CSRF Protection** | Disabled | Not needed for stateless JWT |
| **CORS** | Enabled (`*`) | Allow cross-origin requests (update for production) |

---

## File Structure

```
src/main/java/com/_cortex/url_management/
│
├── security/
│   ├── JwtUtil.java                    # Token generation & validation
│   ├── JwtAuthenticationFilter.java    # Request filter for JWT
│   └── SecurityConfig.java             # Spring Security configuration
│
├── service/
│   ├── CustomUserDetailsService.java   # Load user for authentication
│   └── UserService.java                # User business logic
│
├── controller/
│   └── AuthController.java             # Login & registration endpoints
│
├── dto/
│   ├── LoginRequest.java               # Login request body
│   └── JwtResponse.java                # JWT token response
│
├── model/
│   └── User.java                       # User entity
│
└── repository/
    └── UserRepository.java             # User data access
```

---

## Environment Variables (Production)

```bash
# JWT Configuration
JWT_SECRET_KEY=your-256-bit-secret-key-here
JWT_EXPIRATION_MS=36000000  # 10 hours in milliseconds

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/urlshortener
SPRING_DATASOURCE_USERNAME=your-db-username
SPRING_DATASOURCE_PASSWORD=your-db-password

# Security
ALLOWED_ORIGINS=https://yourdomain.com
```
