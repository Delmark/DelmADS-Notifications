package ru.delmark.dads.notifications.data.repository;

import org.apache.ibatis.annotations.Mapper;
import ru.delmark.dads.notifications.data.model.NotificationSubscription;

import java.util.List;

@Mapper
public interface NotificationSubscriptionDAO {
    List<NotificationSubscription> findUserSubscriptions(Long userId);
    void subscribe(NotificationSubscription notificationSubscription);
    void unsubscribe(String topicName, Long userId);
}
