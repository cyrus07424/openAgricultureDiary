import static org.junit.Assert.*;

import org.junit.Test;
import play.libs.ws.WSClient;
import services.SlackNotificationService;

import static org.mockito.Mockito.*;

public class SlackNotificationServiceTest {

    @Test
    public void testSlackNotificationServiceInitializationWithoutWebhookUrl() {
        // Mock WSClient
        WSClient mockWSClient = mock(WSClient.class);
        
        // Create service instance without SLACK_WEBHOOK_URL environment variable
        SlackNotificationService slackService = new SlackNotificationService(mockWSClient);
        
        assertNotNull("SlackNotificationService should be instantiated", slackService);
        
        // The service should handle missing webhook URL gracefully
        // and not throw exceptions during instantiation
    }
}