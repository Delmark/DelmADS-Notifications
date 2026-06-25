package ru.delmark.dads.notifications.data.repository;

import org.apache.ibatis.annotations.Mapper;
import ru.delmark.dads.notifications.data.model.NotificationTopic;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NotificationTopicDAO {
    List<NotificationTopic> getNotificationTopics();
    Optional<NotificationTopic> getNotificationTopicByName(String topicName);
}
