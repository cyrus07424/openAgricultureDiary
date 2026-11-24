import controllers.routes;
import models.User;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.test.Helpers.*;

public class AuthenticationTest extends WithApplication {

    private static final String PASSWORD = "Preview-only-password-42!";

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .configure("play.evolutions.db.default.enabled", "true")
            .configure("play.evolutions.db.default.autoApply", "true")
            .configure("play.filters.hosts.allowed", List.of("localhost", "localhost:19001"))
            .configure("db.default.driver", "org.h2.Driver")
            .configure("db.default.url", "jdbc:h2:mem:test")
            .build();
    }

    @Test
    public void redirectToLoginWhenAccessingCropsWithoutAuth() {
        Result result = route(app, controllers.routes.CropController.list(0, "name", "asc", ""));
        
        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation().get()).contains("/login");
    }

    @Test
    public void showLoginForm() {
        Result result = route(app, fakeRequest(GET, routes.AuthController.showLogin().url())
            .header("Accept-Language", "ja"));
        
        assertThat(result.status()).isEqualTo(OK);
        assertThat(contentAsString(result)).contains("ログイン");
        assertThat(contentAsString(result)).contains("type=\"email\"", "name=\"email\"", "fa-envelope");
        assertThat(contentAsString(result)).doesNotContain("name=\"username\"");
    }

    @Test
    public void showRegisterForm() {
        Result result = route(app, controllers.routes.AuthController.showRegister());
        
        assertThat(result.status()).isEqualTo(OK);
        assertThat(contentAsString(result)).contains("ユーザー登録");
    }
    
    @Test
    public void redirectToLoginFromHome() {
        Result result = route(app, controllers.routes.HomeController.index());
        
        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation().get()).contains("/login");
    }

    @Test
    public void loginWithRegisteredEmailAndCorrectPassword() {
        registerUser();

        Result result = loginWith(Map.of("email", "  FARMER@EXAMPLE.TEST  ", "password", PASSWORD));

        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation()).hasValue("/dashboard");
        User user = io.ebean.DB.find(User.class).where().eq("email", "farmer@example.test").findOne();
        assertThat(user).isNotNull();
        assertThat(result.session().get("userId")).hasValue(user.getId().toString());
    }

    @Test
    public void rejectWrongPassword() {
        registerUser();

        Result result = loginWith(Map.of("email", "farmer@example.test", "password", "wrong-password"));

        assertThat(result.status()).isEqualTo(BAD_REQUEST);
        assertThat(result.session()).isNull();
        assertThat(contentAsString(result)).contains(
            "メールアドレスまたはパスワードが間違っています",
            "type=\"email\" class=\"form-control is-invalid\"");
    }

    @Test
    public void rejectUsernameLoginAndInvalidEmail() {
        registerUser();
        User user = new User("alias@example.test", "different@example.test", PASSWORD);
        user.save();

        for (Map<String, String> data : List.of(
                Map.of("username", "farmer", "password", PASSWORD),
                Map.of("email", "farmer", "password", PASSWORD),
                Map.of("email", user.getUsername(), "password", PASSWORD),
                Map.of("email", "", "password", PASSWORD))) {
            Result result = loginWith(data);
            assertThat(result.status()).isEqualTo(BAD_REQUEST);
            assertThat(result.session()).isNull();
        }
    }

    private void registerUser() {
        Result result = route(app, addCSRFToken(fakeRequest(POST, routes.AuthController.register().url())
            .header(HOST, "localhost:19001")
            .bodyForm(Map.of(
                "username", "farmer",
                "email", "Farmer@Example.Test",
                "password", PASSWORD,
                "confirmPassword", PASSWORD,
                "agreeToTerms", "true"))));
        assertThat(result.status()).isEqualTo(SEE_OTHER);
    }

    private Result loginWith(Map<String, String> data) {
        return route(app, addCSRFToken(fakeRequest(POST, routes.AuthController.login().url())
            .header(HOST, "localhost:19001")
            .bodyForm(data)));
    }
}