package controllers;

import actions.Authenticated;
import actions.AuthenticatedAction;
import actions.GlobalConfig;
import jakarta.persistence.PersistenceException;
import models.Field;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.FieldRepository;
import services.SlackNotificationService;
import utils.GlobalConfigHelper;
import views.html.field.createForm;
import views.html.field.editForm;
import views.html.field.list;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Authenticated
@GlobalConfig
public class FieldController extends Controller {

    private final FieldRepository fieldRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;
    private final SlackNotificationService slackNotificationService;

    @Inject
    public FieldController(FormFactory formFactory,
                          FieldRepository fieldRepository,
                          ClassLoaderExecutionContext classLoaderExecutionContext,
                          MessagesApi messagesApi,
                          SlackNotificationService slackNotificationService) {
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
    private Result GO_FIELD_LIST = Results.redirect(
            routes.FieldController.list(0, "name", "asc", "")
    );

    /**
     * Display the paginated list of fields.
     *
     * @param page   Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on field names
     */
    public CompletionStage<Result> list(Http.Request request, int page, String sortBy, String order, String filter) {
        User user = getCurrentUser(request);
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return fieldRepository.pageByUser(page, 10, sortBy, order, filter, user.getId()).thenApplyAsync(pagedList -> {
            // This is the HTTP rendering thread context
            return ok(list.render(pagedList, sortBy, order, filter, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display the 'edit form' of a existing Field.
     *
     * @param id Id of the field to edit
     */
    public CompletionStage<Result> edit(Http.Request request, Long id) {
        User user = getCurrentUser(request);

        // Run the lookup in another thread:
        return fieldRepository.lookupByUser(id, user.getId()).thenApplyAsync(fieldOptional -> {
            if (fieldOptional.isEmpty()) {
                return notFound("Field not found or you don't have permission to access it");
            }
            // This is the HTTP rendering thread context
            Field f = fieldOptional.get();
            Form<Field> fieldForm = formFactory.form(Field.class).fill(f);
            return ok(editForm.render(id, fieldForm, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the field to edit
     */
    public CompletionStage<Result> update(Http.Request request, Long id) throws PersistenceException {
        User user = getCurrentUser(request);
        Form<Field> fieldForm = formFactory.form(Field.class).bindFromRequest(request);
        if (fieldForm.hasErrors()) {
            // This is the HTTP rendering thread context
            return java.util.concurrent.CompletableFuture.completedFuture(
                badRequest(editForm.render(id, fieldForm, request, messagesApi.preferred(request)))
            );
        } else {
            Field newFieldData = fieldForm.get();
            newFieldData.setUser(user); // Ensure the field belongs to current user
            // Run update operation and then flash and then redirect
            return fieldRepository.updateByUser(id, newFieldData, user.getId()).thenApplyAsync(data -> {
                if (data.isEmpty()) {
                    return notFound("Field not found or you don't have permission to update it");
                }
                
                // Send Slack notification for field update
                slackNotificationService.notifyDataUpdate("フィールド", newFieldData.getName(), user, request)
                    .exceptionally(throwable -> {
                        play.Logger.of(FieldController.class).warn("Failed to send Slack update notification for field: " + newFieldData.getName(), throwable);
                        return false;
                    });
                
                // This is the HTTP rendering thread context
                return GO_FIELD_LIST
                        .flashing("success", "Field " + newFieldData.getName() + " has been updated");
            }, classLoaderExecutionContext.current());
        }
    }

    /**
     * Display the 'new field form'.
     */
    public CompletionStage<Result> create(Http.Request request) {
        Form<Field> fieldForm = formFactory.form(Field.class);
        // This is the HTTP rendering thread context
        return java.util.concurrent.CompletableFuture.completedFuture(
            ok(createForm.render(fieldForm, request, messagesApi.preferred(request)))
        );
    }

    /**
     * Handle the 'new field form' submission
     */
    public CompletionStage<Result> save(Http.Request request) {
        User user = getCurrentUser(request);
        Form<Field> fieldForm = formFactory.form(Field.class).bindFromRequest(request);
        if (fieldForm.hasErrors()) {
            // This is the HTTP rendering thread context
            return java.util.concurrent.CompletableFuture.completedFuture(
                badRequest(createForm.render(fieldForm, request, messagesApi.preferred(request)))
            );
        }

        Field field = fieldForm.get();
        field.setUser(user); // Set the current user as the owner
        // Run insert db operation, then redirect
        return fieldRepository.insert(field).thenApplyAsync(data -> {
            // Send Slack notification for field creation
            slackNotificationService.notifyDataCreation("フィールド", field.getName(), user, request)
                .exceptionally(throwable -> {
                    play.Logger.of(FieldController.class).warn("Failed to send Slack creation notification for field: " + field.getName(), throwable);
                    return false;
                });
            
            // This is the HTTP rendering thread context
            return GO_FIELD_LIST
                    .flashing("success", "Field " + field.getName() + " has been created");
        }, classLoaderExecutionContext.current());
    }

    /**
     * Handle field deletion
     */
    public CompletionStage<Result> delete(Http.Request request, Long id) {
        User user = getCurrentUser(request);
        // First lookup the field to get its name for notification
        return fieldRepository.lookupByUser(id, user.getId()).thenComposeAsync(fieldOptional -> {
            if (fieldOptional.isEmpty()) {
                return CompletableFuture.completedFuture(notFound("Field not found or you don't have permission to delete it"));
            }
            
            Field field = fieldOptional.get();
            String fieldName = field.getName();
            
            // Run delete db operation, then redirect
            return fieldRepository.deleteByUser(id, user.getId()).thenApplyAsync(success -> {
                if (!success) {
                    return notFound("Field not found or you don't have permission to delete it");
                }
                
                // Send Slack notification for field deletion
                slackNotificationService.notifyDataDeletion("フィールド", fieldName, user, request)
                    .exceptionally(throwable -> {
                        play.Logger.of(FieldController.class).warn("Failed to send Slack deletion notification for field: " + fieldName, throwable);
                        return false;
                    });
                
                // This is the HTTP rendering thread context
                return GO_FIELD_LIST
                        .flashing("success", "Field has been deleted");
            }, classLoaderExecutionContext.current());
        }, classLoaderExecutionContext.current());
    }
}