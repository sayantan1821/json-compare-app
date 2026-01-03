-- Simple database setup - Run as postgres superuser
-- Connect to PostgreSQL: psql -U postgres

-- 1. Create database
CREATE DATABASE json_compare_db;

-- 2. Create user and set password
CREATE USER json_compare_admin WITH PASSWORD 'password123';

-- 3. Grant privileges
GRANT ALL PRIVILEGES ON DATABASE json_compare_db TO json_compare_admin;

-- 4. Connect to the database
\c json_compare_db

-- 5. Grant schema permissions
GRANT ALL ON SCHEMA public TO json_compare_admin;
GRANT CREATE ON SCHEMA public TO json_compare_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO json_compare_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO json_compare_admin;
