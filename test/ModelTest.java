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
        return new GuiceApplicationBuilder().build();
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
    
}
