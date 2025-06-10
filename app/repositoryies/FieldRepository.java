package repositoryies;

import io.ebean.DB;
import io.ebean.Model;
import io.ebean.PagedList;
import io.ebean.Transaction;
import models.Field;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class FieldRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public FieldRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Return a paged list of fields for a specific user
     */
    public CompletionStage<PagedList<Field>> pageByUser(int page, int pageSize, String sortBy, String order, String filter, Long userId) {
        return supplyAsync(() ->
                DB.find(Field.class)
                    .fetch("user")
                    .where()
                    .eq("user.id", userId)
                    .ilike("name", "%" + filter + "%")
                    .orderBy(sortBy + " " + order)
                    .setFirstRow(page * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList(), executionContext);
    }

    public CompletionStage<Optional<Field>> lookup(Long id) {
        return supplyAsync(() -> DB.find(Field.class).setId(id).findOneOrEmpty(), executionContext);
    }

    /**
     * Lookup a field by ID and ensure it belongs to the specified user
     */
    public CompletionStage<Optional<Field>> lookupByUser(Long id, Long userId) {
        return supplyAsync(() -> 
                DB.find(Field.class)
                    .fetch("user")
                    .where()
                    .eq("id", id)
                    .eq("user.id", userId)
                    .findOneOrEmpty(), executionContext);
    }

    public CompletionStage<Optional<Long>> update(Long id, Field newFieldData) {
        return supplyAsync(() -> {
            Transaction txn = DB.beginTransaction();
            Optional<Long> value = Optional.empty();
            try {
                Field savedField = DB.find(Field.class).setId(id).findOne();
                if (savedField != null) {
                    savedField.update(newFieldData);
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
     * Update a field ensuring it belongs to the specified user
     */
    public CompletionStage<Optional<Long>> updateByUser(Long id, Field newFieldData, Long userId) {
        return supplyAsync(() -> {
            Transaction txn = DB.beginTransaction();
            Optional<Long> value = Optional.empty();
            try {
                Field savedField = DB.find(Field.class)
                        .fetch("user")
                        .where()
                        .eq("id", id)
                        .eq("user.id", userId)
                        .findOne();
                if (savedField != null) {
                    savedField.update(newFieldData);
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
                Optional<Field> fieldOptional = DB.find(Field.class).setId(id).findOneOrEmpty();
                fieldOptional.ifPresent(Model::delete);
                return fieldOptional.map(f -> f.getId());
            } catch (Exception e) {
                return Optional.empty();
            }
        }, executionContext);
    }

    /**
     * Delete a field ensuring it belongs to the specified user
     */
    public CompletionStage<Boolean> deleteByUser(Long id, Long userId) {
        return supplyAsync(() -> {
            try {
                Optional<Field> fieldOptional = DB.find(Field.class)
                        .fetch("user")
                        .where()
                        .eq("id", id)
                        .eq("user.id", userId)
                        .findOneOrEmpty();
                fieldOptional.ifPresent(Model::delete);
                return fieldOptional.isPresent();
            } catch (Exception e) {
                return false;
            }
        }, executionContext);
    }

    public CompletionStage<Long> insert(Field field) {
        return supplyAsync(() -> {
             field.setId(System.currentTimeMillis()); // not ideal, but it works
             DB.insert(field);
             return field.getId();
        }, executionContext);
    }
}