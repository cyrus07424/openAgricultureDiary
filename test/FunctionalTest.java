import controllers.routes;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.test.Helpers.*;

// Use FixMethodOrder to run the tests sequentially
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FunctionalTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .configure("play.evolutions.db.default.enabled", "true")
            .configure("play.evolutions.db.default.autoApply", "true")
            .configure("play.filters.hosts.allowed.0", "localhost:19001")
            .configure("db.default.driver", "org.h2.Driver")
            .configure("db.default.url", "jdbc:h2:mem:test")
            .build();
    }

    @Test
    public void redirectHomePage() {
        Result result = route(app, controllers.routes.HomeController.index());

        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation().get()).isEqualTo("/crops");
    }

    @Test
    public void listCropsOnTheFirstPage() {
        Result result = route(app, controllers.routes.CropController.list(0, "name", "asc", ""));

        assertThat(result.status()).isEqualTo(OK);
        assertThat(contentAsString(result)).contains("574 crops found");
    }

    @Test
    public void filterCropByName() {
        Result result = route(app, controllers.routes.CropController.list(0, "name", "asc", "Apple"));

        assertThat(result.status()).isEqualTo(OK);
        assertThat(contentAsString(result)).contains("13 crops found");
    }

    @Test
    public void createANewCrop() {
        Result result = route(app, addCSRFToken(fakeRequest().uri(controllers.routes.CropController.save().url())));
        assertThat(result.status()).isEqualTo(OK);

        Map<String, String> data = new HashMap<>();
        data.put("name", "FooBar");
        data.put("introduced", "badbadbad");
        data.put("company.id", "1");

        String saveUrl = controllers.routes.CropController.save().url();
        result = route(app, addCSRFToken(fakeRequest().bodyForm(data).method("POST").uri(saveUrl)));

        assertThat(result.status()).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(result)).contains("<option value=\"1\" selected=\"selected\">Apple Inc.</option>");
        //  <input type="text" id="introduced" name="introduced" value="badbadbad" aria-describedby="introduced_info_0 introduced_error_0" aria-invalid="true" class="form-control">
        assertThat(contentAsString(result)).contains("<input class=\"form-control is-invalid\" type=\"date\" id=\"introduced\" name=\"introduced\" value=\"badbadbad\" ");
        // <input type="text" id="name" name="name" value="FooBar" aria-describedby="name_info_0" required="true" class="form-control">
        assertThat(contentAsString(result)).contains("<input class=\"form-control\" type=\"text\" id=\"name\" name=\"name\" value=\"FooBar\" ");

        data.put("introduced", "2011-12-24");

        result = route(app, fakeRequest().bodyForm(data).method("POST").uri(saveUrl));

        assertThat(result.status()).isEqualTo(SEE_OTHER);
        assertThat(result.redirectLocation().get()).isEqualTo("/crops");
        assertThat(result.flash().get("success").get()).isEqualTo("Crop FooBar has been created");

        result = route(app, controllers.routes.CropController.list(0, "name", "asc", "FooBar"));
        assertThat(result.status()).isEqualTo(OK);
        assertThat(contentAsString(result)).contains("One crop found");
    }

}
