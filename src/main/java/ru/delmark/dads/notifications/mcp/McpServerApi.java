package ru.delmark.dads.notifications.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.data.DataMcpBridge;
import ru.delmark.dads.notifications.exception.McpEventHandleException;
import ru.delmark.dads.notifications.integration.telegram.TelegramMcpBridge;
import ru.delmark.dads.notifications.mcp.request.MessageNotificationRequest;
import ru.delmark.dads.notifications.mcp.response.McpNotificationTopicListResponse;
import ru.delmark.dads.notifications.mcp.response.McpResponse;


@Component
@RequiredArgsConstructor
public class McpServerApi {

    private final TelegramMcpBridge telegramMcpBridge;
    private final DataMcpBridge dataMcpBridge;

    @McpTool(
            name = "get_available_notification_topics",
            description = "Get available notification topics for messages"
    )
    public McpNotificationTopicListResponse getAvailableNotificationThemes() {
        return dataMcpBridge.getAvailableTopics();
    }

    @McpTool(
            name = "send_message_notification",
            description = "Sends message notification to user application",
            generateOutputSchema = true
    )
    public McpResponse sendMessageNotification(
            @McpToolParam(description = "Message Notification Request")
            MessageNotificationRequest messageNotificationRequest
    ) {
        try {
            return telegramMcpBridge.sendNotification(messageNotificationRequest);
        } catch (McpEventHandleException e) {
            return new McpResponse(
                    "Send failed",
                    "Send failed due %s".formatted(e.getMessage())
            );
        } catch (Exception e) {
            return new McpResponse(
                    "Server error",
                    "Internal server error, debug: " + e.getMessage()
            );
        }
    }

}
