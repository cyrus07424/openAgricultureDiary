package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Service for sending notifications to Slack via webhook
 */
@Singleton
public class SlackNotificationService {
    
    private final WSClient wsClient;
    private final String webhookUrl;
    private final ObjectMapper objectMapper;
    
    @Inject
    public SlackNotificationService(WSClient wsClient) {
        this.wsClient = wsClient;
        this.webhookUrl = System.getenv("SLACK_WEBHOOK_URL");
        this.objectMapper = new ObjectMapper();
        
        if (this.webhookUrl == null) {
            play.Logger.of(SlackNotificationService.class).warn("SLACK_WEBHOOK_URL environment variable not set. Slack notifications will be disabled.");
        }
    }
    
    /**
     * Send notification for user registration
     */
    public CompletionStage<Boolean> notifyUserRegistration(User user, Http.Request request) {
        if (webhookUrl == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        String message = String.format("🆕 *新規ユーザー登録* - %s (%s)", 
            user.getUsername(), user.getEmail());
        
        return sendNotification(message, user, request);
    }
    
    /**
     * Send notification for user login
     */
    public CompletionStage<Boolean> notifyUserLogin(User user, Http.Request request) {
        if (webhookUrl == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        String message = String.format("🔐 *ユーザーログイン* - %s", user.getUsername());
        
        return sendNotification(message, user, request);
    }
    
    /**
     * Send notification for data creation
     */
    public CompletionStage<Boolean> notifyDataCreation(String dataType, String dataName, User user, Http.Request request) {
        if (webhookUrl == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        String message = String.format("➕ *%s作成* - %s (作成者: %s)", 
            dataType, dataName, user.getUsername());
        
        return sendNotification(message, user, request);
    }
    
    /**
     * Send notification for data update
     */
    public CompletionStage<Boolean> notifyDataUpdate(String dataType, String dataName, User user, Http.Request request) {
        if (webhookUrl == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        String message = String.format("✏️ *%s更新* - %s (更新者: %s)", 
            dataType, dataName, user.getUsername());
        
        return sendNotification(message, user, request);
    }
    
    /**
     * Send notification for data deletion
     */
    public CompletionStage<Boolean> notifyDataDeletion(String dataType, String dataName, User user, Http.Request request) {
        if (webhookUrl == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        String message = String.format("🗑️ *%s削除* - %s (削除者: %s)", 
            dataType, dataName, user.getUsername());
        
        return sendNotification(message, user, request);
    }
    
    /**
     * Send notification to Slack webhook
     */
    private CompletionStage<Boolean> sendNotification(String message, User user, Http.Request request) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("text", message);
            
            // Add user details
            ObjectNode userField = objectMapper.createObjectNode();
            userField.put("title", "ユーザー情報");
            userField.put("value", String.format("ID: %s\nユーザー名: %s\nメール: %s", 
                user.getId(), user.getUsername(), user.getEmail()));
            userField.put("short", true);
            
            // Add request details
            ObjectNode requestField = objectMapper.createObjectNode();
            requestField.put("title", "リクエスト情報");
            requestField.put("value", String.format("IP: %s\nUser-Agent: %s", 
                getClientIpAddress(request), 
                request.header("User-Agent").orElse("Unknown")));
            requestField.put("short", true);
            
            // Create attachment with fields
            ObjectNode attachment = objectMapper.createObjectNode();
            attachment.put("color", "good");
            attachment.putArray("fields").add(userField).add(requestField);
            
            payload.putArray("attachments").add(attachment);
            
            return wsClient.url(webhookUrl)
                .setContentType("application/json")
                .post(payload)
                .thenApplyAsync(response -> {
                    if (response.getStatus() == 200) {
                        play.Logger.of(SlackNotificationService.class).info("Slack notification sent successfully");
                        return true;
                    } else {
                        play.Logger.of(SlackNotificationService.class).warn("Failed to send Slack notification. Status: " + response.getStatus());
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    play.Logger.of(SlackNotificationService.class).error("Error sending Slack notification", throwable);
                    return false;
                });
                
        } catch (Exception e) {
            play.Logger.of(SlackNotificationService.class).error("Error creating Slack notification payload", e);
            return CompletableFuture.completedFuture(false);
        }
    }
    
    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(Http.Request request) {
        // Check for X-Forwarded-For header (common in load balancers)
        String xForwardedFor = request.header("X-Forwarded-For").orElse(null);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check for X-Real-IP header
        String xRealIp = request.header("X-Real-IP").orElse(null);
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fall back to remote address
        return request.remoteAddress();
    }
}