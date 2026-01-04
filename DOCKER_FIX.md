# Fix Docker Database Connection Issue

## Problem
Application is connecting to `localhost:5432` instead of Render.com database.

## Solution

### Step 1: Verify Environment Variables in Container

```bash
# Check if environment variables are set
docker exec json-compare-app env | grep -E "DATABASE|DB_|SPRING_PROFILES"

# Should show:
# SPRING_PROFILES_ACTIVE=prod
# DATABASE_URL=jdbc:postgresql://dpg-d5cpp4mr433s73a01le0-a.oregon-postgres.render.com:5432/json_compare_db
# DB_USERNAME=sayantan
# DB_PASSWORD=OgEpElDtWXE7JYgMlePKEDB8yN0CuJI4
```

### Step 2: Restart Container with Correct Environment

**Option A: Update docker-compose.yml and restart**
```bash
docker-compose down
docker-compose up -d
```

**Option B: Set environment variables and restart**
```bash
# PowerShell
$env:DATABASE_URL="jdbc:postgresql://dpg-d5cpp4mr433s73a01le0-a.oregon-postgres.render.com:5432/json_compare_db"
$env:DB_USERNAME="sayantan"
$env:DB_PASSWORD="OgEpElDtWXE7JYgMlePKEDB8yN0CuJI4"
$env:SPRING_PROFILES_ACTIVE="prod"

docker-compose down
docker-compose up -d
```

### Step 3: Check Application Logs

```bash
# Check startup logs
docker-compose logs app | grep -i "datasource\|database\|profile"

# Should show:
# The following profiles are active: prod
# HikariPool-1 - Starting...
# HikariPool-1 - Start completed.
```

### Step 4: Verify Database Connection

```bash
# Check if connection is successful
docker-compose logs app | grep -i "connection\|connected\|hikari"

# If you see connection errors, check:
# 1. DATABASE_URL has jdbc: prefix
# 2. Port is included (:5432)
# 3. Database is accessible from Docker container
```

## Quick Fix Commands

```bash
# 1. Stop container
docker-compose down

# 2. Set environment variables (PowerShell)
$env:DATABASE_URL="jdbc:postgresql://dpg-d5cpp4mr433s73a01le0-a.oregon-postgres.render.com:5432/json_compare_db"
$env:DB_USERNAME="sayantan"
$env:DB_PASSWORD="OgEpElDtWXE7JYgMlePKEDB8yN0CuJI4"

# 3. Start container
docker-compose up -d

# 4. Check logs
docker-compose logs -f app
```

## Common Issues

### Issue: Still connecting to localhost
**Check:**
1. SPRING_PROFILES_ACTIVE is set to `prod`
2. Environment variables are set before starting container
3. Container was restarted after setting variables

### Issue: Environment variables not persisting
**Solution:** Set them in docker-compose.yml directly or use .env file

### Issue: Database connection timeout
**Check:**
1. Render.com database allows external connections
2. Firewall/network allows outbound connections from Docker
3. DATABASE_URL format is correct (includes jdbc: and port)

## Testing Connection

```bash
# Test from inside container
docker exec -it json-compare-app sh

# Inside container, test connection
# (if psql is available)
psql $DATABASE_URL -U $DB_USERNAME

# Or check Spring Boot logs
docker-compose logs app | tail -50
```

