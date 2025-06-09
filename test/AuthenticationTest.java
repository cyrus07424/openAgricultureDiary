import controllers.routes;
import org.junit.Test;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.test.Helpers.*;

public class AuthenticationTest extends WithApplication {

    @Test
    public void redirectToLoginWhenAccessingCropsWithoutAuth() {
        Result result = route(app, controllers.routes.CropController.list(0, "name", "asc", ""));
        
        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation().get()).contains("/login");
    }

    @Test
    public void showLoginForm() {
        Result result = route(app, controllers.routes.AuthController.showLogin());
        
        assertThat(result.status()).isEqualTo(OK);
        assertThat(contentAsString(result)).contains("ログイン");
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
}