# JWT Authentication - Quick Reference Card

## 🚀 Quick Start (3 Steps)

### 1️⃣ Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","email":"user1@mail.com","password":"pass123"}'
```

### 2️⃣ Login & Get Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass123"}'
```
**📝 Copy the token from response!**

### 3️⃣ Use Token
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"originalUrl":"https://google.com","userId":1}'
```

---

## 📋 Key Components

| Component | Location | Purpose |
|-----------|----------|---------|
| **JwtUtil** | `security/JwtUtil.java` | Generate & validate tokens |
| **JwtAuthenticationFilter** | `security/JwtAuthenticationFilter.java` | Intercept & validate requests |
| **SecurityConfig** | `security/SecurityConfig.java` | Configure Spring Security |
| **CustomUserDetailsService** | `service/CustomUserDetailsService.java` | Load user data |
| **AuthController** | `controller/AuthController.java` | Login/Register endpoints |

---

## 🔐 API Endpoints

### Public (No Auth Required)
| Method | Endpoint | Use Case |
|--------|----------|----------|
| POST | `/api/auth/register` | Create new account |
| POST | `/api/auth/login` | Get JWT token |
| GET | `/{shortCode}` | Redirect to URL |

### Protected (Auth Required)
| Method | Endpoint | Use Case |
|--------|----------|----------|
| POST | `/api/urls` | Create short URL |
| GET | `/api/urls/{shortCode}` | Get URL details |
| DELETE | `/api/urls/{id}` | Delete URL |
| GET | `/api/users/{userId}/urls` | Get user's URLs |

---

## 🎯 Configuration

**Token Lifetime:** 10 hours  
**Algorithm:** HS256  
**Password Hash:** BCrypt  
**Session:** Stateless  

---

## ✅ Security Checklist

- ✅ Passwords hashed with BCrypt
- ✅ Tokens expire after 10 hours
- ✅ Stateless authentication (no cookies)
- ✅ Secure token signing (HS256)
- ✅ Public endpoints for redirect & auth
- ⚠️ Use HTTPS in production
- ⚠️ Update CORS settings for production

---

## 🛠️ Common Commands

**Start Application:**
```bash
./mvnw spring-boot:run
```

**Compile:**
```bash
./mvnw compile
```

**Run Tests:**
```bash
./mvnw test
```

**Build:**
```bash
./mvnw clean package
```

---

## 📖 Documentation Files

1. **JWT_IMPLEMENTATION_SUMMARY.md** - Complete overview
2. **AUTHENTICATION.md** - Detailed guide
3. **JWT_EXAMPLES.md** - Code examples (cURL, JS, Python)
4. **JWT_FLOW_DIAGRAM.md** - Visual diagrams
5. **This file** - Quick reference

---

## 🐛 Troubleshooting

**403 Forbidden?**
- Check `Authorization: Bearer TOKEN` header
- Verify token not expired
- Ensure space after "Bearer"

**Login Failed?**
- Verify username/password correct
- Ensure user registered first

**Token Expired?**
- Login again to get new token
- Tokens last 10 hours

---

## 💡 Pro Tips

**Store Token:**
```javascript
// Save
localStorage.setItem('jwt_token', token);

// Retrieve
const token = localStorage.getItem('jwt_token');

// Delete (logout)
localStorage.removeItem('jwt_token');
```

**Check Expiration:**
```javascript
function isExpired(token) {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return Date.now() >= payload.exp * 1000;
}
```

**Auto-include in Requests:**
```javascript
fetch(url, {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
  }
});
```

---

## 📞 Need Help?

1. Check **AUTHENTICATION.md** for details
2. See **JWT_EXAMPLES.md** for code samples
3. View **JWT_FLOW_DIAGRAM.md** for visual flows
4. Review tests in `JwtAuthenticationTest.java`

---

**Status:** ✅ JWT Authentication Fully Implemented  
**Last Updated:** 2025-11-21  
**Version:** 1.0
