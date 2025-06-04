package repository;

import io.ebean.DB;
import io.ebean.Model;
import io.ebean.PagedList;
import io.ebean.Transaction;
import models.Crop;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class CropRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public CropRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Return a paged list of crop
     *
     * @param page     Page to display
     * @param pageSize Number of crops per page
     * @param sortBy   Crop property used for sorting
     * @param order    Sort order (either or asc or desc)
     * @param filter   Filter applied on the name column
     */
    public CompletionStage<PagedList<Crop>> page(int page, int pageSize, String sortBy, String order, String filter) {
        return supplyAsync(() ->
                DB.find(Crop.class)
                    .fetch("company").where()
                    .ilike("name", "%" + filter + "%")
                    .orderBy(sortBy + " " + order)
                    .setFirstRow(page * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList(), executionContext);
    }

    public CompletionStage<Optional<Crop>> lookup(Long id) {
        return supplyAsync(() -> DB.find(Crop.class).setId(id).findOneOrEmpty(), executionContext);
    }

    public CompletionStage<Optional<Long>> update(Long id, Crop newCropData) {
        return supplyAsync(() -> {
            Transaction txn = DB.beginTransaction();
            Optional<Long> value = Optional.empty();
            try {
                Crop savedCrop = DB.find(Crop.class).setId(id).findOne();
                if (savedCrop != null) {
                    savedCrop.update(newCropData);
                    txn.commit();
                    value = Optional.of(id);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<Optional<Long>> delete(Long id) {
        return supplyAsync(() -> {
            try {
                Optional<Crop> cropOptional = DB.find(Crop.class).setId(id).findOneOrEmpty();
                cropOptional.ifPresent(Model::delete);
                return cropOptional.map(c -> c.getId());
            } catch (Exception e) {
                return Optional.empty();
            }
        }, executionContext);
    }

    public CompletionStage<Long> insert(Crop crop) {
        return supplyAsync(() -> {
             crop.setId(System.currentTimeMillis()); // not ideal, but it works
             DB.insert(crop);
             return crop.getId();
        }, executionContext);
    }
}
