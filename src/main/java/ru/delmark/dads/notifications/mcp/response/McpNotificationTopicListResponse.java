package ru.delmark.dads.notifications.mcp.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class McpNotificationTopicListResponse extends McpResponse {
    List<String> notificationTopics;

    public McpNotificationTopicListResponse(String status, String details, List<String> notificationTopics) {
        super(status, details);
        this.notificationTopics = notificationTopics;
    }
}
