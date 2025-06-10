package repositoryies;

import io.ebean.DB;
import models.User;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes user database operations in a different
 * execution context.
 */
public class UserRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public UserRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public CompletionStage<Optional<User>> findByUsername(String username) {
        return supplyAsync(() -> DB.find(User.class).where().eq("username", username).findOneOrEmpty(), executionContext);
    }

    public CompletionStage<Optional<User>> findByEmail(String email) {
        return supplyAsync(() -> DB.find(User.class).where().eq("email", email).findOneOrEmpty(), executionContext);
    }

    public CompletionStage<Optional<User>> findById(Long id) {
        return supplyAsync(() -> DB.find(User.class).setId(id).findOneOrEmpty(), executionContext);
    }

    public CompletionStage<Long> insert(User user) {
        return supplyAsync(() -> {
            user.save();
            return user.getId();
        }, executionContext);
    }

    public CompletionStage<Boolean> existsByUsername(String username) {
        return supplyAsync(() -> DB.find(User.class).where().eq("username", username).exists(), executionContext);
    }

    public CompletionStage<Boolean> existsByEmail(String email) {
        return supplyAsync(() -> DB.find(User.class).where().eq("email", email).exists(), executionContext);
    }
}