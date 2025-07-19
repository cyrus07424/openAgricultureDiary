package utils;

import com.typesafe.config.Config;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Utility class for managing Terms of Service and Privacy Policy URLs from environment variables
 */
@Singleton
public class LegalLinksConfiguration {
    
    private final Config config;
    
    @Inject
    public LegalLinksConfiguration(Config config) {
        this.config = config;
    }
    
    /**
     * Get the Terms of Service URL from configuration
     * @return Terms of Service URL or empty if not configured
     */
    public Optional<String> getTermsOfServiceUrl() {
        try {
            if (config.hasPath("legal.terms.url")) {
                String url = config.getString("legal.terms.url");
                if (url != null && !url.trim().isEmpty()) {
                    return Optional.of(url);
                }
            }
        } catch (Exception e) {
            // If configuration is missing or invalid, return empty
        }
        return Optional.empty();
    }
    
    /**
     * Get the Privacy Policy URL from configuration
     * @return Privacy Policy URL or empty if not configured
     */
    public Optional<String> getPrivacyPolicyUrl() {
        try {
            if (config.hasPath("legal.privacy.url")) {
                String url = config.getString("legal.privacy.url");
                if (url != null && !url.trim().isEmpty()) {
                    return Optional.of(url);
                }
            }
        } catch (Exception e) {
            // If configuration is missing or invalid, return empty
        }
        return Optional.empty();
    }
    
    /**
     * Check if Terms of Service URL is configured
     * @return true if Terms of Service URL is configured
     */
    public boolean hasTermsOfServiceUrl() {
        return getTermsOfServiceUrl().isPresent();
    }
    
    /**
     * Check if Privacy Policy URL is configured
     * @return true if Privacy Policy URL is configured
     */
    public boolean hasPrivacyPolicyUrl() {
        return getPrivacyPolicyUrl().isPresent();
    }
}