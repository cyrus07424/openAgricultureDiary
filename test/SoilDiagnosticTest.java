import io.ebean.PagedList;
import models.SoilDiagnostic;
import models.Field;
import models.User;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import repositoryies.SoilDiagnosticRepository;
import repositoryies.FieldRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SoilDiagnosticTest extends WithApplication {

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
        final SoilDiagnosticRepository soilDiagnosticRepository = app.injector().instanceOf(SoilDiagnosticRepository.class);
        
        // Test that the repository can be injected and basic operations work
        CompletionStage<PagedList<SoilDiagnostic>> stage = soilDiagnosticRepository.pageByUser(0, 10, "diagnosticDate", "DESC", "", 999L);

        await().atMost(1, SECONDS).until(() -> {
            try {
                PagedList<SoilDiagnostic> soilDiagnostics = stage.toCompletableFuture().get();
                return soilDiagnostics != null;
            } catch (Exception e) {
                return false;
            }
        });
    }
    
    @Test
    public void paginationEmpty() {
        final SoilDiagnosticRepository soilDiagnosticRepository = app.injector().instanceOf(SoilDiagnosticRepository.class);
        CompletionStage<PagedList<SoilDiagnostic>> stage = soilDiagnosticRepository.pageByUser(0, 10, "diagnosticDate", "DESC", "", 999L);

        // Test that pagination works for non-existent user
        await().atMost(1, SECONDS).until(() -> {
            try {
                PagedList<SoilDiagnostic> soilDiagnostics = stage.toCompletableFuture().get();
                return soilDiagnostics.getTotalCount() == 0 && soilDiagnostics.getList().size() == 0;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @Test
    public void modelBasicFields() {
        // Test that SoilDiagnostic model can be created with basic fields
        SoilDiagnostic soilDiagnostic = new SoilDiagnostic();
        soilDiagnostic.setDiagnosticDate(LocalDate.now());
        soilDiagnostic.setCec(new BigDecimal("15.5"));
        soilDiagnostic.setEc(new BigDecimal("250.0"));
        soilDiagnostic.setPhH2O(new BigDecimal("6.5"));
        soilDiagnostic.setPhKCL(new BigDecimal("6.0"));
        soilDiagnostic.setNh4N(new BigDecimal("12.3"));
        soilDiagnostic.setK2O(new BigDecimal("45.7"));
        soilDiagnostic.setP2O5(new BigDecimal("23.8"));
        soilDiagnostic.setCaO(new BigDecimal("156.2"));
        soilDiagnostic.setMgO(new BigDecimal("34.1"));
        soilDiagnostic.setNo3N(new BigDecimal("8.9"));
        soilDiagnostic.setAvailableNitrogen(new BigDecimal("21.2"));
        soilDiagnostic.setPhosphorusAbsorptionCoefficient(new BigDecimal("2500.0"));
        soilDiagnostic.setHumus(new BigDecimal("3.2"));

        // Test getters
        assertEquals(new BigDecimal("15.5"), soilDiagnostic.getCec());
        assertEquals(new BigDecimal("250.0"), soilDiagnostic.getEc());
        assertEquals(new BigDecimal("6.5"), soilDiagnostic.getPhH2O());
        assertEquals(new BigDecimal("6.0"), soilDiagnostic.getPhKCL());
        assertEquals(new BigDecimal("12.3"), soilDiagnostic.getNh4N());
        assertEquals(new BigDecimal("45.7"), soilDiagnostic.getK2O());
        assertEquals(new BigDecimal("23.8"), soilDiagnostic.getP2O5());
        assertEquals(new BigDecimal("156.2"), soilDiagnostic.getCaO());
        assertEquals(new BigDecimal("34.1"), soilDiagnostic.getMgO());
        assertEquals(new BigDecimal("8.9"), soilDiagnostic.getNo3N());
        assertEquals(new BigDecimal("21.2"), soilDiagnostic.getAvailableNitrogen());
        assertEquals(new BigDecimal("2500.0"), soilDiagnostic.getPhosphorusAbsorptionCoefficient());
        assertEquals(new BigDecimal("3.2"), soilDiagnostic.getHumus());
    }
}