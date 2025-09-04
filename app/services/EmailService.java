package services;

import play.Logger;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Service for sending emails using SMTP with bilingual support
 */
@Singleton
public class EmailService {
    
    private static final Logger.ALogger logger = Logger.of(EmailService.class);
    
    private final ClassLoaderExecutionContext executionContext;
    private final MailerClient mailerClient;
    private final MessagesApi messagesApi;
    private final String fromEmail;
    private final String fromName;
    
    @Inject
    public EmailService(ClassLoaderExecutionContext executionContext, MailerClient mailerClient, MessagesApi messagesApi) {
        this.executionContext = executionContext;
        this.mailerClient = mailerClient;
        this.messagesApi = messagesApi;
        this.fromEmail = System.getenv("SMTP_FROM_EMAIL");
        this.fromName = System.getenv().getOrDefault("SMTP_FROM_NAME", "Open Agriculture Diary");
        
        if (this.fromEmail == null) {
            logger.warn("SMTP_FROM_EMAIL environment variable not set. Email functionality will be disabled.");
        }
        
        // Check if SMTP configuration is present
        String smtpHost = System.getenv("SMTP_HOST");
        if (smtpHost == null) {
            logger.warn("SMTP_HOST environment variable not set. Email functionality will be disabled.");
        }
    }
    
    /**
     * Send a welcome email to a new user (bilingual)
     */
    public CompletionStage<Boolean> sendWelcomeEmail(String toEmail, String username) {
        if (!isConfigured()) {
            logger.warn("Email service not configured. Skipping welcome email for: " + toEmail);
            return CompletableFuture.completedFuture(false);
        }
        
        // Create bilingual subject and content
        Messages messagesJa = messagesApi.preferred(java.util.Arrays.asList(Lang.forCode("ja")));
        Messages messagesEn = messagesApi.preferred(java.util.Arrays.asList(Lang.forCode("en")));
        
        String subjectJa = messagesJa.at("email.welcome.subject");
        String subjectEn = messagesEn.at("email.welcome.subject");
        String subject = subjectJa + " / " + subjectEn;
        
        String htmlContent = buildBilingualWelcomeEmailHtml(username, messagesJa, messagesEn);
        String textContent = buildBilingualWelcomeEmailText(username, messagesJa, messagesEn);
        
        return sendEmail(toEmail, subject, htmlContent, textContent);
    }
    
    /**
     * Send a password reset email (bilingual)
     */
    public CompletionStage<Boolean> sendPasswordResetEmail(String toEmail, String username, String resetToken, String baseUrl) {
        if (!isConfigured()) {
            logger.warn("Email service not configured. Skipping password reset email for: " + toEmail);
            return CompletableFuture.completedFuture(false);
        }
        
        // Create bilingual subject and content
        Messages messagesJa = messagesApi.preferred(java.util.Arrays.asList(Lang.forCode("ja")));
        Messages messagesEn = messagesApi.preferred(java.util.Arrays.asList(Lang.forCode("en")));
        
        String subjectJa = messagesJa.at("email.password.reset.subject");
        String subjectEn = messagesEn.at("email.password.reset.subject");
        String subject = subjectJa + " / " + subjectEn;
        
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        String htmlContent = buildBilingualPasswordResetEmailHtml(username, resetUrl, messagesJa, messagesEn);
        String textContent = buildBilingualPasswordResetEmailText(username, resetUrl, messagesJa, messagesEn);
        
        return sendEmail(toEmail, subject, htmlContent, textContent);
    }
    
    /**
     * Send email using SMTP via play-mailer
     */
    private CompletionStage<Boolean> sendEmail(String toEmail, String subject, String htmlContent, String textContent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Email email = new Email()
                    .setSubject(subject)
                    .setFrom(fromName + " <" + fromEmail + ">")
                    .addTo(toEmail)
                    .setBodyText(textContent)
                    .setBodyHtml(htmlContent);
                
                mailerClient.send(email);
                logger.info("Email sent successfully to: " + toEmail);
                return true;
                
            } catch (Exception e) {
                logger.error("Error sending email to: " + toEmail, e);
                return false;
            }
        }, executionContext.current());
    }
    
    /**
     * Check if email service is properly configured
     */
    private boolean isConfigured() {
        String smtpHost = System.getenv("SMTP_HOST");
        return smtpHost != null && fromEmail != null;
    }
    
    /**
     * Build bilingual welcome email HTML content
     */
    private String buildBilingualWelcomeEmailHtml(String username, Messages messagesJa, Messages messagesEn) {
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'></head><body>" +
               "<div style='margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #ccc;'>" +
               "<h2>" + messagesJa.at("email.welcome.subject") + "</h2>" +
               "<p>" + messagesJa.at("email.welcome.greeting", username) + "</p>" +
               "<p>" + messagesJa.at("email.welcome.message") + "</p>" +
               "<p>" + messagesJa.at("email.welcome.enjoy") + "</p>" +
               "<p>" + messagesJa.at("email.welcome.questions") + "</p>" +
               "<p>" + messagesJa.at("email.welcome.regards").replace("\\n", "<br>") + "</p>" +
               "</div>" +
               "<div>" +
               "<h2>" + messagesEn.at("email.welcome.subject") + "</h2>" +
               "<p>" + messagesEn.at("email.welcome.greeting", username) + "</p>" +
               "<p>" + messagesEn.at("email.welcome.message") + "</p>" +
               "<p>" + messagesEn.at("email.welcome.enjoy") + "</p>" +
               "<p>" + messagesEn.at("email.welcome.questions") + "</p>" +
               "<p>" + messagesEn.at("email.welcome.regards").replace("\\n", "<br>") + "</p>" +
               "</div>" +
               "</body></html>";
    }
    
    /**
     * Build bilingual welcome email text content
     */
    private String buildBilingualWelcomeEmailText(String username, Messages messagesJa, Messages messagesEn) {
        return messagesJa.at("email.welcome.subject") + "\n\n" +
               messagesJa.at("email.welcome.greeting", username) + "\n\n" +
               messagesJa.at("email.welcome.message") + "\n" +
               messagesJa.at("email.welcome.enjoy") + "\n\n" +
               messagesJa.at("email.welcome.questions") + "\n\n" +
               messagesJa.at("email.welcome.regards") + "\n\n" +
               "---\n\n" +
               messagesEn.at("email.welcome.subject") + "\n\n" +
               messagesEn.at("email.welcome.greeting", username) + "\n\n" +
               messagesEn.at("email.welcome.message") + "\n" +
               messagesEn.at("email.welcome.enjoy") + "\n\n" +
               messagesEn.at("email.welcome.questions") + "\n\n" +
               messagesEn.at("email.welcome.regards");
    }
    
    /**
     * Build bilingual password reset email HTML content
     */
    private String buildBilingualPasswordResetEmailHtml(String username, String resetUrl, Messages messagesJa, Messages messagesEn) {
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'></head><body>" +
               "<div style='margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #ccc;'>" +
               "<h2>" + messagesJa.at("email.password.reset.subject") + "</h2>" +
               "<p>" + messagesJa.at("email.password.reset.greeting", username) + "</p>" +
               "<p>" + messagesJa.at("email.password.reset.message") + "</p>" +
               "<p>" + messagesJa.at("email.password.reset.link") + "</p>" +
               "<p><a href=\"" + resetUrl + "\">" + resetUrl + "</a></p>" +
               "<p>" + messagesJa.at("email.password.reset.expire") + "</p>" +
               "<p>" + messagesJa.at("email.password.reset.ignore") + "</p>" +
               "<p>" + messagesJa.at("email.password.reset.regards").replace("\\n", "<br>") + "</p>" +
               "</div>" +
               "<div>" +
               "<h2>" + messagesEn.at("email.password.reset.subject") + "</h2>" +
               "<p>" + messagesEn.at("email.password.reset.greeting", username) + "</p>" +
               "<p>" + messagesEn.at("email.password.reset.message") + "</p>" +
               "<p>" + messagesEn.at("email.password.reset.link") + "</p>" +
               "<p><a href=\"" + resetUrl + "\">" + resetUrl + "</a></p>" +
               "<p>" + messagesEn.at("email.password.reset.expire") + "</p>" +
               "<p>" + messagesEn.at("email.password.reset.ignore") + "</p>" +
               "<p>" + messagesEn.at("email.password.reset.regards").replace("\\n", "<br>") + "</p>" +
               "</div>" +
               "</body></html>";
    }
    
    /**
     * Build bilingual password reset email text content
     */
    private String buildBilingualPasswordResetEmailText(String username, String resetUrl, Messages messagesJa, Messages messagesEn) {
        return messagesJa.at("email.password.reset.subject") + "\n\n" +
               messagesJa.at("email.password.reset.greeting", username) + "\n\n" +
               messagesJa.at("email.password.reset.message") + "\n" +
               messagesJa.at("email.password.reset.link") + "\n\n" +
               resetUrl + "\n\n" +
               messagesJa.at("email.password.reset.expire") + "\n" +
               messagesJa.at("email.password.reset.ignore") + "\n\n" +
               messagesJa.at("email.password.reset.regards") + "\n\n" +
               "---\n\n" +
               messagesEn.at("email.password.reset.subject") + "\n\n" +
               messagesEn.at("email.password.reset.greeting", username) + "\n\n" +
               messagesEn.at("email.password.reset.message") + "\n" +
               messagesEn.at("email.password.reset.link") + "\n\n" +
               resetUrl + "\n\n" +
               messagesEn.at("email.password.reset.expire") + "\n" +
               messagesEn.at("email.password.reset.ignore") + "\n\n" +
               messagesEn.at("email.password.reset.regards");
    }
}