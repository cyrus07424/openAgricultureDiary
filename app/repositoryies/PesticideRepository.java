package repositoryies;

import io.ebean.DB;
import io.ebean.PagedList;
import models.PesticideRegistration;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes pesticide registration database operations in a different
 * execution context.
 */
public class PesticideRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public PesticideRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public CompletionStage<Optional<PesticideRegistration>> findById(Long id) {
        return supplyAsync(() -> DB.find(PesticideRegistration.class).setId(id).findOneOrEmpty(), executionContext);
    }

    public CompletionStage<PagedList<PesticideRegistration>> page(int page, int pageSize, String sortBy, String order, String filter) {
        return supplyAsync(() -> {
            var query = DB.find(PesticideRegistration.class);
            
            if (filter != null && !filter.isEmpty()) {
                query = query.where()
                    .or()
                        .icontains("registrationNumber", filter)
                        .icontains("pesticideName", filter)
                        .icontains("cropName", filter)
                    .endOr()
                    .query();
            }
            
            if (sortBy != null && !sortBy.isEmpty()) {
                if ("desc".equals(order)) {
                    query = query.orderBy(sortBy + " desc");
                } else {
                    query = query.orderBy(sortBy + " asc");
                }
            }
            
            return query.setFirstRow(page * pageSize).setMaxRows(pageSize).findPagedList();
        }, executionContext);
    }

    public CompletionStage<Long> insert(PesticideRegistration pesticide) {
        return supplyAsync(() -> {
            pesticide.save();
            return pesticide.getId();
        }, executionContext);
    }

    public CompletionStage<Void> update(PesticideRegistration pesticide) {
        return supplyAsync(() -> {
            pesticide.update();
            return null;
        }, executionContext);
    }

    public CompletionStage<Void> delete(Long id) {
        return supplyAsync(() -> {
            DB.find(PesticideRegistration.class).setId(id).delete();
            return null;
        }, executionContext);
    }

    public CompletionStage<Void> insertAll(List<PesticideRegistration> pesticides) {
        return supplyAsync(() -> {
            DB.saveAll(pesticides);
            return null;
        }, executionContext);
    }

    public CompletionStage<Void> deleteAll() {
        return supplyAsync(() -> {
            DB.find(PesticideRegistration.class).delete();
            return null;
        }, executionContext);
    }
}