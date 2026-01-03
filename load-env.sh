#!/bin/bash
# Load Windows User Environment Variables into Git Bash session
# Source this file: source load-env.sh

# Function to get Windows environment variable
get_win_env() {
    powershell -Command "[Environment]::GetEnvironmentVariable('$1', 'User')" 2>/dev/null
}

# Load environment variables from Windows User Environment
export DATABASE_URL=$(get_win_env "DATABASE_URL")
export DB_USERNAME=$(get_win_env "DB_USERNAME")
export DB_PASSWORD=$(get_win_env "DB_PASSWORD")
export JWT_SECRET=$(get_win_env "JWT_SECRET")
export JWT_EXPIRATION=$(get_win_env "JWT_EXPIRATION")
export JSON_MAX_SIZE=$(get_win_env "JSON_MAX_SIZE")

echo "Environment variables loaded from Windows User Environment:"
echo "DATABASE_URL=$DATABASE_URL"
echo "DB_USERNAME=$DB_USERNAME"
echo "DB_PASSWORD=***"
echo "JWT_SECRET=***"
echo "JWT_EXPIRATION=$JWT_EXPIRATION"
echo "JSON_MAX_SIZE=$JSON_MAX_SIZE"
