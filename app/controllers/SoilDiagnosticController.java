package controllers;

import actions.Authenticated;
import actions.AuthenticatedAction;
import actions.GlobalConfig;
import jakarta.persistence.PersistenceException;
import models.SoilDiagnostic;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.SoilDiagnosticRepository;
import repositoryies.FieldRepository;
import services.SlackNotificationService;
import utils.GlobalConfigHelper;
import views.html.soilDiagnostic.createForm;
import views.html.soilDiagnostic.editForm;
import views.html.soilDiagnostic.list;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Authenticated
@GlobalConfig
public class SoilDiagnosticController extends Controller {

    private final SoilDiagnosticRepository soilDiagnosticRepository;
    private final FieldRepository fieldRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;
    private final SlackNotificationService slackNotificationService;

    @Inject
    public SoilDiagnosticController(FormFactory formFactory,
                                  SoilDiagnosticRepository soilDiagnosticRepository,
                                  FieldRepository fieldRepository,
                                  ClassLoaderExecutionContext classLoaderExecutionContext,
                                  MessagesApi messagesApi,
                                  SlackNotificationService slackNotificationService) {
        this.soilDiagnosticRepository = soilDiagnosticRepository;
        this.fieldRepository = fieldRepository;
        this.formFactory = formFactory;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
        this.slackNotificationService = slackNotificationService;
    }

    private User getCurrentUser(Http.Request request) {
        return request.attrs().get(AuthenticatedAction.USER_KEY);
    }

    /**
     * This result directly redirect to application home.
     */
    private Result GO_SOIL_DIAGNOSTIC_LIST = Results.redirect(
            routes.SoilDiagnosticController.list(0, "diagnosticDate", "desc", "")
    );

    /**
     * Display the paginated list of soil diagnostics.
     *
     * @param page   Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on field names
     */
    public CompletionStage<Result> list(Http.Request request, int page, String sortBy, String order, String filter) {
        User user = getCurrentUser(request);
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return soilDiagnosticRepository.pageByUser(page, 10, sortBy, order, filter, user.getId()).thenApplyAsync(pagedList -> {
            // This is the HTTP rendering thread context
            return ok(list.render(pagedList, sortBy, order, filter, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display the 'edit form' of a existing SoilDiagnostic.
     *
     * @param id Id of the soil diagnostic to edit
     */
    public CompletionStage<Result> edit(Http.Request request, Long id) {
        User user = getCurrentUser(request);

        // Get field options and soil diagnostic in parallel
        CompletionStage<java.util.Map<String, String>> fieldOptionsFuture = fieldRepository.optionsByUser(user.getId());
        
        // Run the lookup in another thread:
        return soilDiagnosticRepository.lookupByUser(id, user.getId()).thenCombineAsync(fieldOptionsFuture, (soilDiagnosticOptional, fieldOptions) -> {
            if (soilDiagnosticOptional.isEmpty()) {
                return notFound("Soil diagnostic not found or you don't have permission to access it");
            }
            // This is the HTTP rendering thread context
            SoilDiagnostic sd = soilDiagnosticOptional.get();
            Form<SoilDiagnostic> soilDiagnosticForm = formFactory.form(SoilDiagnostic.class).fill(sd);
            return ok(editForm.render(id, soilDiagnosticForm, fieldOptions, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the soil diagnostic to edit
     */
    public CompletionStage<Result> update(Http.Request request, Long id) throws PersistenceException {
        User user = getCurrentUser(request);
        Form<SoilDiagnostic> soilDiagnosticForm = formFactory.form(SoilDiagnostic.class).bindFromRequest(request);
        
        if (soilDiagnosticForm.hasErrors()) {
            // Get field options for re-rendering the form
            return fieldRepository.optionsByUser(user.getId()).thenApplyAsync(fieldOptions -> {
                return badRequest(editForm.render(id, soilDiagnosticForm, fieldOptions, request, messagesApi.preferred(request)));
            }, classLoaderExecutionContext.current());
        } else {
            SoilDiagnostic newSoilDiagnosticData = soilDiagnosticForm.get();
            newSoilDiagnosticData.setUser(user); // Ensure the soil diagnostic belongs to current user
            // Run update operation and then flash and then redirect
            return soilDiagnosticRepository.updateByUser(id, newSoilDiagnosticData, user.getId()).thenApplyAsync(data -> {
                if (data.isEmpty()) {
                    return notFound("Soil diagnostic not found or you don't have permission to update it");
                }
                
                // Send Slack notification for soil diagnostic update
                String fieldName = newSoilDiagnosticData.getField() != null ? newSoilDiagnosticData.getField().getName() : "Unknown Field";
                slackNotificationService.notifyDataUpdate("土壌診断", fieldName + " (" + newSoilDiagnosticData.getDiagnosticDate() + ")", user, request)
                    .exceptionally(throwable -> {
                        play.Logger.of(SoilDiagnosticController.class).warn("Failed to send Slack update notification for soil diagnostic", throwable);
                        return false;
                    });
                
                // This is the HTTP rendering thread context
                return GO_SOIL_DIAGNOSTIC_LIST
                        .flashing("success", "Soil diagnostic for " + fieldName + " has been updated");
            }, classLoaderExecutionContext.current());
        }
    }

    /**
     * Display the 'new soil diagnostic form'.
     */
    public CompletionStage<Result> create(Http.Request request) {
        User user = getCurrentUser(request);
        Form<SoilDiagnostic> soilDiagnosticForm = formFactory.form(SoilDiagnostic.class);
        
        // Get field options for the form
        return fieldRepository.optionsByUser(user.getId()).thenApplyAsync(fieldOptions -> {
            return ok(createForm.render(soilDiagnosticForm, fieldOptions, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle the 'new soil diagnostic form' submission
     */
    public CompletionStage<Result> save(Http.Request request) {
        User user = getCurrentUser(request);
        Form<SoilDiagnostic> soilDiagnosticForm = formFactory.form(SoilDiagnostic.class).bindFromRequest(request);
        
        if (soilDiagnosticForm.hasErrors()) {
            // Get field options for re-rendering the form
            return fieldRepository.optionsByUser(user.getId()).thenApplyAsync(fieldOptions -> {
                return badRequest(createForm.render(soilDiagnosticForm, fieldOptions, request, messagesApi.preferred(request)));
            }, classLoaderExecutionContext.current());
        }

        SoilDiagnostic soilDiagnostic = soilDiagnosticForm.get();
        soilDiagnostic.setUser(user); // Set the current user as the owner
        // Run insert db operation, then redirect
        return soilDiagnosticRepository.insert(soilDiagnostic).thenApplyAsync(data -> {
            // Send Slack notification for soil diagnostic creation
            String fieldName = soilDiagnostic.getField() != null ? soilDiagnostic.getField().getName() : "Unknown Field";
            slackNotificationService.notifyDataCreation("土壌診断", fieldName + " (" + soilDiagnostic.getDiagnosticDate() + ")", user, request)
                .exceptionally(throwable -> {
                    play.Logger.of(SoilDiagnosticController.class).warn("Failed to send Slack creation notification for soil diagnostic", throwable);
                    return false;
                });
            
            // This is the HTTP rendering thread context
            return GO_SOIL_DIAGNOSTIC_LIST
                    .flashing("success", "Soil diagnostic for " + fieldName + " has been created");
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle soil diagnostic deletion
     */
    public CompletionStage<Result> delete(Http.Request request, Long id) {
        User user = getCurrentUser(request);
        // First lookup the soil diagnostic to get its details for notification
        return soilDiagnosticRepository.lookupByUser(id, user.getId()).thenComposeAsync(soilDiagnosticOptional -> {
            if (soilDiagnosticOptional.isEmpty()) {
                return CompletableFuture.completedFuture(notFound("Soil diagnostic not found or you don't have permission to delete it"));
            }
            
            SoilDiagnostic soilDiagnostic = soilDiagnosticOptional.get();
            String fieldName = soilDiagnostic.getField() != null ? soilDiagnostic.getField().getName() : "Unknown Field";
            String diagnosticInfo = fieldName + " (" + soilDiagnostic.getDiagnosticDate() + ")";
            
            // Run delete db operation, then redirect
            return soilDiagnosticRepository.deleteByUser(id, user.getId()).thenApplyAsync(success -> {
                if (!success) {
                    return notFound("Soil diagnostic not found or you don't have permission to delete it");
                }
                
                // Send Slack notification for soil diagnostic deletion
                slackNotificationService.notifyDataDeletion("土壌診断", diagnosticInfo, user, request)
                    .exceptionally(throwable -> {
                        play.Logger.of(SoilDiagnosticController.class).warn("Failed to send Slack deletion notification for soil diagnostic", throwable);
                        return false;
                    });
                
                // This is the HTTP rendering thread context
                return GO_SOIL_DIAGNOSTIC_LIST
                        .flashing("success", "Soil diagnostic has been deleted");
            }, classLoaderExecutionContext.current());
        }, classLoaderExecutionContext.current());
    }
}