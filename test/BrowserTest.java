import org.junit.Test;
import play.test.WithBrowser;

import static io.fluentlenium.core.filter.FilterConstructor.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BrowserTest extends WithBrowser {

  @Test
  public void testBrowser() {
    browser.goTo("http://localhost:" + port);

    assertThat(browser.$(".navbar-brand").first().text(), equalTo("Play sample application — Crop database"));
    assertThat(browser.$("#page-title").first().text(), equalTo("574 crops found"));

    assertThat(browser.$(".pagination li[aria-current]").first().text(), equalTo("Displaying 1 to 10 of 574"));

    browser.$(".pagination li.next a").click();

    assertThat(browser.$(".pagination li[aria-current]").first().text(), equalTo("Displaying 11 to 20 of 574"));

    browser.$("#searchbox").fill().with("Apple");
    browser.$("#searchsubmit").click();

    assertThat(browser.$("#page-title").first().text(), equalTo("13 crops found"));
    browser.$("a", withText("Apple II")).click();

    assertThat(browser.$("#page-title").first().text(), equalTo("Edit crop"));

    browser.$("#name").fill().with("");
    browser.$("button.btn-success").click();

    assertThat(browser.$("#name").attributes("class").get(0), equalTo("form-control is-invalid"));
    assertThat(browser.$("div#input-for-name span").first().text(), equalTo("This field is required"));

    browser.$("#name").fill().with("Apple IIa");

    browser.$("button.btn-success").click();

    assertThat(browser.$("#page-title").first().text(), equalTo("574 crops found"));
    assertThat(browser.$(".alert-warning").first().text(), equalTo("Done! Crop Apple IIa has been updated"));

    browser.$("#searchbox").fill().with("Apple");
    browser.$("#searchsubmit").click();

    browser.$("a", withText("Apple IIa")).click();
    browser.$("button.btn-danger").click();

    browser.takeHtmlDump("target/delete.html");

    assertThat(browser.$("#page-title").first().text(), equalTo("573 crops found"));
    assertThat(browser.$(".alert-warning").first().text(), equalTo("Done! Crop has been deleted"));

    browser.$("#searchbox").fill().with("Apple");
    browser.$("#searchsubmit").click();

    assertThat(browser.$("#page-title").first().text(), equalTo("12 crops found"));
  }

}
