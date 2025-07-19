package utils;

import actions.GlobalConfigAction;
import play.mvc.Http;

/**
 * Implicit conversions for templates to access global configurations.
 * This class provides implicit methods for Scala templates to access
 * GoogleTagManager and LegalLinksConfiguration from request context.
 */
public class TemplateImplicits {

    /**
     * Get implicit GoogleTagManager for templates
     * @param request the HTTP request  
     * @return GoogleTagManager instance from request context
     */
    public static GoogleTagManager gtm(Http.Request request) {
        return request.attrs().get(GlobalConfigAction.GTM_KEY);
    }

    /**
     * Get implicit LegalLinksConfiguration for templates  
     * @param request the HTTP request
     * @return LegalLinksConfiguration instance from request context
     */
    public static LegalLinksConfiguration legalLinks(Http.Request request) {
        return request.attrs().get(GlobalConfigAction.LEGAL_LINKS_KEY);
    }
}