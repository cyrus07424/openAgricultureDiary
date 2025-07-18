# PostgreSQL Configuration for Heroku Deployment

This application now supports PostgreSQL for production deployment on Heroku while maintaining H2 database support for local development.

## Configuration

### Local Development (H2)
By default, the application uses H2 in-memory database for local development. No additional configuration is needed.

### Production Deployment (PostgreSQL)
For Heroku deployment, set the following environment variables:

```bash
DATABASE_URL_DRIVER=org.postgresql.Driver
DATABASE_URL=jdbc:postgresql://hostname:port/database_name
# Or use Heroku's DATABASE_URL format:
# DATABASE_URL=postgres://username:password@hostname:port/database_name
```

### Heroku Deployment
Heroku automatically provides the `DATABASE_URL` environment variable when you add a PostgreSQL add-on:

```bash
# Add PostgreSQL add-on
heroku addons:create heroku-postgresql:mini

# Set the PostgreSQL driver
heroku config:set DATABASE_URL_DRIVER=org.postgresql.Driver
```

## Database Schema
The database schema is compatible with both H2 and PostgreSQL. The application uses Play Framework Evolutions to manage database schema changes.

## Dependencies
- PostgreSQL driver: `org.postgresql:postgresql:42.7.3`
- H2 database: `com.h2database:h2:2.3.232` (for local development)

## Testing
To test PostgreSQL configuration locally:

```bash
# Set environment variables and run tests
DATABASE_URL_DRIVER=org.postgresql.Driver DATABASE_URL=jdbc:postgresql://localhost:5432/your_db sbt test
```

Note: You need a running PostgreSQL instance for this to work.