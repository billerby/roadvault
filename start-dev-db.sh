#!/bin/bash

# Start PostgreSQL and MailDev for development
echo "Starting PostgreSQL and MailDev containers for development..."

# Stop any existing containers
docker-compose -f docker-compose.dev.yml down

# Start services
docker-compose -f docker-compose.dev.yml up -d

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker-compose -f docker-compose.dev.yml exec db pg_isready -U postgres; do
  echo "PostgreSQL is unavailable - sleeping"
  sleep 1
done

echo "PostgreSQL is ready!"
echo "MailDev is starting..."
sleep 3

echo ""
echo "=== DEVELOPMENT ENVIRONMENT READY ==="
echo ""
echo "Database connection:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: roadvault"
echo "  Username: postgres"
echo "  Password: postgres"
echo ""
echo "Email testing (MailDev):"
echo "  SMTP Server: localhost:1025"
echo "  Web Interface: http://localhost:1080"
echo ""
echo "You can now start your Spring Boot application in IntelliJ"
echo "To stop services: docker-compose -f docker-compose.dev.yml down"
echo ""
