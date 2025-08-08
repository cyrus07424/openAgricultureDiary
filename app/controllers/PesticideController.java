package controllers;

import actions.Admin;
import actions.AuthenticatedAction;
import actions.GlobalConfig;
import forms.PesticideUploadForm;
import models.PesticideRegistration;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.Files;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repositoryies.PesticideRepository;
import views.html.pesticide.list;
import views.html.pesticide.upload;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Controller for pesticide registration management (admin only)
 */
@Admin
@GlobalConfig
public class PesticideController extends Controller {

    private final PesticideRepository pesticideRepository;
    private final FormFactory formFactory;
    private final ClassLoaderExecutionContext classLoaderExecutionContext;
    private final MessagesApi messagesApi;

    @Inject
    public PesticideController(FormFactory formFactory,
                              PesticideRepository pesticideRepository,
                              ClassLoaderExecutionContext classLoaderExecutionContext,
                              MessagesApi messagesApi) {
        this.pesticideRepository = pesticideRepository;
        this.formFactory = formFactory;
        this.classLoaderExecutionContext = classLoaderExecutionContext;
        this.messagesApi = messagesApi;
    }

    private User getCurrentUser(Http.Request request) {
        return request.attrs().get(AuthenticatedAction.USER_KEY);
    }

    /**
     * Display the paginated list of pesticide registrations.
     */
    public CompletionStage<Result> list(Http.Request request, int page, String sortBy, String order, String filter) {
        return pesticideRepository.page(page, 10, sortBy, order, filter).thenApplyAsync(pagedList -> {
            return ok(list.render(pagedList, sortBy, order, filter, request, messagesApi.preferred(request)));
        }, classLoaderExecutionContext.current());
    }

    /**
     * Display the upload form
     */
    public Result showUpload(Http.Request request) {
        Form<PesticideUploadForm> uploadForm = formFactory.form(PesticideUploadForm.class);
        return ok(upload.render(uploadForm, request, messagesApi.preferred(request)));
    }

    /**
     * Handle file upload and parse ZIP files
     */
    public CompletionStage<Result> upload(Http.Request request) {
        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<Files.TemporaryFile> file = body.getFile("file");
        
        if (file == null) {
            Form<PesticideUploadForm> uploadForm = formFactory.form(PesticideUploadForm.class);
            return CompletableFuture.completedFuture(
                badRequest(upload.render(
                    uploadForm.withError("file", "ファイルを選択してください"),
                    request, 
                    messagesApi.preferred(request)
                ))
            );
        }

        String fileName = file.getFilename();
        if (!fileName.toLowerCase().endsWith(".zip")) {
            Form<PesticideUploadForm> uploadForm = formFactory.form(PesticideUploadForm.class);
            return CompletableFuture.completedFuture(
                badRequest(upload.render(
                    uploadForm.withError("file", "ZIPファイルを選択してください"),
                    request, 
                    messagesApi.preferred(request)
                ))
            );
        }

        try {
            Path filePath = file.getRef().path();
            List<PesticideRegistration> pesticides = parseZipFile(filePath);
            
            if (pesticides.isEmpty()) {
                Form<PesticideUploadForm> uploadForm = formFactory.form(PesticideUploadForm.class);
                return CompletableFuture.completedFuture(
                    badRequest(upload.render(
                        uploadForm.withError("file", "有効なデータが見つかりませんでした"),
                        request, 
                        messagesApi.preferred(request)
                    ))
                );
            }

            return pesticideRepository.insertAll(pesticides).thenApplyAsync(v -> {
                return Results.redirect(routes.PesticideController.list(0, "registrationNumber", "asc", ""))
                        .flashing("success", pesticides.size() + "件の農薬登録情報を追加しました");
            }, classLoaderExecutionContext.current());

        } catch (Exception e) {
            play.Logger.of(PesticideController.class).error("Failed to parse zip file", e);
            Form<PesticideUploadForm> uploadForm = formFactory.form(PesticideUploadForm.class);
            return CompletableFuture.completedFuture(
                badRequest(upload.render(
                    uploadForm.withError("file", "ファイルの解析に失敗しました: " + e.getMessage()),
                    request, 
                    messagesApi.preferred(request)
                ))
            );
        }
    }

    /**
     * Clear all pesticide data
     */
    public CompletionStage<Result> clear(Http.Request request) {
        return pesticideRepository.deleteAll().thenApplyAsync(v -> {
            return Results.redirect(routes.PesticideController.list(0, "registrationNumber", "asc", ""))
                    .flashing("success", "全ての農薬登録情報を削除しました");
        }, classLoaderExecutionContext.current());
    }

    private List<PesticideRegistration> parseZipFile(Path zipPath) throws IOException {
        List<PesticideRegistration> pesticides = new ArrayList<>();
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()), 
                Charset.forName("Shift_JIS"))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".csv")) {
                    pesticides.addAll(parseCsvFromZip(zis));
                }
                zis.closeEntry();
            }
        }
        
        return pesticides;
    }

    private List<PesticideRegistration> parseCsvFromZip(ZipInputStream zis) throws IOException {
        List<PesticideRegistration> pesticides = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zis, "Shift_JIS"))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header line
                }
                
                String[] fields = line.split(",", -1);
                if (fields.length >= 10) {
                    PesticideRegistration pesticide = new PesticideRegistration();
                    pesticide.setRegistrationNumber(cleanField(fields[0]));
                    pesticide.setUsage(cleanField(fields[1]));
                    pesticide.setPesticideType(cleanField(fields[2]));
                    pesticide.setPesticideName(cleanField(fields[3]));
                    pesticide.setAbbreviation(cleanField(fields[4]));
                    pesticide.setCropName(cleanField(fields[5]));
                    pesticide.setApplicationLocation(cleanField(fields[6]));
                    pesticide.setTargetPestDisease(cleanField(fields[7]));
                    pesticide.setPurpose(cleanField(fields[8]));
                    pesticide.setDilutionAmount(cleanField(fields[9]));
                    
                    pesticides.add(pesticide);
                }
            }
        }
        
        return pesticides;
    }

    private String cleanField(String field) {
        if (field == null) return "";
        // Remove quotes if present
        field = field.trim();
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1);
        }
        return field;
    }
}