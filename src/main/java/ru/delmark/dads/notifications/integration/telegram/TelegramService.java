package ru.delmark.dads.notifications.integration.telegram;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.delmark.dads.notifications.data.model.AccessMode;
import ru.delmark.dads.notifications.data.model.BotUser;
import ru.delmark.dads.notifications.data.model.Chat;
import ru.delmark.dads.notifications.data.model.NotificationSubscription;
import ru.delmark.dads.notifications.data.model.NotificationTopic;
import ru.delmark.dads.notifications.data.repository.BotUserDAO;
import ru.delmark.dads.notifications.data.repository.ChatDAO;
import ru.delmark.dads.notifications.data.repository.NotificationSubscriptionDAO;
import ru.delmark.dads.notifications.data.repository.NotificationTopicDAO;
import ru.delmark.dads.notifications.exception.TelegramCommandHandleException;
import ru.delmark.dads.notifications.integration.telegram.dto.TelegramNotificationTopicsInfo;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelegramService {

    private final BotUserDAO botUserDAO;
    private final NotificationTopicDAO notificationTopicDAO;
    private final NotificationSubscriptionDAO notificationSubscriptionDAO;
    private final ChatDAO chatDAO;

    @Cacheable(value = "registeredChat", unless = "#result.equals(false)")
    public boolean registerNewChatIfNew(Long chatId, Long userId) {
        if (!chatDAO.chatExists(chatId)) {
            chatDAO.insertChat(new Chat(chatId, userId));
            return false;
        }
        return true;
    }

    @Cacheable(value = "registeredUser", unless = "#result.equals(false)")
    public Boolean isUserRegistered(Long userId) {
        return botUserDAO.findById(userId).isPresent();
    }

    public void registerNewUser(Long userId, String username) {
        botUserDAO.save(
                new BotUser(
                        userId,
                        username,
                        AccessMode.PUBLIC,
                        OffsetDateTime.now()
                )
        );
    }

    public List<TelegramNotificationTopicsInfo> getNotificationTopics(Long userId) {
        BotUser user = getUser(userId);

        List<NotificationTopic> topics = notificationTopicDAO.getNotificationTopics();
        topics.removeIf(isNotAvailableForAccess(user.getAccessLevel()));
        if (CollectionUtils.isEmpty(topics)) {
            return Collections.emptyList();
        }

        Set<String> userSubs = getUserSubscriptions(user.getId());

        return topics.stream()
                .map(topic ->
                        new TelegramNotificationTopicsInfo(
                                topic.getName(),
                                userSubs.contains(topic.getName())
                        )
                )
                .collect(Collectors.toList());
    }

    private Predicate<NotificationTopic> isNotAvailableForAccess(AccessMode accessMode) {
        return topic -> topic.getMinAccessLevel().getLevel() > accessMode.getLevel();
    }

    public void subscribeToNotification(String topicName, Long userId) {
        BotUser user = getUser(userId);

        NotificationTopic topic = notificationTopicDAO.getNotificationTopicByName(topicName)
                .orElseThrow(() -> new TelegramCommandHandleException("Указанный тэг рассылки не найден"));

        if (isNotAvailableForAccess(user.getAccessLevel()).test(topic)) {
            throw new TelegramCommandHandleException("У вас нет доступа к данной рассылке");
        }

        Set<String> userSubs = getUserSubscriptions(user.getId());
        if (userSubs.contains(topic.getName())) {
            throw new TelegramCommandHandleException("Вы уже подписаны на данную рассылку");
        }

        notificationSubscriptionDAO.subscribe(
                new NotificationSubscription(
                        user.getId(),
                        topic.getName(),
                        OffsetDateTime.now()
                )
        );
    }

    public void unsubscribeFromNotification(String topicName, Long userId) {
        BotUser user = getUser(userId);

        Set<String> userSubs = getUserSubscriptions(user.getId());
        if (!userSubs.contains(topicName)) {
            throw new TelegramCommandHandleException("Вы не подписаны на данную рассылку");
        }

        notificationSubscriptionDAO.unsubscribe(topicName, userId);
    }

    private BotUser getUser(Long userId) {
        return botUserDAO.findById(userId)
                .orElseThrow(() -> new TelegramCommandHandleException("Пользователь не найден"));
    }

    private Set<String> getUserSubscriptions(Long userId) {
        return notificationSubscriptionDAO.findUserSubscriptions(userId)
                .stream().map(NotificationSubscription::getTopicName).collect(Collectors.toSet());
    }
}
