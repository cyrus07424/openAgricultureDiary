import io.ebean.PagedList;
import models.WorkHistory;
import models.User;
import models.Field;
import models.Crop;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import repositoryies.WorkHistoryRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorkHistoryTest extends WithApplication {

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
        final WorkHistoryRepository workHistoryRepository = app.injector().instanceOf(WorkHistoryRepository.class);
        
        // Test that the repository can be injected and basic operations work
        CompletionStage<PagedList<WorkHistory>> stage = workHistoryRepository.pageByUser(0, 10, "date", "DESC", "", 999L);

        await().atMost(1, SECONDS).until(() -> {
            try {
                PagedList<WorkHistory> workHistories = stage.toCompletableFuture().get();
                return workHistories != null;
            } catch (Exception e) {
                return false;
            }
        });
    }
    
    @Test
    public void paginationEmpty() {
        final WorkHistoryRepository workHistoryRepository = app.injector().instanceOf(WorkHistoryRepository.class);
        CompletionStage<PagedList<WorkHistory>> stage = workHistoryRepository.pageByUser(0, 10, "date", "DESC", "", 999L);

        // Test that pagination works for non-existent user
        await().atMost(1, SECONDS).until(() -> {
            try {
                PagedList<WorkHistory> workHistories = stage.toCompletableFuture().get();
                return workHistories.getTotalCount() == 0 && workHistories.getList().size() == 0;
            } catch (Exception e) {
                return false;
            }
        });
    }
    
    @Test
    public void workHistoryModel() {
        // Test WorkHistory model creation
        WorkHistory workHistory = new WorkHistory();
        workHistory.setDate(LocalDate.now());
        workHistory.setStartTime(LocalTime.of(9, 0));
        workHistory.setEndTime(LocalTime.of(17, 0));
        workHistory.setContent("Test work content");
        
        // Test basic getters
        assertEquals(LocalDate.now(), workHistory.getDate());
        assertEquals(LocalTime.of(9, 0), workHistory.getStartTime());
        assertEquals(LocalTime.of(17, 0), workHistory.getEndTime());
        assertEquals("Test work content", workHistory.getContent());
    }
}