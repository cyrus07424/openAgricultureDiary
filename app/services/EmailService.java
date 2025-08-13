package services;

import play.Logger;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Service for sending emails using SMTP
 */
@Singleton
public class EmailService {
    
    private static final Logger.ALogger logger = Logger.of(EmailService.class);
    
    private final ClassLoaderExecutionContext executionContext;
    private final MailerClient mailerClient;
    private final String fromEmail;
    private final String fromName;
    
    @Inject
    public EmailService(ClassLoaderExecutionContext executionContext, MailerClient mailerClient) {
        this.executionContext = executionContext;
        this.mailerClient = mailerClient;
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
     * Send a welcome email to a new user
     */
    public CompletionStage<Boolean> sendWelcomeEmail(String toEmail, String username) {
        if (!isConfigured()) {
            logger.warn("Email service not configured. Skipping welcome email for: " + toEmail);
            return CompletableFuture.completedFuture(false);
        }
        
        String subject = "農業日誌へようこそ！";
        String htmlContent = buildWelcomeEmailHtml(username);
        String textContent = buildWelcomeEmailText(username);
        
        return sendEmail(toEmail, subject, htmlContent, textContent);
    }
    
    /**
     * Send a password reset email
     */
    public CompletionStage<Boolean> sendPasswordResetEmail(String toEmail, String username, String resetToken, String baseUrl) {
        if (!isConfigured()) {
            logger.warn("Email service not configured. Skipping password reset email for: " + toEmail);
            return CompletableFuture.completedFuture(false);
        }
        
        String subject = "パスワードリセットのご案内";
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        String htmlContent = buildPasswordResetEmailHtml(username, resetUrl);
        String textContent = buildPasswordResetEmailText(username, resetUrl);
        
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
    
    private String buildWelcomeEmailHtml(String username) {
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'></head><body>" +
               "<h2>農業日誌へようこそ！</h2>" +
               "<p>" + username + " さん、</p>" +
               "<p>農業日誌にご登録いただき、ありがとうございます。</p>" +
               "<p>これから農業活動の記録をお楽しみください。</p>" +
               "<p>何かご質問がございましたら、お気軽にお問い合わせください。</p>" +
               "<p>よろしくお願いいたします。<br>農業日誌チーム</p>" +
               "</body></html>";
    }
    
    private String buildWelcomeEmailText(String username) {
        return "農業日誌へようこそ！\n\n" +
               username + " さん、\n\n" +
               "農業日誌にご登録いただき、ありがとうございます。\n" +
               "これから農業活動の記録をお楽しみください。\n\n" +
               "何かご質問がございましたら、お気軽にお問い合わせください。\n\n" +
               "よろしくお願いいたします。\n農業日誌チーム";
    }
    
    private String buildPasswordResetEmailHtml(String username, String resetUrl) {
        return "<!DOCTYPE html>" +
               "<html><head><meta charset='UTF-8'></head><body>" +
               "<h2>パスワードリセットのご案内</h2>" +
               "<p>" + username + " さん、</p>" +
               "<p>パスワードリセットのリクエストを受け付けました。</p>" +
               "<p>下記のURLをクリックして、新しいパスワードを設定してください：</p>" +
               "<p><a href=\"" + resetUrl + "\">" + resetUrl + "</a></p>" +
               "<p>このリンクは24時間有効です。</p>" +
               "<p>もしこのリクエストにお心当たりがない場合は、このメールを無視してください。</p>" +
               "<p>よろしくお願いいたします。<br>農業日誌チーム</p>" +
               "</body></html>";
    }
    
    private String buildPasswordResetEmailText(String username, String resetUrl) {
        return "パスワードリセットのご案内\n\n" +
               username + " さん、\n\n" +
               "パスワードリセットのリクエストを受け付けました。\n" +
               "下記のURLをクリックして、新しいパスワードを設定してください：\n\n" +
               resetUrl + "\n\n" +
               "このリンクは24時間有効です。\n" +
               "もしこのリクエストにお心当たりがない場合は、このメールを無視してください。\n\n" +
               "よろしくお願いいたします。\n農業日誌チーム";
    }
}