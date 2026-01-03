#!/bin/bash
# Script to set environment variables for the application

export DATABASE_URL="jdbc:postgresql://localhost:5432/json_compare_db"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"  # Change this to your actual password
export JWT_SECRET="your-secret-key-change-this-in-production-minimum-256-bits"
export JWT_EXPIRATION="3600000"
export JSON_MAX_SIZE="10485760"

echo "Environment variables set:"
echo "DATABASE_URL=$DATABASE_URL"
echo "DB_USERNAME=$DB_USERNAME"
echo "DB_PASSWORD=***"
