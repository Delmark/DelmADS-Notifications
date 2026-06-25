package ru.delmark.dads.notifications.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.data.model.NotificationTopic;
import ru.delmark.dads.notifications.data.repository.NotificationTopicDAO;
import ru.delmark.dads.notifications.mcp.response.McpNotificationTopicListResponse;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class DataMcpBridge {

    private final NotificationTopicDAO topicDao;

    public McpNotificationTopicListResponse getAvailableTopics() {
        try {
            return new McpNotificationTopicListResponse(
                    "Success",
                    "Available notification topics is showed in response",
                    topicDao.getNotificationTopics().stream().map(NotificationTopic::getName).toList()
            );
        } catch (Exception e) {
            return new McpNotificationTopicListResponse(
                    "Failed",
                    "Failed operation due to server exception: " + e.getMessage(),
                    Collections.emptyList()
            );
        }
    }
}
