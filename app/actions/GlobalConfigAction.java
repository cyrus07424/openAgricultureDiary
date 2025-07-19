package actions;

import play.libs.typedmap.TypedKey;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import utils.GoogleTagManager;
import utils.LegalLinksConfiguration;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * Action that makes GoogleTagManager and LegalLinksConfiguration globally available
 * in the request context, eliminating the need for manual injection in every controller.
 */
public class GlobalConfigAction extends Action<GlobalConfig> {

    private final GoogleTagManager gtm;
    private final LegalLinksConfiguration legalLinksConfiguration;

    public static final TypedKey<GoogleTagManager> GTM_KEY = TypedKey.create("gtm");
    public static final TypedKey<LegalLinksConfiguration> LEGAL_LINKS_KEY = TypedKey.create("legalLinks");

    @Inject
    public GlobalConfigAction(GoogleTagManager gtm, LegalLinksConfiguration legalLinksConfiguration) {
        this.gtm = gtm;
        this.legalLinksConfiguration = legalLinksConfiguration;
    }

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        // Add global configurations to request attributes
        // Handle null cases gracefully
        GoogleTagManager gtagManager = gtm != null ? gtm : new GoogleTagManager(null);
        LegalLinksConfiguration legalConfig = legalLinksConfiguration != null ? legalLinksConfiguration : new LegalLinksConfiguration(null);
        
        Http.Request enhancedRequest = request
            .addAttr(GTM_KEY, gtagManager)
            .addAttr(LEGAL_LINKS_KEY, legalConfig);
        
        return delegate.call(enhancedRequest);
    }
}