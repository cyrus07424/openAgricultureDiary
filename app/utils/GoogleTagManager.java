package utils;

import com.typesafe.config.Config;
import play.api.libs.json.Json;
import play.twirl.api.Html;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Utility class for Google Tag Manager integration
 */
@Singleton
public class GoogleTagManager {
    
    private final Config config;
    
    @Inject
    public GoogleTagManager(Config config) {
        this.config = config;
    }
    
    /**
     * Get the GTM container ID from configuration
     * @return GTM container ID or null if not configured
     */
    public Optional<String> getContainerId() {
        try {
            if (config.hasPath("gtm.container.id")) {
                String containerId = config.getString("gtm.container.id");
                if (containerId != null && !containerId.trim().isEmpty()) {
                    return Optional.of(containerId);
                }
            }
        } catch (Exception e) {
            // If configuration is missing or invalid, return empty
        }
        return Optional.empty();
    }
    
    /**
     * Generate GTM head script tag
     * @return HTML for GTM head script or empty if not configured
     */
    public Html getHeadScript() {
        Optional<String> containerId = getContainerId();
        if (containerId.isPresent()) {
            String script = String.format(
                "<!-- Google Tag Manager -->\n" +
                "<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':\n" +
                "new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],\n" +
                "j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=\n" +
                "'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);\n" +
                "})(window,document,'script','dataLayer','%s');</script>\n" +
                "<!-- End Google Tag Manager -->",
                containerId.get()
            );
            return Html.apply(script);
        }
        return Html.apply("");
    }
    
    /**
     * Generate GTM body noscript tag
     * @return HTML for GTM body noscript or empty if not configured
     */
    public Html getBodyScript() {
        Optional<String> containerId = getContainerId();
        if (containerId.isPresent()) {
            String script = String.format(
                "<!-- Google Tag Manager (noscript) -->\n" +
                "<noscript><iframe src=\"https://www.googletagmanager.com/ns.html?id=%s\"\n" +
                "height=\"0\" width=\"0\" style=\"display:none;visibility:hidden\"></iframe></noscript>\n" +
                "<!-- End Google Tag Manager (noscript) -->",
                containerId.get()
            );
            return Html.apply(script);
        }
        return Html.apply("");
    }
}