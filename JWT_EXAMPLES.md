# JWT Authentication - Quick Start Examples

## Using cURL

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**Expected Response:**
```
User registered successfully
```

---

### 2. Login and Get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTczNzQ3..."
}
```

**💡 Save this token - you'll need it for authenticated requests!**

---

### 3. Create a Shortened URL (Authenticated)
```bash
# Replace YOUR_JWT_TOKEN with the token from step 2
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "originalUrl": "https://www.google.com",
    "userId": 1
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.google.com",
  "createdAt": "2025-11-21T19:58:24",
  "expireAt": null,
  "hitCount": 0
}
```

---

### 4. Access a Shortened URL (No Authentication Required)
```bash
curl -L http://localhost:8080/abc123
```

This will redirect to the original URL.

---

## Using Postman

### Setup
1. Open Postman
2. Create a new Collection called "URL Shortener API"

### 1. Register User

- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/register`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "username": "jane_smith",
    "email": "jane@example.com",
    "password": "MyPassword456"
  }
  ```

### 2. Login

- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/login`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "username": "jane_smith",
    "password": "MyPassword456"
  }
  ```

**📝 Copy the token from the response!**

### 3. Set Up Authorization for Protected Requests

For all subsequent requests:

1. Go to the **Authorization** tab
2. **Type:** Bearer Token
3. **Token:** Paste your JWT token here

**OR** Add it manually to headers:
- **Key:** `Authorization`
- **Value:** `Bearer YOUR_JWT_TOKEN`

### 4. Create URL (Protected)

- **Method:** POST
- **URL:** `http://localhost:8080/api/urls`
- **Authorization:** Bearer Token (from step 3)
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "originalUrl": "https://github.com",
    "userId": 1
  }
  ```

---

## Using JavaScript (Frontend)

### Complete Authentication Flow

```javascript
const API_BASE_URL = 'http://localhost:8080';
let jwtToken = null;

// 1. Register a new user
async function registerUser(username, email, password) {
  const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, email, password })
  });
  
  if (!response.ok) {
    throw new Error('Registration failed');
  }
  
  return await response.text();
}

// 2. Login and get JWT token
async function login(username, password) {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  });
  
  if (!response.ok) {
    throw new Error('Login failed');
  }
  
  const data = await response.json();
  jwtToken = data.token;
  
  // Store token in localStorage (for web apps)
  localStorage.setItem('jwt_token', jwtToken);
  
  return jwtToken;
}

// 3. Create a shortened URL (authenticated request)
async function createShortUrl(originalUrl, userId) {
  const response = await fetch(`${API_BASE_URL}/api/urls`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({ originalUrl, userId })
  });
  
  if (!response.ok) {
    throw new Error('Failed to create short URL');
  }
  
  return await response.json();
}

// 4. Get user's URLs (authenticated request)
async function getUserUrls(userId) {
  const response = await fetch(`${API_BASE_URL}/api/users/${userId}/urls`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });
  
  if (!response.ok) {
    throw new Error('Failed to fetch user URLs');
  }
  
  return await response.json();
}

// Usage Example
async function main() {
  try {
    // Register
    await registerUser('alice', 'alice@example.com', 'AlicePass123');
    console.log('User registered successfully!');
    
    // Login
    const token = await login('alice', 'AlicePass123');
    console.log('Logged in! Token:', token);
    
    // Create short URL
    const shortUrl = await createShortUrl('https://www.example.com', 1);
    console.log('Short URL created:', shortUrl);
    
    // Get user's URLs
    const urls = await getUserUrls(1);
    console.log('User URLs:', urls);
    
  } catch (error) {
    console.error('Error:', error.message);
  }
}

// Run the example
main();
```

### React Example

```jsx
import React, { useState } from 'react';

function LoginComponent() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [token, setToken] = useState(null);

  const handleLogin = async (e) => {
    e.preventDefault();
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      });
      
      if (!response.ok) {
        throw new Error('Login failed');
      }
      
      const data = await response.json();
      setToken(data.token);
      localStorage.setItem('jwt_token', data.token);
      
      alert('Login successful!');
    } catch (error) {
      alert('Login failed: ' + error.message);
    }
  };

  return (
    <div>
      <h2>Login</h2>
      <form onSubmit={handleLogin}>
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit">Login</button>
      </form>
      
      {token && (
        <div>
          <h3>Your JWT Token:</h3>
          <code>{token}</code>
        </div>
      )}
    </div>
  );
}

export default LoginComponent;
```

---

## Using Python

```python
import requests

API_BASE_URL = 'http://localhost:8080'

# 1. Register a user
def register_user(username, email, password):
    response = requests.post(
        f'{API_BASE_URL}/api/auth/register',
        json={
            'username': username,
            'email': email,
            'password': password
        }
    )
    response.raise_for_status()
    return response.text

# 2. Login and get token
def login(username, password):
    response = requests.post(
        f'{API_BASE_URL}/api/auth/login',
        json={
            'username': username,
            'password': password
        }
    )
    response.raise_for_status()
    return response.json()['token']

# 3. Create short URL (authenticated)
def create_short_url(token, original_url, user_id):
    response = requests.post(
        f'{API_BASE_URL}/api/urls',
        json={
            'originalUrl': original_url,
            'userId': user_id
        },
        headers={
            'Authorization': f'Bearer {token}'
        }
    )
    response.raise_for_status()
    return response.json()

# 4. Get user's URLs (authenticated)
def get_user_urls(token, user_id):
    response = requests.get(
        f'{API_BASE_URL}/api/users/{user_id}/urls',
        headers={
            'Authorization': f'Bearer {token}'
        }
    )
    response.raise_for_status()
    return response.json()

# Usage Example
if __name__ == '__main__':
    try:
        # Register
        register_user('bob', 'bob@example.com', 'BobPass789')
        print('User registered successfully!')
        
        # Login
        token = login('bob', 'BobPass789')
        print(f'Logged in! Token: {token}')
        
        # Create short URL
        short_url = create_short_url(token, 'https://www.python.org', 1)
        print(f'Short URL created: {short_url}')
        
        # Get user's URLs
        urls = get_user_urls(token, 1)
        print(f'User URLs: {urls}')
        
    except requests.exceptions.HTTPError as e:
        print(f'Error: {e}')
```

---

## Testing with HTTPie

```bash
# 1. Register
http POST localhost:8080/api/auth/register \
  username=charlie \
  email=charlie@example.com \
  password=CharliePass321

# 2. Login
http POST localhost:8080/api/auth/login \
  username=charlie \
  password=CharliePass321

# 3. Create URL (replace TOKEN with actual token)
http POST localhost:8080/api/urls \
  Authorization:"Bearer TOKEN" \
  originalUrl=https://www.example.com \
  userId:=1
```

---

## Common Issues & Solutions

### Issue: "Access Denied" or 403 Forbidden

**Solutions:**
- Make sure you're including `Authorization: Bearer {token}` header
- Verify the token hasn't expired (10-hour limit)
- Ensure there's a space after "Bearer"
- Check you're using the correct endpoint

### Issue: "Incorrect username or password"

**Solutions:**
- Verify the user exists (register first)
- Check username and password are spelled correctly
- Ensure password meets any requirements

### Issue: Token expired

**Solution:**
- Login again to get a new token
- Tokens expire after 10 hours

---

## Pro Tips

1. **Store token securely**
   - Use `localStorage` for web apps (but be aware of XSS risks)
   - Use secure storage APIs for mobile apps
   - Use environment variables for server-to-server

2. **Check token expiration**
   ```javascript
   function isTokenExpired(token) {
     const payload = JSON.parse(atob(token.split('.')[1]));
     return Date.now() >= payload.exp * 1000;
   }
   ```

3. **Automatic token refresh**
   - Implement interceptors to automatically refresh tokens
   - Or redirect to login when token expires

4. **Logout**
   ```javascript
   function logout() {
     localStorage.removeItem('jwt_token');
     // Redirect to login page
   }
   ```
