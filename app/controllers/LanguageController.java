package controllers;

import actions.GlobalConfig;
import play.i18n.Lang;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * Controller for handling language switching
 */
@GlobalConfig
public class LanguageController extends Controller {

    @Inject
    private play.i18n.MessagesApi messagesApi;

    /**
     * Switch language and redirect back to referrer or home
     */
    public Result switchLanguage(String lang, Http.Request request) {
        // Set the language in the session
        String referer = request.header("Referer").orElse("/");
        
        // Return redirect with language cookie set
        return redirect(referer).withLang(Lang.forCode(lang), messagesApi);
    }
}