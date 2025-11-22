#!/bin/sh
# Entrypoint script to parse DATABASE_URL for Render deployment
# This script parses Render's DATABASE_URL format: postgresql://user:password@host:port/database
# and sets individual Spring Boot environment variables

if [ -n "$DATABASE_URL" ]; then
  # Parse DATABASE_URL format: postgresql://user:password@host:port/database
  # Remove the postgresql:// prefix
  DB_URL=${DATABASE_URL#postgresql://}
  
  # Extract user:password@host:port/database
  # Split by @ to separate credentials from host
  CREDENTIALS_HOST=${DB_URL%@*}
  HOST_DB=${DB_URL#*@}
  
  # Extract user and password
  DB_USER=${CREDENTIALS_HOST%%:*}
  DB_PASSWORD=${CREDENTIALS_HOST#*:}
  DB_PASSWORD=${DB_PASSWORD%@*}
  
  # Extract host, port, and database
  HOST_PORT=${HOST_DB%%/*}
  DB_NAME=${HOST_DB#*/}
  
  # Extract host and port
  DB_HOST=${HOST_PORT%%:*}
  DB_PORT=${HOST_PORT#*:}
  
  # If port is not specified, use default PostgreSQL port
  if [ "$DB_PORT" = "$HOST_PORT" ]; then
    DB_PORT=5432
  fi
  
  # Set Spring Boot environment variables
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
  export SPRING_DATASOURCE_USERNAME="${DB_USER}"
  export SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD}"
  export SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.postgresql.Driver"
  
  echo "Parsed DATABASE_URL and set Spring Boot datasource variables"
fi

# Execute the main command
exec "$@"

