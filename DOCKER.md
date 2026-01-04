# Docker Setup Guide

## Building the Docker Image

### Basic Build
```bash
docker build -t json-compare-app .
```

### Build with Tag
```bash
docker build -t json-compare-app:latest .
```

## Running the Container

### Run with Environment Variables
```bash
docker run -d \
  --name json-compare-app \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/json_compare_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  json-compare-app
```

### Run with External Database
```bash
docker run -d \
  --name json-compare-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://your-db-host:5432/json_compare_db \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  json-compare-app
```

## Using Docker Compose (Recommended)

### Start All Services
```bash
docker-compose up -d
```

### View Logs
```bash
docker-compose logs -f app
```

### Stop Services
```bash
docker-compose down
```

### Stop and Remove Volumes
```bash
docker-compose down -v
```

### Rebuild After Changes
```bash
docker-compose up -d --build
```

## Common Issues

### Issue: "Cannot find gradlew"
**Solution:** Make sure `gradlew` and `gradle/` directory are in the project root.

### Issue: "Port already in use"
**Solution:** Change the port mapping in docker-compose.yml or stop the conflicting service.

### Issue: "Database connection failed"
**Solution:** 
- Check database is running: `docker-compose ps db`
- Verify environment variables are set correctly
- Check database logs: `docker-compose logs db`

### Issue: "Health check failing"
**Solution:** 
- Wait longer for app to start (health check has 40s start period)
- Check app logs: `docker-compose logs app`
- Verify `/health` endpoint is accessible

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |
| `DATABASE_URL` | JDBC URL | Required |
| `DB_USERNAME` | Database username | Required |
| `DB_PASSWORD` | Database password | Required |
| `JWT_SECRET` | JWT secret key | (not used yet) |
| `JSON_MAX_SIZE` | Max JSON size | `10485760` |

## Production Considerations

1. **Use .env file** - Don't commit secrets to docker-compose.yml
2. **Use secrets management** - Use Docker secrets or external secret managers
3. **Use specific tags** - Don't use `:latest` in production
4. **Resource limits** - Add memory/CPU limits in docker-compose.yml
5. **Health checks** - Already configured, monitor them
6. **Logging** - Configure log rotation and aggregation

## Multi-stage Build Benefits

The Dockerfile uses a multi-stage build:
- **Stage 1**: Builds the application (includes Gradle, JDK)
- **Stage 2**: Runtime image (only JRE, smaller size)

This results in a smaller final image (~200MB vs ~500MB+).

