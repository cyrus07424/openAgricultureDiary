package repositoryies;

import io.ebean.DB;
import io.ebean.PagedList;
import io.ebean.Transaction;
import models.WorkHistory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes work history database operations in a different
 * execution context.
 */
public class WorkHistoryRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public WorkHistoryRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Return a paged list of work history for a specific user
     */
    public CompletionStage<PagedList<WorkHistory>> pageByUser(int page, int pageSize, String sortBy, String order, String filter, Long userId) {
        return supplyAsync(() ->
                DB.find(WorkHistory.class)
                    .fetch("user")
                    .fetch("field")
                    .fetch("crop")
                    .where()
                    .eq("user.id", userId)
                    .ilike("content", "%" + filter + "%")
                    .orderBy(sortBy + " " + order)
                    .setFirstRow(page * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList(), executionContext);
    }

    public CompletionStage<Optional<WorkHistory>> lookup(Long id) {
        return supplyAsync(() -> DB.find(WorkHistory.class).setId(id).findOneOrEmpty(), executionContext);
    }

    /**
     * Lookup a work history by ID and ensure it belongs to the specified user
     */
    public CompletionStage<Optional<WorkHistory>> lookupByUser(Long id, Long userId) {
        return supplyAsync(() -> 
                DB.find(WorkHistory.class)
                    .fetch("field")
                    .fetch("crop")
                    .fetch("user")
                    .where()
                    .eq("id", id)
                    .eq("user.id", userId)
                    .findOneOrEmpty(), executionContext);
    }

    public CompletionStage<Optional<Long>> update(Long id, WorkHistory newWorkHistoryData) {
        return supplyAsync(() -> {
            Transaction txn = DB.beginTransaction();
            Optional<Long> value = Optional.empty();
            try {
                WorkHistory savedWorkHistory = DB.find(WorkHistory.class).setId(id).findOne();
                if (savedWorkHistory != null) {
                    savedWorkHistory.update(newWorkHistoryData);
                    txn.commit();
                    value = Optional.of(id);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    /**
     * Update a work history ensuring it belongs to the specified user
     */
    public CompletionStage<Optional<Long>> updateByUser(Long id, WorkHistory newWorkHistoryData, Long userId) {
        return supplyAsync(() -> {
            Transaction txn = DB.beginTransaction();
            Optional<Long> value = Optional.empty();
            try {
                WorkHistory savedWorkHistory = DB.find(WorkHistory.class)
                        .fetch("user")
                        .where()
                        .eq("id", id)
                        .eq("user.id", userId)
                        .findOne();
                if (savedWorkHistory != null) {
                    savedWorkHistory.update(newWorkHistoryData);
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
            final Optional<WorkHistory> workHistoryOptional = DB.find(WorkHistory.class).setId(id).findOneOrEmpty();
            if (workHistoryOptional.isPresent()) {
                workHistoryOptional.get().delete();
                return Optional.of(id);
            }
            return Optional.empty();
        }, executionContext);
    }

    /**
     * Delete a work history ensuring it belongs to the specified user
     */
    public CompletionStage<Boolean> deleteByUser(Long id, Long userId) {
        return supplyAsync(() -> {
            final Optional<WorkHistory> workHistoryOptional = DB.find(WorkHistory.class)
                    .fetch("user")
                    .where()
                    .eq("id", id)
                    .eq("user.id", userId)
                    .findOneOrEmpty();
            if (workHistoryOptional.isPresent()) {
                workHistoryOptional.get().delete();
                return true;
            }
            return false;
        }, executionContext);
    }

    public CompletionStage<Long> insert(WorkHistory workHistory) {
        return supplyAsync(() -> {
            workHistory.save();
            return workHistory.getId();
        }, executionContext);
    }
}