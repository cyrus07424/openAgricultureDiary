package actions;

import controllers.routes;
import models.User;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.UserRepository;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Action that ensures the user is authenticated and is an admin
 */
public class AdminAction extends Action<Admin> {

    private final UserRepository userRepository;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;

    @Inject
    public AdminAction(UserRepository userRepository, 
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
                
                User user = userOptional.get();
                if (!user.isAdmin()) {
                    return CompletableFuture.completedFuture(
                        Results.forbidden("管理者権限が必要です")
                    );
                }
                
                // Add user to request attributes for access in controllers
                Http.Request authenticatedRequest = request.addAttr(AuthenticatedAction.USER_KEY, user);
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