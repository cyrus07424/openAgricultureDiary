package utils;

import actions.GlobalConfigAction;
import play.mvc.Http;

/**
 * Utility class to access globally available GoogleTagManager and LegalLinksConfiguration
 * from the request context.
 */
public class GlobalConfigHelper {

    /**
     * Get GoogleTagManager from request context
     * @param request the HTTP request
     * @return GoogleTagManager instance
     */
    public static GoogleTagManager getGoogleTagManager(Http.Request request) {
        return request.attrs().get(GlobalConfigAction.GTM_KEY);
    }

    /**
     * Get LegalLinksConfiguration from request context
     * @param request the HTTP request
     * @return LegalLinksConfiguration instance
     */
    public static LegalLinksConfiguration getLegalLinksConfiguration(Http.Request request) {
        return request.attrs().get(GlobalConfigAction.LEGAL_LINKS_KEY);
    }
}