-- Database setup script for JSON Compare Application
-- Run this script as PostgreSQL superuser (usually 'postgres')

-- Create database (if it doesn't exist)
SELECT 'CREATE DATABASE json_compare_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'json_compare_db')\gexec

-- Connect to the database
\c json_compare_db

-- Create user if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'json_compare_admin') THEN
        CREATE USER json_compare_admin WITH PASSWORD 'password123';
    END IF;
END
$$;

-- Grant all privileges on database
GRANT ALL PRIVILEGES ON DATABASE json_compare_db TO json_compare_admin;

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO json_compare_admin;
GRANT CREATE ON SCHEMA public TO json_compare_admin;

-- Grant all privileges on all tables in public schema (current and future)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO json_compare_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO json_compare_admin;

-- Make the user the owner of the public schema (optional, but gives full control)
ALTER SCHEMA public OWNER TO json_compare_admin;

-- Verify
\du json_compare_admin




create table comparison_diffs_test (deleted boolean not null,identical boolean not null,created_at timestamp(6) not null,input_string_b TEXT not null,last_compared_at timestamp(6),updated_at timestamp(6),created_by_id uuid not null,id uuid not null,comparison_status varchar(50) not null check (comparison_status in ('COMPLETED','FAILED','IN_PROGRESS','ARCHIVED')),description TEXT,input_string_a TEXT not null,result TEXT not null, primary key (id))