import models.Crop;
import models.Field;
import models.User;
import models.WorkHistory;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.test.Helpers.*;

public class DashboardMapTest extends WithApplication {
    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .configure("play.evolutions.db.default.enabled", true)
            .configure("play.evolutions.db.default.autoApply", true)
            .configure("play.filters.hosts.allowed", List.of("localhost:19001"))
            .configure("db.default.driver", "org.h2.Driver")
            .configure("db.default.url", "jdbc:h2:mem:dashboard")
            .build();
    }

    private User user(String name) {
        User user = new User(name, name + "@example.test", "Preview-only-password-42!");
        user.save();
        return user;
    }

    private Field field(User user, String name) {
        Field field = new Field();
        field.setUser(user);
        field.setName(name);
        field.setNorthEastLatitude(36.1);
        field.setNorthEastLongitude(138.1);
        field.setSouthWestLatitude(36.0);
        field.setSouthWestLongitude(138.0);
        field.save();
        return field;
    }

    private Http.RequestBuilder get(String url, User user) {
        return fakeRequest(GET, url).header(HOST, "localhost:19001")
            .header("Accept-Language", "ja").session("userId", user.getId().toString());
    }

    @Test
    public void protectsDashboardAndPreservesLoginRedirects() {
        for (String url : List.of("/", "/dashboard", "/crops")) {
            Result result = route(app, fakeRequest(GET, url).header(HOST, "localhost:19001"));
            assertThat(result.status()).isEqualTo(SEE_OTHER);
            assertThat(result.redirectLocation()).hasValue("/login");
        }
        for (String url : List.of("/login", "/register")) {
            assertThat(route(app, fakeRequest(GET, url).header(HOST, "localhost:19001")).status()).isEqualTo(OK);
        }
    }

    @Test
    public void dashboardShowsAllOwnedFieldsButNeverAnotherUsersFields() {
        User owner = user("owner");
        for (int i = 0; i < 12; i++) field(owner, "Owned field " + i);
        field(user("other"), "Private other field");
        field(owner, "<script>alert(1)</script>");

        Result result = route(app, get("/dashboard", owner));
        assertThat(result.status()).isEqualTo(OK);
        String html = contentAsString(result);
        assertThat(html).contains("data-farm-map", "data-name=\"Owned field 11\"", "maplibre-gl@5.6.1");
        assertThat(html).doesNotContain("Private other field", "<script>alert(1)</script>");
        assertThat(html).contains("&lt;script&gt;alert(1)&lt;/script&gt;");
        assertThat(route(app, get("/", owner)).redirectLocation()).hasValue("/dashboard");
        assertThat(route(app, get("/login", owner)).redirectLocation()).hasValue("/dashboard");
        assertThat(route(app, get("/register", owner)).redirectLocation()).hasValue("/dashboard");
        Result login = route(app, addCSRFToken(fakeRequest(POST, "/login")
            .header(HOST, "localhost:19001")
            .bodyForm(Map.of("username", "owner", "password", "Preview-only-password-42!"))));
        assertThat(login.redirectLocation()).hasValue("/dashboard");

        String filtered = contentAsString(route(app, get("/fields?f=Owned%20field%2011", owner)));
        assertThat(filtered).contains("data-name=\"Owned field 11\"");
        assertThat(filtered).doesNotContain("data-name=\"Owned field 0\"");
    }

    @Test
    public void workMapUsesTheFilteredRecordsField() {
        User owner = user("worker");
        Field field = field(owner, "Mapped work field");
        Crop crop = new Crop();
        crop.setName("Carrot");
        crop.setUser(owner);
        crop.save();
        WorkHistory work = new WorkHistory();
        work.setUser(owner);
        work.setField(field);
        work.setCrop(crop);
        work.setDate(LocalDate.now());
        work.setStartTime(LocalTime.of(9, 0));
        work.setEndTime(LocalTime.of(10, 0));
        work.setContent("Weeding");
        work.save();

        String html = contentAsString(route(app, get("/work-history?f=Weeding", owner)));
        assertThat(html).contains("data-name=\"Mapped work field\"", "Weeding");
        String empty = contentAsString(route(app, get("/work-history?f=Harvest", owner)));
        assertThat(empty).doesNotContain("data-name=\"Mapped work field\"");
        assertThat(contentAsString(route(app, get("/dashboard", owner)))).contains("Weeding", "Carrot");
    }

    @Test
    public void rejectsInvalidBoundsOnCreateAndUpdateAndKeepsFormValues() {
        User owner = user("editor");
        Field existing = field(owner, "Original field");
        Map<String, String> invalid = Map.of(
            "name", "Invalid bounds", "northEastLatitude", "35", "northEastLongitude", "138",
            "southWestLatitude", "36", "southWestLongitude", "139");
        for (String url : List.of("/fields", "/fields/" + existing.getId())) {
            Http.RequestBuilder request = get(url, owner).method(POST).bodyForm(invalid);
            Result result = route(app, addCSRFToken(request));
            assertThat(result.status()).isEqualTo(BAD_REQUEST);
            assertThat(contentAsString(result)).contains("Invalid bounds", "data-editable=\"true\"");
        }
        existing.refresh();
        assertThat(existing.getName()).isEqualTo("Original field");
        assertThat(existing.getNorthEastLatitude()).isEqualTo(36.1);

        Http.RequestBuilder valid = get("/fields", owner).method(POST).bodyForm(Map.of(
            "name", "Valid new field", "northEastLatitude", "36.2", "northEastLongitude", "138.2",
            "southWestLatitude", "36.1", "southWestLongitude", "138.1"));
        assertThat(route(app, addCSRFToken(valid)).status()).isEqualTo(SEE_OTHER);
        assertThat(contentAsString(route(app, get("/dashboard", owner)))).contains("data-name=\"Valid new field\"");
        Http.RequestBuilder update = get("/fields/" + existing.getId(), owner).method(POST).bodyForm(Map.of(
            "name", "Updated field", "northEastLatitude", "36.3", "northEastLongitude", "138.3",
            "southWestLatitude", "36.2", "southWestLongitude", "138.2"));
        assertThat(route(app, addCSRFToken(update)).status()).isEqualTo(SEE_OTHER);
        existing.refresh();
        assertThat(existing.getName()).isEqualTo("Updated field");
        assertThat(existing.getNorthEastLatitude()).isEqualTo(36.3);
    }
}
