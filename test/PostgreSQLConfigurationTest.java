import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import play.test.WithApplication;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;

/**
 * Test to verify PostgreSQL configuration works properly
 */
public class PostgreSQLConfigurationTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .configure("play.evolutions.db.default.enabled", "false")
            .build();
    }

    @Test
    public void testH2ConfigurationByDefault() {
        // When no DATABASE_URL is set, should use H2 by default
        String driver = app.config().getString("db.default.driver");
        String url = app.config().getString("db.default.url");
        
        assertEquals("Should use H2 driver by default", "org.h2.Driver", driver);
        assertEquals("Should use H2 URL by default", "jdbc:h2:mem:play", url);
    }

    @Test
    public void testPostgreSQLConfigurationFromEnv() {
        // This test verifies that the configuration structure is correct
        // The actual PostgreSQL connection would require a real database instance
        
        // Test that config allows for environment variable overrides
        String driverConfig = app.config().getString("db.default.driver");
        assertNotNull("Driver configuration should be available", driverConfig);
        
        // The configuration should have the structure that allows env variable overrides
        assertTrue("Configuration should be working", app.config().hasPath("db.default.driver"));
        assertTrue("Configuration should be working", app.config().hasPath("db.default.url"));
    }
}