package controllers;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final AssetsFinder assetsFinder;

    @Inject
    public HomeController(AssetsFinder assetsFinder) {
        this.assetsFinder = assetsFinder;
    }

    /**
     * Handle default path requests, redirect to crops list
     */
    public Result index(Http.Request request) {
        return Results.redirect(
                routes.CropController.list(0, "name", "asc", ""));
    }
}
