package repositoryies;

import io.ebean.DB;
import io.ebean.Model;
import io.ebean.PagedList;
import io.ebean.Transaction;
import models.SoilDiagnostic;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class SoilDiagnosticRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public SoilDiagnosticRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Return a paged list of soil diagnostics for a specific user
     */
    public CompletionStage<PagedList<SoilDiagnostic>> pageByUser(int page, int pageSize, String sortBy, String order, String filter, Long userId) {
        return supplyAsync(() ->
                DB.find(SoilDiagnostic.class)
                    .fetch("user")
                    .fetch("field")
                    .where()
                    .eq("user.id", userId)
                    .ilike("field.name", "%" + filter + "%")
                    .orderBy(sortBy + " " + order)
                    .setFirstRow(page * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList(), executionContext);
    }

    public CompletionStage<Optional<SoilDiagnostic>> lookup(Long id) {
        return supplyAsync(() -> DB.find(SoilDiagnostic.class).setId(id).findOneOrEmpty(), executionContext);
    }

    /**
     * Lookup a soil diagnostic by ID and ensure it belongs to the specified user
     */
    public CompletionStage<Optional<SoilDiagnostic>> lookupByUser(Long id, Long userId) {
        return supplyAsync(() -> 
                DB.find(SoilDiagnostic.class)
                    .fetch("user")
                    .fetch("field")
                    .where()
                    .eq("id", id)
                    .eq("user.id", userId)
                    .findOneOrEmpty(), executionContext);
    }

    public CompletionStage<Optional<Long>> update(Long id, SoilDiagnostic newSoilDiagnosticData) {
        return supplyAsync(() -> {
            Transaction txn = DB.beginTransaction();
            Optional<Long> value = Optional.empty();
            try {
                SoilDiagnostic savedSoilDiagnostic = DB.find(SoilDiagnostic.class).setId(id).findOne();
                if (savedSoilDiagnostic != null) {
                    savedSoilDiagnostic.update(newSoilDiagnosticData);
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
     * Update a soil diagnostic ensuring it belongs to the specified user
     */
    public CompletionStage<Optional<Long>> updateByUser(Long id, SoilDiagnostic newSoilDiagnosticData, Long userId) {
        return supplyAsync(() -> {
            Transaction txn = DB.beginTransaction();
            Optional<Long> value = Optional.empty();
            try {
                SoilDiagnostic savedSoilDiagnostic = DB.find(SoilDiagnostic.class)
                        .fetch("user")
                        .fetch("field")
                        .where()
                        .eq("id", id)
                        .eq("user.id", userId)
                        .findOne();
                if (savedSoilDiagnostic != null) {
                    savedSoilDiagnostic.update(newSoilDiagnosticData);
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
                Optional<SoilDiagnostic> soilDiagnosticOptional = DB.find(SoilDiagnostic.class).setId(id).findOneOrEmpty();
                soilDiagnosticOptional.ifPresent(Model::delete);
                return soilDiagnosticOptional.map(sd -> sd.getId());
            } catch (Exception e) {
                return Optional.empty();
            }
        }, executionContext);
    }

    /**
     * Delete a soil diagnostic ensuring it belongs to the specified user
     */
    public CompletionStage<Boolean> deleteByUser(Long id, Long userId) {
        return supplyAsync(() -> {
            try {
                Optional<SoilDiagnostic> soilDiagnosticOptional = DB.find(SoilDiagnostic.class)
                        .fetch("user")
                        .fetch("field")
                        .where()
                        .eq("id", id)
                        .eq("user.id", userId)
                        .findOneOrEmpty();
                soilDiagnosticOptional.ifPresent(Model::delete);
                return soilDiagnosticOptional.isPresent();
            } catch (Exception e) {
                return false;
            }
        }, executionContext);
    }

    public CompletionStage<Long> insert(SoilDiagnostic soilDiagnostic) {
        return supplyAsync(() -> {
             soilDiagnostic.setId(System.currentTimeMillis()); // not ideal, but it works
             DB.insert(soilDiagnostic);
             return soilDiagnostic.getId();
        }, executionContext);
    }
}