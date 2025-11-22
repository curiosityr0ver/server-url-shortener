# Rebuilding Docker Container to Fix JWT Signature Error

## Problem
The JWT signature validation error is occurring because the Docker container is running an old version of the code. The exception handling improvements need to be applied.

## Solution: Rebuild the Docker Container

### Step 1: Stop the current containers
```bash
cd server
docker-compose down
```

### Step 2: Rebuild and restart
```bash
docker-compose up --build
```

This will:
- Rebuild the application with the latest code changes
- Apply the improved JWT exception handling
- Ensure the SignatureException is properly caught and returns a 401 response

### Step 3: Verify the fix
After rebuilding, test with an invalid token. You should now get a proper 401 JSON response instead of a stack trace:
```json
{
  "error": "JWT signature validation failed",
  "status": "401"
}
```

## Alternative: Quick Restart (if code hasn't changed)
If you just need to restart without rebuilding:
```bash
docker-compose restart app
```

## Important Note
If you're using tokens generated before the rebuild, they may still fail signature validation if:
1. The JWT secret changed between token generation and validation
2. The application was restarted and the secret wasn't consistent

**Solution**: Set a consistent `JWT_SECRET` environment variable in `docker-compose.yml`:
```yaml
environment:
  JWT_SECRET: "YourConsistentSecretKeyHereAtLeast32BytesLong!!"
```

This ensures tokens remain valid across restarts.

