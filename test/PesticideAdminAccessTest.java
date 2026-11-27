import controllers.routes;
import io.ebean.DB;
import models.PesticideRegistration;
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

public class PesticideAdminAccessTest extends WithApplication {

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
    public void redirectToLoginWhenListingWithoutAuthentication() {
        Result result = route(app, routes.PesticideController.list(0, "registrationNumber", "asc", ""));

        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation()).hasValue("/login");
    }

    @Test
    public void forbidNonAdminFromListing() {
        User user = createUser(false);
        Result result = route(app, fakeRequest(GET, routes.PesticideController.list(0, "registrationNumber", "asc", "").url())
            .header(HOST, "localhost:19001")
            .header("Accept-Language", "ja")
            .session("userId", String.valueOf(user.getId())));

        assertThat(result.status()).isEqualTo(FORBIDDEN);
        assertThat(contentAsString(result)).contains("管理者権限が必要です");
    }

    @Test
    public void forbidNonAdminFromViewingUpload() {
        User user = createUser(false);
        Result result = route(app, fakeRequest(GET, routes.PesticideController.showUpload().url())
            .header(HOST, "localhost:19001")
            .session("userId", String.valueOf(user.getId())));

        assertThat(result.status()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void forbidNonAdminFromUploading() {
        User user = createUser(false);
        int countBefore = DB.find(PesticideRegistration.class).findCount();
        Result result = route(app, addCSRFToken(fakeRequest(POST, routes.PesticideController.upload().url())
            .header(HOST, "localhost:19001")
            .session("userId", String.valueOf(user.getId()))
            .bodyForm(Map.of())));

        assertThat(result.status()).isEqualTo(FORBIDDEN);
        assertThat(DB.find(PesticideRegistration.class).findCount()).isEqualTo(countBefore);
    }

    @Test
    public void forbidNonAdminFromClearing() {
        User user = createUser(false);
        PesticideRegistration pesticide = createPesticide("non-admin-clear");
        int countBefore = DB.find(PesticideRegistration.class).findCount();
        Result result = route(app, addCSRFToken(fakeRequest(POST, routes.PesticideController.clear().url())
            .header(HOST, "localhost:19001")
            .session("userId", String.valueOf(user.getId()))
            .bodyForm(Map.of())));

        assertThat(result.status()).isEqualTo(FORBIDDEN);
        assertThat(DB.find(PesticideRegistration.class).findCount()).isEqualTo(countBefore);
        assertThat(DB.find(PesticideRegistration.class).where().eq("id", pesticide.getId()).findOne()).isNotNull();
    }

    @Test
    public void allowAdminToList() {
        User user = createUser(true);
        Result result = route(app, fakeRequest(GET, routes.PesticideController.list(0, "registrationNumber", "asc", "").url())
            .header(HOST, "localhost:19001")
            .session("userId", String.valueOf(user.getId())));

        assertThat(result.status()).isEqualTo(OK);
    }

    @Test
    public void allowAdminToViewUpload() {
        User user = createUser(true);
        Result result = route(app, fakeRequest(GET, routes.PesticideController.showUpload().url())
            .header(HOST, "localhost:19001")
            .session("userId", String.valueOf(user.getId())));

        assertThat(result.status()).isEqualTo(OK);
    }

    @Test
    public void allowAdminToClear() {
        User user = createUser(true);
        createPesticide("admin-clear-1");
        createPesticide("admin-clear-2");
        assertThat(DB.find(PesticideRegistration.class).findCount()).isGreaterThanOrEqualTo(2);
        Result result = route(app, addCSRFToken(fakeRequest(POST, routes.PesticideController.clear().url())
            .header(HOST, "localhost:19001")
            .session("userId", String.valueOf(user.getId()))
            .bodyForm(Map.of())));

        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation()).hasValue(
            routes.PesticideController.list(0, "registrationNumber", "asc", "").url());
        assertThat(DB.find(PesticideRegistration.class).findCount()).isZero();
    }

    private User createUser(boolean isAdmin) {
        User user = new User("farmer", "farmer@example.test", "Preview-only-password-42!");
        user.setIsAdmin(isAdmin);
        user.save();
        return user;
    }

    private PesticideRegistration createPesticide(String registrationNumber) {
        PesticideRegistration pesticide = new PesticideRegistration();
        pesticide.setRegistrationNumber(registrationNumber);
        pesticide.save();
        return pesticide;
    }
}
