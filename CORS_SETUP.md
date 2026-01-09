# CORS Configuration for Deployment

## Quick Setup

### For Docker Deployment

**Option 1: Set in docker-compose.yml**
Edit `docker-compose.yml` and update the `CORS_ALLOWED_ORIGINS` environment variable:

```yaml
environment:
  - CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

**Option 2: Set via Environment Variable**
```bash
# PowerShell
$env:CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com"
docker-compose up -d

# Or in docker-compose.yml, it will read from your environment
```

**Option 3: Use .env file**
Create `.env` file:
```env
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

Then uncomment in docker-compose.yml:
```yaml
env_file:
  - .env
```

### For Production Deployment (Non-Docker)

Set environment variable:
```bash
export CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

Or in `application-prod.properties`, set directly:
```properties
cors.allowed-origins=https://yourdomain.com,https://www.yourdomain.com
```

## Multiple Origins

Separate multiple origins with commas (no spaces):
```yaml
- CORS_ALLOWED_ORIGINS=https://app.yourdomain.com,https://www.yourdomain.com,https://staging.yourdomain.com
```

## Common Frontend URLs

- **Vercel**: `https://your-app.vercel.app`
- **Netlify**: `https://your-app.netlify.app`
- **GitHub Pages**: `https://username.github.io`
- **Custom Domain**: `https://yourdomain.com`

## Verify CORS is Working

### Test with curl:
```bash
curl -H "Origin: https://yourdomain.com" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://your-api-url/api/auth/login \
     -v
```

Should return headers:
```
Access-Control-Allow-Origin: https://yourdomain.com
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Credentials: true
```

### Test from Browser Console:
```javascript
fetch('http://your-api-url/api/health', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
  }
})
.then(r => r.json())
.then(console.log)
.catch(console.error);
```

## Troubleshooting

### Issue: Still getting CORS errors
1. **Check origin matches exactly** - No trailing slashes, correct protocol (http vs https)
2. **Rebuild container** - CORS config changes require rebuild:
   ```bash
   docker-compose down
   docker-compose build
   docker-compose up -d
   ```
3. **Check browser console** - Look for the exact origin in the error message
4. **Verify CORS_ALLOWED_ORIGINS is set**:
   ```bash
   docker-compose exec app env | grep CORS
   ```

### Issue: Preflight (OPTIONS) requests failing
- CORS is configured to handle OPTIONS automatically
- Check that `OPTIONS` is in allowed methods (it is by default)
- Verify `Access-Control-Request-Headers` are allowed

### Issue: Credentials not working
- Make sure `allowCredentials: true` is set (it is by default)
- Frontend must include `credentials: 'include'` in fetch:
  ```javascript
  fetch(url, {
    credentials: 'include'
  })
  ```

## Security Notes

⚠️ **Never use wildcard (`*`) in production** - Always specify exact origins

✅ **Good**: `https://yourdomain.com,https://www.yourdomain.com`
❌ **Bad**: `*` or `https://*.yourdomain.com`

## Quick Reference

| Setting | Value |
|---------|-------|
| Allowed Methods | GET, POST, PUT, PATCH, DELETE, OPTIONS |
| Allowed Headers | Content-Type, Authorization, X-Requested-With, Accept, Origin |
| Credentials | true (enabled) |
| Max Age | 3600 seconds (1 hour) |

