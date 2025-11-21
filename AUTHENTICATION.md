# JWT Authentication Guide

This application uses **JSON Web Tokens (JWT)** for secure authentication and authorization.

## Overview

- **Algorithm**: HS256 (HMAC with SHA-256)
- **Token Expiration**: 10 hours
- **Session Management**: Stateless
- **Password Encryption**: BCrypt

## Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |
| GET | `/{shortCode}` | Redirect to original URL |

## Protected Endpoints (Authentication Required)

All other API endpoints require a valid JWT token in the `Authorization` header.

## API Usage

### 1. Register a New User

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response**:
```json
"User registered successfully"
```

**Status Code**: `200 OK`

---

### 2. Login

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "username": "johndoe",
  "password": "securePassword123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjg..."
}
```

**Status Code**: `200 OK`

**Error Response** (Invalid credentials):
```json
{
  "message": "Incorrect username or password"
}
```

**Status Code**: `403 Forbidden` or `401 Unauthorized`

---

### 3. Using the JWT Token

For all protected endpoints, include the JWT token in the `Authorization` header:

**Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNjg...
```

**Example with cURL**:
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "originalUrl": "https://www.example.com",
    "userId": 1
  }'
```

**Example with Postman**:
1. Go to the **Authorization** tab
2. Select **Type**: Bearer Token
3. Paste your JWT token in the **Token** field

**Example with JavaScript (fetch)**:
```javascript
const token = "YOUR_JWT_TOKEN";

fetch('http://localhost:8080/api/urls', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    originalUrl: 'https://www.example.com',
    userId: 1
  })
})
.then(response => response.json())
.then(data => console.log(data));
```

---

## Security Features

### 1. Password Hashing
- User passwords are hashed using **BCrypt** before storage
- No plain-text passwords are stored in the database

### 2. Token Structure
JWT tokens contain:
- **Subject (sub)**: Username
- **Issued At (iat)**: Timestamp when token was created
- **Expiration (exp)**: Timestamp when token expires (10 hours from creation)

### 3. Token Validation
On each request to a protected endpoint:
1. Extract token from `Authorization` header
2. Validate token signature
3. Check if token is expired
4. Verify username matches the token subject
5. Load user details and set authentication in security context

### 4. Stateless Sessions
- No server-side session storage
- All authentication state is contained in the JWT token
- Scalable for distributed systems

---

## Error Handling

| Scenario | HTTP Status | Response |
|----------|-------------|----------|
| Missing token | 403 Forbidden | Access Denied |
| Invalid token | 403 Forbidden | Access Denied |
| Expired token | 403 Forbidden | Access Denied |
| Wrong username/password | 401 Unauthorized | Incorrect username or password |
| User not found | 404 Not Found | User not found with username: {username} |

---

## Testing Authentication Flow

### Step 1: Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123456"
  }'
```

### Step 2: Login to Get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456"
  }'
```

**Save the token from the response!**

### Step 3: Use Token to Access Protected Endpoints
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "originalUrl": "https://www.google.com",
    "userId": 1
  }'
```

---

## Best Practices

1. **Store tokens securely**
   - Use `httpOnly` cookies for web applications
   - Use secure storage in mobile applications
   - Never expose tokens in URLs

2. **Token expiration**
   - Current expiration is 10 hours
   - Implement refresh token mechanism for longer sessions

3. **HTTPS in production**
   - Always use HTTPS to prevent token interception
   - Tokens transmitted over HTTP can be stolen

4. **Logout**
   - Client-side: Delete the stored token
   - Optional: Implement token blacklist for server-side logout

5. **Token refresh**
   - Consider implementing refresh tokens for better security
   - Short-lived access tokens + long-lived refresh tokens

---

## Security Configuration Details

### Protected by Default
All endpoints are **protected by default** except those explicitly marked as public in `SecurityConfig.java`.

### CORS Configuration
Currently, the `UrlController` has `@CrossOrigin(origins = "*")` which allows all origins. **Update this in production** to restrict to specific domains:

```java
@CrossOrigin(origins = "https://yourdomain.com")
```

### Custom Security Rules
To modify access rules, edit `SecurityConfig.java`:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/{shortCode}").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN") // Example
    .anyRequest().authenticated())
```

---

## Troubleshooting

### "Access Denied" Error
- Ensure you're including the `Authorization` header
- Verify the token hasn't expired (10-hour limit)
- Check the token format: `Bearer {token}`

### "Incorrect username or password"
- Verify username and password are correct
- Ensure the user has been registered
- Check database connectivity

### Token Not Working
- Verify token is not expired
- Ensure no extra spaces in the Authorization header
- Check that the server's secret key hasn't changed (would invalidate all tokens)

---

## Future Enhancements

Consider implementing:
- **Refresh tokens** for extended sessions
- **Role-based access control** (RBAC) for admin/user roles
- **Token blacklist** for logout functionality
- **Multi-factor authentication** (MFA)
- **OAuth2 integration** (Google, GitHub, etc.)
- **Rate limiting** to prevent brute-force attacks
