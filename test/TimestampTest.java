import models.Crop;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import static org.junit.Assert.*;

public class TimestampTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .configure("play.evolutions.db.default.enabled", "true")
            .configure("play.evolutions.db.default.autoApply", "true")
            .configure("play.filters.hosts.allowed.0", "localhost:19001")
            .configure("db.default.driver", "org.h2.Driver")
            .configure("db.default.url", "jdbc:h2:mem:timestamp-test")
            .build();
    }

    @Test
    public void testTimestampsAreSetOnSave() throws InterruptedException {
        // Create a new crop and save it
        Crop testCrop = new Crop();
        testCrop.setName("Test Crop for Timestamps");
        testCrop.save();
        
        // Test that both timestamps are set
        assertNotNull("Created at should be set", testCrop.getCreatedAt());
        assertNotNull("Updated at should be set", testCrop.getUpdatedAt());
        
        // Test that both timestamps are close to now
        final long nowMillis = System.currentTimeMillis();
        final long createdAtMillis = testCrop.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        final long updatedAtMillis = testCrop.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // Timestamps should be within 5 seconds of now
        assertTrue("Created at should be recent", Math.abs(nowMillis - createdAtMillis) < 5000);
        assertTrue("Updated at should be recent", Math.abs(nowMillis - updatedAtMillis) < 5000);
        
        // Store original timestamps for update test
        final java.time.LocalDateTime originalCreatedAt = testCrop.getCreatedAt();
        final java.time.LocalDateTime originalUpdatedAt = testCrop.getUpdatedAt();
        
        // Wait a moment to ensure update timestamp differs
        Thread.sleep(100);
        
        // Update the crop
        testCrop.setName("Updated Test Crop");
        testCrop.update();
        
        // Test that createdAt didn't change but updatedAt did
        assertEquals("Created at should not change on update", originalCreatedAt, testCrop.getCreatedAt());
        assertTrue("Updated at should change on update", testCrop.getUpdatedAt().isAfter(originalUpdatedAt));
    }
}