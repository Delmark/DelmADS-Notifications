package ru.delmark.dads.notifications.data.repository;

import org.apache.ibatis.annotations.Mapper;
import ru.delmark.dads.notifications.data.dto.NotificationTopicFilter;
import ru.delmark.dads.notifications.data.model.NotificationTopic;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NotificationTopicDAO {
    List<NotificationTopic> getNotificationTopics(NotificationTopicFilter filter);
    Optional<NotificationTopic> getNotificationTopic(String topicKey);
}
