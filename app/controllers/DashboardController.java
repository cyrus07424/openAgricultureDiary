package controllers;

import actions.Authenticated;
import actions.AuthenticatedAction;
import actions.GlobalConfig;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repositoryies.CropRepository;
import repositoryies.FieldRepository;
import repositoryies.WorkHistoryRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

@Authenticated
@GlobalConfig
public class DashboardController extends Controller {
    private final FieldRepository fields;
    private final CropRepository crops;
    private final WorkHistoryRepository work;
    private final MessagesApi messages;
    private final ClassLoaderExecutionContext executionContext;

    @Inject
    public DashboardController(FieldRepository fields, CropRepository crops,
                               WorkHistoryRepository work, MessagesApi messages,
                               ClassLoaderExecutionContext executionContext) {
        this.fields = fields;
        this.crops = crops;
        this.work = work;
        this.messages = messages;
        this.executionContext = executionContext;
    }

    public CompletionStage<Result> index(Http.Request request) {
        Long userId = request.attrs().get(AuthenticatedAction.USER_KEY).getId();
        return fields.listByUser(userId).thenCombine(
                crops.pageByUser(0, 1, "name", "asc", "", userId),
                (fieldList, cropPage) -> new java.util.AbstractMap.SimpleImmutableEntry<>(fieldList, cropPage.getTotalCount()))
            .thenCombineAsync(work.pageByUser(0, 6, "date", "desc", "", userId),
                (summary, workPage) -> ok(views.html.dashboard.render(
                    summary.getKey(), summary.getValue(), workPage, request, messages.preferred(request))),
                executionContext.current());
    }
}
