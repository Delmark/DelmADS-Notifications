package ru.delmark.dads.notifications.mcp.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.mcp.annotation.McpToolParam;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;


@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageNotificationRequest implements McpRequest {

    @McpToolParam(
            description = "Required. Notification message that will be sent to Telegram." +
                          "Can be used for notification summary, supports emoji",
            required = true
    )
    String message;

    @McpToolParam(
            description = "Required. Notification topic, can be obtained by" +
                          " mcp tool 'get_available_notification_topic'",
            required = true
    )
    String notificationTopic;

    @McpToolParam(
            description = "Optional. List of attached files to notification message",
            required = false
    )
    List<AttachedFile> attachedFiles;
}
