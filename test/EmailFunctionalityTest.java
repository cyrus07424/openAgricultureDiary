import controllers.routes;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static play.test.Helpers.*;

public class EmailFunctionalityTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .configure("play.evolutions.db.default.enabled", "true")
            .configure("play.evolutions.db.default.autoApply", "true")
            .configure("play.filters.hosts.allowed.0", "localhost:19001")
            .configure("db.default.driver", "org.h2.Driver")
            .configure("db.default.url", "jdbc:h2:mem:test")
            // Disable email sending for tests
            .configure("sendgrid.api.key", "")
            .configure("sendgrid.from.email", "test@example.com")
            .build();
    }

    @Test
    public void showForgotPasswordForm() {
        Result result = route(app, fakeRequest(GET, "/forgot-password").host("localhost:19001"));
        
        assertThat(result.status()).isEqualTo(OK);
        assertThat(contentAsString(result)).contains("パスワードを忘れた場合");
        assertThat(contentAsString(result)).contains("メールアドレス");
    }

    @Test
    public void invalidTokenShowsError() {
        Result result = route(app, fakeRequest(GET, "/reset-password?token=invalid-token").host("localhost:19001"));
        
        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation().get()).contains("/login");
    }
}