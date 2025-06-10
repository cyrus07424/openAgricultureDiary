package controllers;

import forms.LoginForm;
import forms.RegisterForm;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.UserRepository;
import views.html.auth.login;
import views.html.auth.register;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller handles authentication related actions
 */
public class AuthController extends Controller {

    private final UserRepository userRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;

    @Inject
    public AuthController(UserRepository userRepository,
                         FormFactory formFactory,
                         ClassLoaderExecutionContext classLoaderExecutionContext,
                         MessagesApi messagesApi) {
        this.userRepository = userRepository;
        this.formFactory = formFactory;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
    }

    /**
     * Display the login form
     */
    public Result showLogin(Http.Request request) {
        // If user is already logged in, redirect to crops
        if (request.session().get("userId").isPresent()) {
            return Results.redirect(routes.CropController.list(0, "name", "asc", ""));
        }
        
        Form<LoginForm> loginForm = formFactory.form(LoginForm.class);
        return ok(login.render(loginForm, request, messagesApi.preferred(request)));
    }

    /**
     * Handle login form submission
     */
    public CompletionStage<Result> login(Http.Request request) {
        Form<LoginForm> loginForm = formFactory.form(LoginForm.class).bindFromRequest(request);
        
        if (loginForm.hasErrors()) {
            return CompletableFuture.completedFuture(
                badRequest(login.render(loginForm, request, messagesApi.preferred(request)))
            );
        }

        LoginForm data = loginForm.get();
        
        return userRepository.findByUsername(data.getUsername()).thenApplyAsync(userOptional -> {
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.checkPassword(data.getPassword())) {
                    return Results.redirect(routes.CropController.list(0, "name", "asc", ""))
                            .addingToSession(request, "userId", user.getId().toString())
                            .flashing("success", "ログインしました");
                }
            }
            
            return badRequest(login.render(
                loginForm.withError("username", "ユーザー名またはパスワードが間違っています"),
                request, 
                messagesApi.preferred(request)
            ));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display the registration form
     */
    public Result showRegister(Http.Request request) {
        // If user is already logged in, redirect to crops
        if (request.session().get("userId").isPresent()) {
            return Results.redirect(routes.CropController.list(0, "name", "asc", ""));
        }
        
        Form<RegisterForm> registerForm = formFactory.form(RegisterForm.class);
        return ok(register.render(registerForm, request, messagesApi.preferred(request)));
    }

    /**
     * Handle registration form submission
     */
    public CompletionStage<Result> register(Http.Request request) {
        Form<RegisterForm> registerForm = formFactory.form(RegisterForm.class).bindFromRequest(request);
        
        if (registerForm.hasErrors()) {
            return CompletableFuture.completedFuture(
                badRequest(register.render(registerForm, request, messagesApi.preferred(request)))
            );
        }

        RegisterForm data = registerForm.get();
        
        // Check if passwords match
        if (!data.passwordsMatch()) {
            return CompletableFuture.completedFuture(
                badRequest(register.render(
                    registerForm.withError("confirmPassword", "パスワードが一致しません"),
                    request, 
                    messagesApi.preferred(request)
                ))
            );
        }

        // Check if username or email already exists
        return userRepository.existsByUsername(data.getUsername()).thenComposeAsync(usernameExists -> {
            if (usernameExists) {
                return CompletableFuture.completedFuture(
                    badRequest(register.render(
                        registerForm.withError("username", "このユーザー名は既に使用されています"),
                        request, 
                        messagesApi.preferred(request)
                    ))
                );
            }
            
            return userRepository.existsByEmail(data.getEmail()).thenComposeAsync(emailExists -> {
                if (emailExists) {
                    return CompletableFuture.completedFuture(
                        badRequest(register.render(
                            registerForm.withError("email", "このメールアドレスは既に使用されています"),
                            request, 
                            messagesApi.preferred(request)
                        ))
                    );
                }
                
                // Create new user
                User user = new User(data.getUsername(), data.getEmail(), data.getPassword());
                return userRepository.insert(user).thenApplyAsync(userId -> {
                    return Results.redirect(routes.CropController.list(0, "name", "asc", ""))
                            .addingToSession(request, "userId", userId.toString())
                            .flashing("success", "アカウントが作成されました");
                }, classLoaderExecutionContext.current());
            }, classLoaderExecutionContext.current());
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle logout
     */
    public Result logout(Http.Request request) {
        return Results.redirect(routes.AuthController.showLogin())
                .removingFromSession(request, "userId")
                .flashing("success", "ログアウトしました");
    }
}