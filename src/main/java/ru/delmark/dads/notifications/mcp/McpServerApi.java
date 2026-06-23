package ru.delmark.dads.notifications.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.mcp.response.BaseResponse;
import ru.delmark.dads.notifications.subscribtion.McpEvent;
import ru.delmark.dads.notifications.subscribtion.McpEventPublisher;

@Component
@RequiredArgsConstructor
public class McpServerApi {

    private final McpEventPublisher eventPublisher;

    @McpTool(
            name = "send_message_notification",
            description = "Sends message notification to user application",
            generateOutputSchema = true
    )
    public BaseResponse sendMessageNotification(
            @McpToolParam(description = "Message text") String notificationText,
            @McpToolParam(description = "Notification Type") String notificationType
    ) {
        return new BaseResponse(
                eventPublisher.publish(
                        new McpEvent(notificationType, notificationText)
                ), null
        );
    }
}
