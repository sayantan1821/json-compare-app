# Quick Fix for Database Connection

## The Problem
Spring Boot is connecting to `localhost:5432` instead of Render.com database.

## Solution Applied

1. **Removed fallback defaults** - Environment variables now set directly in docker-compose.yml
2. **Removed localhost fallback** - application-prod.properties no longer falls back to localhost
3. **Added debug logging** - Will show what database URL is being used

## Steps to Fix

### 1. Rebuild and Restart

```bash
# Stop everything
docker-compose down

# Rebuild (to include updated application-prod.properties)
docker-compose build

# Start
docker-compose up -d

# Watch logs
docker-compose logs -f app
```

### 2. Verify Environment Variables

```bash
# Check what's set in container
docker-compose exec app env | grep -E "DATABASE|DB_|SPRING_PROFILES"
```

Should show:
```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://dpg-d5cpp4mr433s73a01le0-a.oregon-postgres.render.com:5432/json_compare_db
DB_USERNAME=sayantan
DB_PASSWORD=OgEpElDtWXE7JYgMlePKEDB8yN0CuJI4
```

### 3. Check Application Logs

```bash
# Look for profile and datasource info
docker-compose logs app | grep -i "profile\|datasource\|hikari\|jdbc"
```

Should show:
- `The following profiles are active: prod`
- `HikariPool-1 - Starting...`
- Connection to Render.com database (not localhost)

## If Still Connecting to Localhost

### Check 1: Profile is Active
```bash
docker-compose logs app | grep "profiles are active"
```
Must show: `prod`

### Check 2: Environment Variables in Container
```bash
docker-compose exec app printenv | grep DATABASE
```

### Check 3: Rebuild Image
The application-prod.properties changes require a rebuild:
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## Debug Mode

The logs will now show:
- Which profile is active
- What datasource URL is being used
- HikariCP connection attempts

Look for lines like:
```
HikariPool-1 - Starting...
HikariPool-1 - Driver does not support get/set network timeout
HikariPool-1 - Start completed.
```

If you see connection errors, they'll show the actual URL being used.

