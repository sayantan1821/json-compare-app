@echo off
REM Script to set environment variables for the application (Windows)

set DATABASE_URL=jdbc:postgresql://localhost:5432/json_compare_db
set DB_USERNAME=postgres
set DB_PASSWORD=postgres
set JWT_SECRET=your-secret-key-change-this-in-production-minimum-256-bits
set JWT_EXPIRATION=3600000
set JSON_MAX_SIZE=10485760

echo Environment variables set:
echo DATABASE_URL=%DATABASE_URL%
echo DB_USERNAME=%DB_USERNAME%
echo DB_PASSWORD=***
