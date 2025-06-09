package actions;

import controllers.routes;
import models.User;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.libs.typedmap.TypedKey;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repository.UserRepository;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Action that ensures the user is authenticated
 */
public class AuthenticatedAction extends Action<Authenticated> {

    private final UserRepository userRepository;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    
    public static final TypedKey<User> USER_KEY = TypedKey.create("user");

    @Inject
    public AuthenticatedAction(UserRepository userRepository, 
                              ClassLoaderExecutionContext classLoaderExecutionContext) {
        this.userRepository = userRepository;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
    }

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        Optional<String> userIdOptional = request.session().get("userId");
        
        if (userIdOptional.isEmpty()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.AuthController.showLogin())
                    .flashing("error", "ログインが必要です")
            );
        }

        try {
            Long userId = Long.parseLong(userIdOptional.get());
            return userRepository.findById(userId).thenComposeAsync(userOptional -> {
                if (userOptional.isEmpty()) {
                    return CompletableFuture.completedFuture(
                        Results.redirect(routes.AuthController.showLogin())
                            .removingFromSession(request, "userId")
                            .flashing("error", "セッションが無効です。再度ログインしてください")
                    );
                }
                
                // Add user to request attributes for access in controllers
                Http.Request authenticatedRequest = request.addAttr(USER_KEY, userOptional.get());
                return delegate.call(authenticatedRequest);
            }, classLoaderExecutionContext.current());
        } catch (NumberFormatException e) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.AuthController.showLogin())
                    .removingFromSession(request, "userId")
                    .flashing("error", "無効なセッションです")
            );
        }
    }
}