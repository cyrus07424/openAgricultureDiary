import io.ebean.PagedList;
import models.Crop;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import repositoryies.CropRepository;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class ModelTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .configure("play.evolutions.db.default.enabled", "true")
            .configure("play.evolutions.db.default.autoApply", "true")
            .configure("play.filters.hosts.allowed.0", "localhost:19001")
            .configure("db.default.driver", "org.h2.Driver")
            .configure("db.default.url", "jdbc:h2:mem:test")
            .build();
    }

    private String formatted(Date date) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    @Test
    public void findById() {
        final CropRepository cropRepository = app.injector().instanceOf(CropRepository.class);
        final CompletionStage<Optional<Crop>> stage = cropRepository.lookup(21L);

        await().atMost(1, SECONDS).until(() -> {
            final Optional<Crop> macintosh = stage.toCompletableFuture().get();
            return macintosh
                .map(mac -> mac.getName().equals("Macintosh") && formatted(mac.getIntroduced()).equals("1984-01-24"))
                .orElseGet(() -> false);
        });
    }
    
    @Test
    public void pagination() {
        final CropRepository cropRepository = app.injector().instanceOf(CropRepository.class);
        CompletionStage<PagedList<Crop>> stage = cropRepository.page(1, 20, "name", "ASC", "");

        // Test the completed result
        await().atMost(1, SECONDS).until(() -> {
            PagedList<Crop> crops = stage.toCompletableFuture().get();
            return crops.getTotalCount() == 574 &&
                crops.getTotalPageCount() == 29 &&
                crops.getList().size() == 20;
        });
    }
    
    @Test
    public void testTimestampsAreSetOnSave() {
        final CropRepository cropRepository = app.injector().instanceOf(CropRepository.class);
        
        // Create a new crop and save it
        Crop testCrop = new Crop();
        testCrop.setName("Test Crop for Timestamps");
        testCrop.save();
        
        await().atMost(1, SECONDS).until(() -> {
            return testCrop.getCreatedAt() != null && testCrop.getUpdatedAt() != null;
        });
        
        // Test that both timestamps are set and are close to now
        final long nowMillis = System.currentTimeMillis();
        final long createdAtMillis = testCrop.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        final long updatedAtMillis = testCrop.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // Timestamps should be within 5 seconds of now
        assert Math.abs(nowMillis - createdAtMillis) < 5000;
        assert Math.abs(nowMillis - updatedAtMillis) < 5000;
        
        // Store original timestamps for update test
        final java.time.LocalDateTime originalCreatedAt = testCrop.getCreatedAt();
        final java.time.LocalDateTime originalUpdatedAt = testCrop.getUpdatedAt();
        
        // Wait a moment to ensure update timestamp differs
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Update the crop
        testCrop.setName("Updated Test Crop");
        testCrop.update();
        
        await().atMost(1, SECONDS).until(() -> {
            return testCrop.getUpdatedAt().isAfter(originalUpdatedAt);
        });
        
        // Test that createdAt didn't change but updatedAt did
        assert testCrop.getCreatedAt().equals(originalCreatedAt);
        assert testCrop.getUpdatedAt().isAfter(originalUpdatedAt);
    }
    
}
