package controllers;

import actions.Authenticated;
import actions.AuthenticatedAction;
import jakarta.persistence.PersistenceException;
import models.Crop;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.CompanyRepository;
import repositoryies.CropRepository;
import utils.GoogleTagManager;
import utils.LegalLinksConfiguration;
import views.html.crop.createForm;
import views.html.crop.editForm;
import views.html.crop.list;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Authenticated
public class CropController extends Controller {

    private final CropRepository cropRepository;
    private final CompanyRepository companyRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;
    private final GoogleTagManager gtm;
    private final LegalLinksConfiguration legalLinksConfiguration;

    @Inject
    public CropController(FormFactory formFactory,
                          CropRepository cropRepository,
                          CompanyRepository companyRepository,
                          ClassLoaderExecutionContext classLoaderExecutionContext,
                          MessagesApi messagesApi,
                          GoogleTagManager gtm,
                          LegalLinksConfiguration legalLinksConfiguration) {
        this.cropRepository = cropRepository;
        this.formFactory = formFactory;
        this.companyRepository = companyRepository;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
        this.gtm = gtm;
        this.legalLinksConfiguration = legalLinksConfiguration;
    }

    private User getCurrentUser(Http.Request request) {
        return request.attrs().get(AuthenticatedAction.USER_KEY);
    }

    /**
     * This result directly redirect to application home.
     */
    private Result GO_CROP_LIST = Results.redirect(
            routes.CropController.list(0, "name", "asc", "")
    );

    /**
     * Display the paginated list of crops.
     *
     * @param page   Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on crop names
     */
    public CompletionStage<Result> list(Http.Request request, int page, String sortBy, String order, String filter) {
        User user = getCurrentUser(request);
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return cropRepository.pageByUser(page, 10, sortBy, order, filter, user.getId()).thenApplyAsync(pagedList -> {
            // This is the HTTP rendering thread context
            return ok(list.render(pagedList, sortBy, order, filter, request, messagesApi.preferred(request), gtm, legalLinksConfiguration));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display the 'edit form' of a existing Crop.
     *
     * @param id Id of the crop to edit
     */
    public CompletionStage<Result> edit(Http.Request request, Long id) {
        User user = getCurrentUser(request);

        // Run a db operation in another thread (using DatabaseExecutionContext)
        CompletionStage<Map<String, String>> companiesFuture = companyRepository.options();

        // Run the lookup also in another thread, then combine the results:
        return cropRepository.lookupByUser(id, user.getId()).thenCombineAsync(companiesFuture, (cropOptional, companies) -> {
            if (cropOptional.isEmpty()) {
                return notFound("Crop not found or you don't have permission to access it");
            }
            // This is the HTTP rendering thread context
            Crop c = cropOptional.get();
            Form<Crop> cropForm = formFactory.form(Crop.class).fill(c);
            return ok(editForm.render(id, cropForm, companies, request, messagesApi.preferred(request), gtm, legalLinksConfiguration));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the crop to edit
     */
    public CompletionStage<Result> update(Http.Request request, Long id) throws PersistenceException {
        User user = getCurrentUser(request);
        Form<Crop> cropForm = formFactory.form(Crop.class).bindFromRequest(request);
        if (cropForm.hasErrors()) {
            // Run companies db operation and then render the failure case
            return companyRepository.options().thenApplyAsync(companies -> {
                // This is the HTTP rendering thread context
                return badRequest(editForm.render(id, cropForm, companies, request, messagesApi.preferred(request), gtm, legalLinksConfiguration));
            }, classLoaderExecutionContext.current());
        } else {
            Crop newCropData = cropForm.get();
            newCropData.setUser(user); // Ensure the crop belongs to current user
            // Run update operation and then flash and then redirect
            return cropRepository.updateByUser(id, newCropData, user.getId()).thenApplyAsync(data -> {
                if (data.isEmpty()) {
                    return notFound("Crop not found or you don't have permission to update it");
                }
                // This is the HTTP rendering thread context
                return GO_CROP_LIST
                        .flashing("success", "Crop " + newCropData.getName() + " has been updated");
            }, classLoaderExecutionContext.current());
        }
    }

    /**
     * Display the 'new crop form'.
     */
    public CompletionStage<Result> create(Http.Request request) {
        Form<Crop> cropForm = formFactory.form(Crop.class);
        // Run companies db operation and then render the form
        return companyRepository.options().thenApplyAsync((Map<String, String> companies) -> {
            // This is the HTTP rendering thread context
            return ok(createForm.render(cropForm, companies, request, messagesApi.preferred(request), gtm, legalLinksConfiguration));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle the 'new crop form' submission
     */
    public CompletionStage<Result> save(Http.Request request) {
        User user = getCurrentUser(request);
        Form<Crop> cropForm = formFactory.form(Crop.class).bindFromRequest(request);
        if (cropForm.hasErrors()) {
            // Run companies db operation and then render the form
            return companyRepository.options().thenApplyAsync(companies -> {
                // This is the HTTP rendering thread context
                return badRequest(createForm.render(cropForm, companies, request, messagesApi.preferred(request), gtm, legalLinksConfiguration));
            }, classLoaderExecutionContext.current());
        }

        Crop crop = cropForm.get();
        crop.setUser(user); // Set the current user as the owner
        // Run insert db operation, then redirect
        return cropRepository.insert(crop).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return GO_CROP_LIST
                    .flashing("success", "Crop " + crop.getName() + " has been created");
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle crop deletion
     */
    public CompletionStage<Result> delete(Http.Request request, Long id) {
        User user = getCurrentUser(request);
        // Run delete db operation, then redirect
        return cropRepository.deleteByUser(id, user.getId()).thenApplyAsync(success -> {
            if (!success) {
                return notFound("Crop not found or you don't have permission to delete it");
            }
            // This is the HTTP rendering thread context
            return GO_CROP_LIST
                    .flashing("success", "Crop has been deleted");
        }, classLoaderExecutionContext.current());
    }
}