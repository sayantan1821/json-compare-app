# Docker Environment Variables Setup

## Quick Setup Options

### Option 1: Using docker-compose with Environment Variables (Recommended)

**Method A: Set in docker-compose.yml directly**
Edit `docker-compose.yml` and update the environment section:
```yaml
environment:
  - DATABASE_URL=jdbc:postgresql://your-host:5432/json_compare_db
  - DB_USERNAME=your_username
  - DB_PASSWORD=your_password
```

**Method B: Use docker-compose.override.yml (Better for secrets)**
1. Copy `docker-compose.override.yml.example` to `docker-compose.override.yml`
2. Fill in your values
3. Run: `docker-compose up -d`

**Method C: Use .env file**
1. Create `.env` file in project root:
```env
DATABASE_URL=jdbc:postgresql://your-host:5432/json_compare_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
SPRING_PROFILES_ACTIVE=prod
```
2. Uncomment `env_file` in docker-compose.yml
3. Run: `docker-compose up -d`

### Option 2: Using docker run command

```bash
docker run -d \
  --name json-compare-app \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://your-host:5432/json_compare_db \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  -e SPRING_PROFILES_ACTIVE=prod \
  json-compare-app
```

### Option 3: Using Environment File with docker run

1. Create `docker.env` file:
```env
DATABASE_URL=jdbc:postgresql://your-host:5432/json_compare_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
SPRING_PROFILES_ACTIVE=prod
```

2. Run:
```bash
docker run -d \
  --name json-compare-app \
  -p 8080:8080 \
  --env-file docker.env \
  json-compare-app
```

## For Your Render.com Database

If using Render.com database, update docker-compose.yml:

```yaml
environment:
  - DATABASE_URL=jdbc:postgresql://dpg-d5cpp4mr433s73a01le0-a.oregon-postgres.render.com:5432/json_compare_db
  - DB_USERNAME=sayantan
  - DB_PASSWORD=OgEpElDtWXE7JYgMlePKEDB8yN0CuJI4
  - SPRING_PROFILES_ACTIVE=prod
```

**Important:** If using external database (Render.com), comment out or remove the `db` service from docker-compose.yml.

## Verify Environment Variables

### Check in running container:
```bash
# List all environment variables
docker exec json-compare-app env

# Check specific variable
docker exec json-compare-app env | grep DATABASE_URL

# Or check via docker-compose
docker-compose exec app env | grep DATABASE
```

### Check application logs:
```bash
docker-compose logs app | grep -i "datasource\|database\|profile"
```

## Common Issues

### Issue: Variables not being read
**Solution:** 
- Restart container: `docker-compose restart app`
- Rebuild: `docker-compose up -d --build`
- Check variable names match exactly (case-sensitive)

### Issue: Using external database but db service still starting
**Solution:** Comment out db service in docker-compose.yml or use profiles:
```yaml
db:
  profiles:
    - dont-start
```

Then run: `docker-compose --profile dont-start up -d`

### Issue: Database connection fails
**Check:**
1. DATABASE_URL has `jdbc:postgresql://` prefix
2. Port is included (usually `:5432`)
3. Database is accessible from Docker container
4. For external DBs, ensure firewall allows connections

## Quick Commands

```bash
# Start with environment variables
docker-compose up -d

# View environment variables
docker-compose exec app env

# View logs
docker-compose logs -f app

# Restart with new env vars
docker-compose restart app

# Stop
docker-compose down
```

