# PostgreSQL Database Configuration

This application uses PostgreSQL as the database for both local development and production deployment.

## Required Environment Variables

Set these environment variables to configure the database connection:

```bash
DATABASE_URL_DRIVER=org.postgresql.Driver
DATABASE_URL=jdbc:postgresql://hostname:port/database_name
```

For Heroku's DATABASE_URL format:
```bash
DATABASE_URL=postgres://username:password@hostname:port/database_name
```

## Local Development

### Install PostgreSQL
First, install PostgreSQL on your local machine:

**macOS:**
```bash
brew install postgresql
brew services start postgresql
```

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
sudo service postgresql start
```

**Windows:**
Download and install from https://www.postgresql.org/download/windows/

### Setup Local Database
Create a local database for development:

```bash
# Create database
createdb open_agriculture_diary

# Set environment variables
export DATABASE_URL_DRIVER=org.postgresql.Driver
export DATABASE_URL=jdbc:postgresql://localhost:5432/open_agriculture_diary
```

Or create a `.env` file in your project root:
```
DATABASE_URL_DRIVER=org.postgresql.Driver
DATABASE_URL=jdbc:postgresql://localhost:5432/open_agriculture_diary
```

## Production Deployment (Heroku)

For Heroku deployment, add a PostgreSQL add-on:

```bash
# Add PostgreSQL add-on
heroku addons:create heroku-postgresql:mini

# Set the PostgreSQL driver
heroku config:set DATABASE_URL_DRIVER=org.postgresql.Driver
```

Heroku automatically provides the `DATABASE_URL` environment variable when you add the PostgreSQL add-on.

## Database Schema
The application uses Play Framework Evolutions to manage database schema changes. The schema is designed for PostgreSQL.

## Dependencies
- PostgreSQL driver: `org.postgresql:postgresql:42.7.3`
- H2 database: `com.h2database:h2:2.3.232` (for testing only)

## Testing
The application uses H2 in-memory database for testing purposes only. To run tests:

```bash
sbt test
```

For integration testing with PostgreSQL, you can run:

```bash
# Make sure PostgreSQL is running and environment variables are set
DATABASE_URL_DRIVER=org.postgresql.Driver DATABASE_URL=jdbc:postgresql://localhost:5432/open_agriculture_diary sbt test
```