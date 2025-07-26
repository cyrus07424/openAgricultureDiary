package controllers;

import actions.GlobalConfig;
import actions.GlobalConfigAction;
import forms.ForgotPasswordForm;
import forms.LoginForm;
import forms.RegisterForm;
import forms.ResetPasswordForm;
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
import services.EmailService;
import services.SlackNotificationService;
import utils.GlobalConfigHelper;
import utils.LegalLinksConfiguration;
import views.html.auth.forgotPassword;
import views.html.auth.login;
import views.html.auth.register;
import views.html.auth.resetPassword;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller handles authentication related actions
 */
@GlobalConfig
public class AuthController extends Controller {

    private final UserRepository userRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;
    private final EmailService emailService;
    private final SlackNotificationService slackNotificationService;

    @Inject
    public AuthController(UserRepository userRepository,
                         FormFactory formFactory,
                         ClassLoaderExecutionContext classLoaderExecutionContext,
                         MessagesApi messagesApi,
                         EmailService emailService,
                         SlackNotificationService slackNotificationService) {
        this.userRepository = userRepository;
        this.formFactory = formFactory;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
        this.emailService = emailService;
        this.slackNotificationService = slackNotificationService;
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
                    // Send Slack notification for successful login
                    slackNotificationService.notifyUserLogin(user, request)
                        .exceptionally(throwable -> {
                            play.Logger.of(AuthController.class).warn("Failed to send Slack login notification for user: " + user.getUsername(), throwable);
                            return false;
                        });
                    
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
        
        // Check if Terms of Service agreement is required and provided
        LegalLinksConfiguration legalLinks = request.attrs().get(GlobalConfigAction.LEGAL_LINKS_KEY);
        if (legalLinks.hasTermsOfServiceUrl() && !data.isAgreeToTerms()) {
            return CompletableFuture.completedFuture(
                badRequest(register.render(
                    registerForm.withError("agreeToTerms", "利用規約に同意してください"),
                    request, 
                    messagesApi.preferred(request)
                ))
            );
        }
        
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
                return userRepository.insert(user).thenComposeAsync(userId -> {
                    // Update user ID
                    user.setId(userId);
                    
                    // Send welcome email asynchronously
                    emailService.sendWelcomeEmail(data.getEmail(), data.getUsername())
                        .exceptionally(throwable -> {
                            // Log error but don't fail registration
                            play.Logger.of(AuthController.class).warn("Failed to send welcome email to: " + data.getEmail(), throwable);
                            return false;
                        });
                    
                    // Send Slack notification for user registration
                    slackNotificationService.notifyUserRegistration(user, request)
                        .exceptionally(throwable -> {
                            play.Logger.of(AuthController.class).warn("Failed to send Slack registration notification for user: " + data.getUsername(), throwable);
                            return false;
                        });
                    
                    return CompletableFuture.completedFuture(
                        Results.redirect(routes.CropController.list(0, "name", "asc", ""))
                                .addingToSession(request, "userId", userId.toString())
                                .flashing("success", "アカウントが作成されました")
                    );
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

    /**
     * Display the forgot password form
     */
    public Result showForgotPassword(Http.Request request) {
        Form<ForgotPasswordForm> forgotPasswordForm = formFactory.form(ForgotPasswordForm.class);
        return ok(forgotPassword.render(forgotPasswordForm, request, messagesApi.preferred(request)));
    }

    /**
     * Handle forgot password form submission
     */
    public CompletionStage<Result> forgotPassword(Http.Request request) {
        Form<ForgotPasswordForm> forgotPasswordForm = formFactory.form(ForgotPasswordForm.class).bindFromRequest(request);
        
        if (forgotPasswordForm.hasErrors()) {
            return CompletableFuture.completedFuture(
                badRequest(forgotPassword.render(forgotPasswordForm, request, messagesApi.preferred(request)))
            );
        }

        ForgotPasswordForm data = forgotPasswordForm.get();
        
        return userRepository.findByEmail(data.getEmail()).thenComposeAsync(userOptional -> {
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.generateResetToken();
                
                return userRepository.update(user).thenComposeAsync(v -> {
                    // Send password reset email
                    String baseUrl = getBaseUrl(request);
                    return emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), user.getResetToken(), baseUrl)
                        .thenApplyAsync(emailSent -> {
                            if (emailSent) {
                                return Results.redirect(routes.AuthController.showLogin())
                                        .flashing("success", "パスワードリセットのメールを送信しました。メールをご確認ください。");
                            } else {
                                return badRequest(forgotPassword.render(
                                    forgotPasswordForm.withError("email", "メール送信に失敗しました。しばらく時間をおいて再度お試しください。"),
                                    request, 
                                    messagesApi.preferred(request)
                                ));
                            }
                        }, classLoaderExecutionContext.current());
                }, classLoaderExecutionContext.current());
            } else {
                // Don't reveal that email doesn't exist
                return CompletableFuture.completedFuture(
                    Results.redirect(routes.AuthController.showLogin())
                            .flashing("success", "該当するメールアドレスが存在する場合、パスワードリセットのメールを送信しました。")
                );
            }
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display the reset password form
     */
    public CompletionStage<Result> showResetPassword(Http.Request request, String token) {
        if (token == null || token.isEmpty()) {
            return CompletableFuture.completedFuture(
                Results.redirect(routes.AuthController.showLogin())
                        .flashing("error", "無効なリセットトークンです。")
            );
        }
        
        return userRepository.findByResetToken(token).thenApplyAsync(userOptional -> {
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.isResetTokenValid(token)) {
                    Form<ResetPasswordForm> resetForm = formFactory.form(ResetPasswordForm.class);
                    ResetPasswordForm formData = new ResetPasswordForm();
                    formData.setToken(token);
                    resetForm = resetForm.fill(formData);
                    
                    return ok(resetPassword.render(resetForm, request, messagesApi.preferred(request)));
                }
            }
            
            return Results.redirect(routes.AuthController.showLogin())
                    .flashing("error", "無効または期限切れのリセットトークンです。");
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle reset password form submission
     */
    public CompletionStage<Result> resetPassword(Http.Request request) {
        Form<ResetPasswordForm> resetForm = formFactory.form(ResetPasswordForm.class).bindFromRequest(request);
        
        if (resetForm.hasErrors()) {
            return CompletableFuture.completedFuture(
                badRequest(resetPassword.render(resetForm, request, messagesApi.preferred(request)))
            );
        }

        ResetPasswordForm data = resetForm.get();
        
        // Check if passwords match
        if (!data.passwordsMatch()) {
            return CompletableFuture.completedFuture(
                badRequest(resetPassword.render(
                    resetForm.withError("confirmPassword", "パスワードが一致しません"),
                    request, 
                    messagesApi.preferred(request)
                ))
            );
        }
        
        return userRepository.findByResetToken(data.getToken()).thenComposeAsync(userOptional -> {
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.isResetTokenValid(data.getToken())) {
                    // Update password and clear reset token
                    user.setPassword(data.getPassword());
                    user.clearResetToken();
                    
                    return userRepository.update(user).thenApplyAsync(v -> {
                        return Results.redirect(routes.AuthController.showLogin())
                                .flashing("success", "パスワードが正常に変更されました。新しいパスワードでログインしてください。");
                    }, classLoaderExecutionContext.current());
                }
            }
            
            return CompletableFuture.completedFuture(
                Results.redirect(routes.AuthController.showLogin())
                        .flashing("error", "無効または期限切れのリセットトークンです。")
            );
        }, classLoaderExecutionContext.current());
    }

    /**
     * Get base URL from request
     */
    private String getBaseUrl(Http.Request request) {
        String protocol = request.secure() ? "https" : "http";
        return protocol + "://" + request.host();
    }
}