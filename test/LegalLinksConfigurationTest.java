import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import utils.LegalLinksConfiguration;

import static org.junit.Assert.*;

public class LegalLinksConfigurationTest {

    @Test
    public void testTermsOfServiceUrlPresent() {
        Config config = ConfigFactory.parseString("legal.terms.url = \"https://example.com/terms\"");
        LegalLinksConfiguration legalLinks = new LegalLinksConfiguration(config);
        
        assertTrue(legalLinks.hasTermsOfServiceUrl());
        assertEquals("https://example.com/terms", legalLinks.getTermsOfServiceUrl().get());
    }

    @Test
    public void testTermsOfServiceUrlEmpty() {
        Config config = ConfigFactory.parseString("legal.terms.url = \"\"");
        LegalLinksConfiguration legalLinks = new LegalLinksConfiguration(config);
        
        assertFalse(legalLinks.hasTermsOfServiceUrl());
        assertFalse(legalLinks.getTermsOfServiceUrl().isPresent());
    }

    @Test
    public void testTermsOfServiceUrlMissing() {
        Config config = ConfigFactory.parseString("");
        LegalLinksConfiguration legalLinks = new LegalLinksConfiguration(config);
        
        assertFalse(legalLinks.hasTermsOfServiceUrl());
        assertFalse(legalLinks.getTermsOfServiceUrl().isPresent());
    }

    @Test
    public void testPrivacyPolicyUrlPresent() {
        Config config = ConfigFactory.parseString("legal.privacy.url = \"https://example.com/privacy\"");
        LegalLinksConfiguration legalLinks = new LegalLinksConfiguration(config);
        
        assertTrue(legalLinks.hasPrivacyPolicyUrl());
        assertEquals("https://example.com/privacy", legalLinks.getPrivacyPolicyUrl().get());
    }

    @Test
    public void testPrivacyPolicyUrlEmpty() {
        Config config = ConfigFactory.parseString("legal.privacy.url = \"\"");
        LegalLinksConfiguration legalLinks = new LegalLinksConfiguration(config);
        
        assertFalse(legalLinks.hasPrivacyPolicyUrl());
        assertFalse(legalLinks.getPrivacyPolicyUrl().isPresent());
    }

    @Test
    public void testPrivacyPolicyUrlMissing() {
        Config config = ConfigFactory.parseString("");
        LegalLinksConfiguration legalLinks = new LegalLinksConfiguration(config);
        
        assertFalse(legalLinks.hasPrivacyPolicyUrl());
        assertFalse(legalLinks.getPrivacyPolicyUrl().isPresent());
    }

    @Test
    public void testBothUrlsPresent() {
        Config config = ConfigFactory.parseString(
            "legal.terms.url = \"https://example.com/terms\"\n" +
            "legal.privacy.url = \"https://example.com/privacy\""
        );
        LegalLinksConfiguration legalLinks = new LegalLinksConfiguration(config);
        
        assertTrue(legalLinks.hasTermsOfServiceUrl());
        assertTrue(legalLinks.hasPrivacyPolicyUrl());
        assertEquals("https://example.com/terms", legalLinks.getTermsOfServiceUrl().get());
        assertEquals("https://example.com/privacy", legalLinks.getPrivacyPolicyUrl().get());
    }
}