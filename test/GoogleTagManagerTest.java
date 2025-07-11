import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import utils.GoogleTagManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.twirl.api.Html;

public class GoogleTagManagerTest {
    
    @Test
    public void testGtmWithValidContainerId() {
        // Create test configuration with GTM container ID
        Config config = ConfigFactory.parseString("gtm.container.id = \"GTM-TEST123\"");
        GoogleTagManager gtm = new GoogleTagManager(config);
        
        // Test that container ID is detected
        assertTrue(gtm.getContainerId().isPresent());
        assertEquals("GTM-TEST123", gtm.getContainerId().get());
        
        // Test head script generation
        Html headScript = gtm.getHeadScript();
        String headScriptHtml = headScript.toString();
        assertTrue(headScriptHtml.contains("Google Tag Manager"));
        assertTrue(headScriptHtml.contains("GTM-TEST123"));
        assertTrue(headScriptHtml.contains("googletagmanager.com/gtm.js"));
        
        // Test body script generation
        Html bodyScript = gtm.getBodyScript();
        String bodyScriptHtml = bodyScript.toString();
        assertTrue(bodyScriptHtml.contains("Google Tag Manager (noscript)"));
        assertTrue(bodyScriptHtml.contains("GTM-TEST123"));
        assertTrue(bodyScriptHtml.contains("googletagmanager.com/ns.html"));
        assertTrue(bodyScriptHtml.contains("<noscript>"));
    }
    
    @Test
    public void testGtmWithoutContainerId() {
        // Create test configuration without GTM container ID
        Config config = ConfigFactory.parseString("some.other.config = \"value\"");
        GoogleTagManager gtm = new GoogleTagManager(config);
        
        // Test that container ID is not detected
        assertFalse(gtm.getContainerId().isPresent());
        
        // Test that scripts are empty
        Html headScript = gtm.getHeadScript();
        assertEquals("", headScript.toString());
        
        Html bodyScript = gtm.getBodyScript();
        assertEquals("", bodyScript.toString());
    }
    
    @Test
    public void testGtmWithEmptyContainerId() {
        // Create test configuration with empty GTM container ID
        Config config = ConfigFactory.parseString("gtm.container.id = \"\"");
        GoogleTagManager gtm = new GoogleTagManager(config);
        
        // Test that container ID is not detected
        assertFalse(gtm.getContainerId().isPresent());
        
        // Test that scripts are empty
        Html headScript = gtm.getHeadScript();
        assertEquals("", headScript.toString());
        
        Html bodyScript = gtm.getBodyScript();
        assertEquals("", bodyScript.toString());
    }
}