package controllers;

import actions.Authenticated;
import actions.AuthenticatedAction;
import actions.GlobalConfig;
import jakarta.persistence.PersistenceException;
import models.WorkHistory;
import models.User;
import models.Field;
import models.Crop;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.WorkHistoryRepository;
import repositoryies.FieldRepository;
import repositoryies.CropRepository;
import services.SlackNotificationService;
import utils.GlobalConfigHelper;
import views.html.workHistory.createForm;
import views.html.workHistory.editForm;
import views.html.workHistory.list;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.Map;

@Authenticated
@GlobalConfig
public class WorkHistoryController extends Controller {

    private final WorkHistoryRepository workHistoryRepository;
    private final FieldRepository fieldRepository;
    private final CropRepository cropRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;
    private final SlackNotificationService slackNotificationService;

    @Inject
    public WorkHistoryController(FormFactory formFactory,
                                WorkHistoryRepository workHistoryRepository,
                                FieldRepository fieldRepository,
                                CropRepository cropRepository,
                                ClassLoaderExecutionContext classLoaderExecutionContext,
                                MessagesApi messagesApi,
                                SlackNotificationService slackNotificationService) {
        this.workHistoryRepository = workHistoryRepository;
        this.fieldRepository = fieldRepository;
        this.cropRepository = cropRepository;
        this.formFactory = formFactory;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
        this.slackNotificationService = slackNotificationService;
    }

    private User getCurrentUser(Http.Request request) {
        return request.attrs().get(AuthenticatedAction.USER_KEY);
    }

    /**
     * This result directly redirect to work history list.
     */
    private Result GO_WORK_HISTORY_LIST = Results.redirect(
            routes.WorkHistoryController.list(0, "date", "desc", "")
    );

    /**
     * Display the paginated list of work history.
     *
     * @param page   Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on work history content
     */
    public CompletionStage<Result> list(Http.Request request, int page, String sortBy, String order, String filter) {
        User user = getCurrentUser(request);
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return workHistoryRepository.pageByUser(page, 10, sortBy, order, filter, user.getId()).thenApplyAsync(pagedList -> {
            // This is the HTTP rendering thread context
            return ok(list.render(pagedList, sortBy, order, filter, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display the 'edit form' of a existing WorkHistory.
     *
     * @param id Id of the work history to edit
     */
    public CompletionStage<Result> edit(Http.Request request, Long id) {
        User user = getCurrentUser(request);

        // Run the lookup in another thread:
        return workHistoryRepository.lookupByUser(id, user.getId()).thenApplyAsync(workHistoryOptional -> {
            if (workHistoryOptional.isEmpty()) {
                return notFound("WorkHistory not found or you don't have permission to access it");
            }
            // This is the HTTP rendering thread context
            WorkHistory wh = workHistoryOptional.get();
            Form<WorkHistory> workHistoryForm = formFactory.form(WorkHistory.class).fill(wh);
            
            // Get the options and render the form synchronously in this thread
            try {
                Map<String, String> fields = fieldRepository.optionsByUser(user.getId()).toCompletableFuture().get();
                Map<String, String> crops = cropRepository.optionsByUser(user.getId()).toCompletableFuture().get();
                return ok(editForm.render(id, workHistoryForm, fields, crops, request, messagesApi.preferred(request)));
            } catch (Exception e) {
                return internalServerError("Error loading form data");
            }
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the work history to edit
     */
    public CompletionStage<Result> update(Http.Request request, Long id) throws PersistenceException {
        User user = getCurrentUser(request);
        Form<WorkHistory> workHistoryForm = formFactory.form(WorkHistory.class).bindFromRequest(request);
        if (workHistoryForm.hasErrors()) {
            // This is the HTTP rendering thread context
            try {
                Map<String, String> fields = fieldRepository.optionsByUser(user.getId()).toCompletableFuture().get();
                Map<String, String> crops = cropRepository.optionsByUser(user.getId()).toCompletableFuture().get();
                return java.util.concurrent.CompletableFuture.completedFuture(
                    badRequest(editForm.render(id, workHistoryForm, fields, crops, request, messagesApi.preferred(request)))
                );
            } catch (Exception e) {
                return java.util.concurrent.CompletableFuture.completedFuture(internalServerError("Error loading form data"));
            }
        } else {
            WorkHistory newWorkHistoryData = workHistoryForm.get();
            newWorkHistoryData.setUser(user); // Ensure the work history belongs to current user
            // Run update operation and then flash and then redirect
            return workHistoryRepository.updateByUser(id, newWorkHistoryData, user.getId()).thenApplyAsync(data -> {
                if (data.isEmpty()) {
                    return notFound("WorkHistory not found or you don't have permission to update it");
                }
                
                // Send Slack notification for work history update  
                slackNotificationService.notifyDataUpdate("作業履歴", "ID:" + id, user, request)
                    .exceptionally(throwable -> {
                        play.Logger.of(WorkHistoryController.class).warn("Failed to send Slack update notification for work history: " + id, throwable);
                        return false;
                    });
                
                // This is the HTTP rendering thread context
                return GO_WORK_HISTORY_LIST
                        .flashing("success", "Work history has been updated");
            }, classLoaderExecutionContext.current());
        }
    }

    /**
     * Display the 'new work history form'.
     */
    public CompletionStage<Result> create(Http.Request request) {
        User user = getCurrentUser(request);
        Form<WorkHistory> workHistoryForm = formFactory.form(WorkHistory.class);
        
        // Get options for fields and crops
        return fieldRepository.optionsByUser(user.getId()).thenCombineAsync(
            cropRepository.optionsByUser(user.getId()), 
            (fields, crops) -> {
                // This is the HTTP rendering thread context
                return ok(createForm.render(workHistoryForm, fields, crops, request, messagesApi.preferred(request)));
            }, 
            classLoaderExecutionContext.current()
        );
    }

    /**
     * Handle the 'new work history form' submission
     */
    public CompletionStage<Result> save(Http.Request request) {
        User user = getCurrentUser(request);
        Form<WorkHistory> workHistoryForm = formFactory.form(WorkHistory.class).bindFromRequest(request);
        if (workHistoryForm.hasErrors()) {
            // This is the HTTP rendering thread context
            try {
                Map<String, String> fields = fieldRepository.optionsByUser(user.getId()).toCompletableFuture().get();
                Map<String, String> crops = cropRepository.optionsByUser(user.getId()).toCompletableFuture().get();
                return java.util.concurrent.CompletableFuture.completedFuture(
                    badRequest(createForm.render(workHistoryForm, fields, crops, request, messagesApi.preferred(request)))
                );
            } catch (Exception e) {
                return java.util.concurrent.CompletableFuture.completedFuture(internalServerError("Error loading form data"));
            }
        }

        WorkHistory workHistory = workHistoryForm.get();
        workHistory.setUser(user); // Set the current user as the owner
        // Run insert db operation, then redirect
        return workHistoryRepository.insert(workHistory).thenApplyAsync(data -> {
            // Send Slack notification for work history creation
            slackNotificationService.notifyDataCreation("作業履歴", workHistory.getContent(), user, request)
                .exceptionally(throwable -> {
                    play.Logger.of(WorkHistoryController.class).warn("Failed to send Slack creation notification for work history: " + workHistory.getContent(), throwable);
                    return false;
                });
            
            // This is the HTTP rendering thread context
            return GO_WORK_HISTORY_LIST
                    .flashing("success", "Work history has been created");
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle work history deletion
     */
    public CompletionStage<Result> delete(Http.Request request, Long id) {
        User user = getCurrentUser(request);
        // First lookup the work history to get its content for notification
        return workHistoryRepository.lookupByUser(id, user.getId()).thenComposeAsync(workHistoryOptional -> {
            if (workHistoryOptional.isEmpty()) {
                return CompletableFuture.completedFuture(notFound("Work history not found or you don't have permission to delete it"));
            }
            
            WorkHistory workHistory = workHistoryOptional.get();
            String workHistoryContent = workHistory.getContent();
            
            // Run delete db operation, then redirect
            return workHistoryRepository.deleteByUser(id, user.getId()).thenApplyAsync(success -> {
                if (!success) {
                    return notFound("Work history not found or you don't have permission to delete it");
                }
                
                // Send Slack notification for work history deletion
                slackNotificationService.notifyDataDeletion("作業履歴", workHistoryContent, user, request)
                    .exceptionally(throwable -> {
                        play.Logger.of(WorkHistoryController.class).warn("Failed to send Slack deletion notification for work history: " + workHistoryContent, throwable);
                        return false;
                    });
                
                // This is the HTTP rendering thread context
                return GO_WORK_HISTORY_LIST
                        .flashing("success", "Work history has been deleted");
            }, classLoaderExecutionContext.current());
        }, classLoaderExecutionContext.current());
    }
}