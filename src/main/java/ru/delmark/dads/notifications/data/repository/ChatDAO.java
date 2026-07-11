package ru.delmark.dads.notifications.data.repository;

import org.apache.ibatis.annotations.Mapper;
import ru.delmark.dads.notifications.data.dto.NotificationRecipientChat;
import ru.delmark.dads.notifications.data.model.Chat;

import java.util.List;

@Mapper
public interface ChatDAO {
    boolean chatExists(Long id);
    void insertChat(Chat chat);
    List<NotificationRecipientChat> getRecipientChatsForNotification(String topicName);
}
