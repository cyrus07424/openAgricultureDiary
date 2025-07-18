import io.ebean.PagedList;
import models.Field;
import models.User;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import repositoryies.FieldRepository;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FieldTest extends WithApplication {

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

    @Test
    public void basicRepositoryTest() {
        final FieldRepository fieldRepository = app.injector().instanceOf(FieldRepository.class);
        
        // Test that the repository can be injected and basic operations work
        CompletionStage<PagedList<Field>> stage = fieldRepository.pageByUser(0, 10, "name", "ASC", "", 999L);

        await().atMost(1, SECONDS).until(() -> {
            try {
                PagedList<Field> fields = stage.toCompletableFuture().get();
                return fields != null;
            } catch (Exception e) {
                return false;
            }
        });
    }
    
    @Test
    public void paginationEmpty() {
        final FieldRepository fieldRepository = app.injector().instanceOf(FieldRepository.class);
        CompletionStage<PagedList<Field>> stage = fieldRepository.pageByUser(0, 10, "name", "ASC", "", 999L);

        // Test that pagination works for non-existent user
        await().atMost(1, SECONDS).until(() -> {
            try {
                PagedList<Field> fields = stage.toCompletableFuture().get();
                return fields.getTotalCount() == 0 && fields.getList().size() == 0;
            } catch (Exception e) {
                return false;
            }
        });
    }
}