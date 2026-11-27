package actions;

import controllers.routes;
import models.User;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.UserRepository;
import utils.GoogleTagManager;
import utils.LegalLinksConfiguration;

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
    private final MessagesApi messagesApi;
    private final GoogleTagManager gtm;
    private final LegalLinksConfiguration legalLinks;

    @Inject
    public AdminAction(UserRepository userRepository, 
                       ClassLoaderExecutionContext classLoaderExecutionContext,
                       MessagesApi messagesApi,
                       GoogleTagManager gtm,
                       LegalLinksConfiguration legalLinks) {
        this.userRepository = userRepository;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
        this.gtm = gtm;
        this.legalLinks = legalLinks;
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
                    Messages messages = messagesApi.preferred(request);
                    Http.Request enhancedRequest = request
                        .addAttr(GlobalConfigAction.GTM_KEY, gtm != null ? gtm : new GoogleTagManager(null))
                        .addAttr(GlobalConfigAction.LEGAL_LINKS_KEY, legalLinks != null ? legalLinks : new LegalLinksConfiguration(null));
                    return CompletableFuture.completedFuture(
                        Results.forbidden(views.html.errors.forbidden.render(enhancedRequest, messages))
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