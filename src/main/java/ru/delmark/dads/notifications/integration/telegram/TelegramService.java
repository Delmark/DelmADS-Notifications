package ru.delmark.dads.notifications.integration.telegram;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.delmark.dads.notifications.data.dto.NotificationTopicFilter;
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
import java.util.Comparator;
import java.util.HashSet;
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
                        false,
                        OffsetDateTime.now()
                )
        );
    }

    private static final Comparator<TelegramNotificationTopicsInfo> topicInfoComparator = Comparator
            .comparing(TelegramNotificationTopicsInfo::getDisplayPriority).reversed()
            .thenComparing(topicInfo ->
                    StringUtils.firstNonEmpty(
                            topicInfo.getAlias(),
                            topicInfo.getTopic()
                    ).length()
            );

    public List<TelegramNotificationTopicsInfo> getNotificationTopics(Long userId) {
        BotUser user = getUser(userId);

        List<NotificationTopic> topics = getTopics();
        topics.removeIf(isNotAvailableForAccess(user.getAccessLevel()));
        if (CollectionUtils.isEmpty(topics)) {
            return Collections.emptyList();
        }

        Set<NotificationSubscription> userSubs = getUserSubscribedTopics(user.getId());
        Set<Long> subIds = userSubs.stream().map(NotificationSubscription::getTopicId).collect(Collectors.toSet());

        return topics.stream()
                .map(topic ->
                        new TelegramNotificationTopicsInfo(
                                topic.getName(),
                                topic.getAlias(),
                                topic.getDescription(),
                                topic.getDisplayPriority(),
                                subIds.contains(topic.getId())
                        )
                )
                .sorted(topicInfoComparator)
                .collect(Collectors.toList());
    }

    private Predicate<NotificationTopic> isNotAvailableForAccess(AccessMode accessMode) {
        return topic -> topic.getMinAccessLevel().getLevel() > accessMode.getLevel();
    }

    public void subscribeToNotification(String topicName, Long userId) {
        BotUser user = getUser(userId);

        NotificationTopic topic = getTopic(topicName);

        if (isNotAvailableForAccess(user.getAccessLevel()).test(topic)) {
            throw new TelegramCommandHandleException("У вас нет доступа к данной рассылке");
        }

        Set<Long> userSubs = getUserSubscribedTopics(user.getId()).stream()
                .map(NotificationSubscription::getTopicId).collect(Collectors.toSet());

        if (userSubs.contains(topic.getId())) {
            throw new TelegramCommandHandleException("Вы уже подписаны на данную рассылку");
        }

        notificationSubscriptionDAO.subscribe(
                new NotificationSubscription(
                        user.getId(),
                        topic.getId(),
                        user.getPreferredSilentMode(),
                        OffsetDateTime.now()
                )
        );
    }

    public List<NotificationTopic> getTopics(NotificationTopicFilter filter) {
        return notificationTopicDAO.getNotificationTopics(filter);
    }

    public List<NotificationTopic> getTopics() {
        return notificationTopicDAO.getNotificationTopics(new NotificationTopicFilter());
    }

    public void updateSubscriptionSilentMode(Long topicId, Long userId, boolean silent) {
        notificationSubscriptionDAO.updateSilentMode(userId, topicId, silent);
    }

    public void unsubscribeFromNotification(String topicName, Long userId) {
        BotUser user = getUser(userId);
        NotificationTopic topic = getTopic(topicName);

        Set<Long> userSubs = getUserSubscribedTopics(user.getId()).stream()
                .map(NotificationSubscription::getTopicId).collect(Collectors.toSet());

        if (!userSubs.contains(topic.getId())) {
            throw new TelegramCommandHandleException("Вы не подписаны на данную рассылку");
        }

        notificationSubscriptionDAO.unsubscribe(topic.getId(), userId);
    }

    private NotificationTopic getTopic(String topicName) {
        return notificationTopicDAO.getNotificationTopic(topicName)
                .orElseThrow(() -> new TelegramCommandHandleException("Указанный тэг рассылки не найден"));
    }

    public void updateUserSilentMode(Long userId, boolean silent) {
        botUserDAO.updateUserSilentMode(userId, silent);
    }

    public BotUser getUser(Long userId) {
        return botUserDAO.findById(userId)
                .orElseThrow(() -> new TelegramCommandHandleException("Пользователь не найден"));
    }

    public Set<NotificationSubscription> getUserSubscribedTopics(Long userId) {
        return new HashSet<>(notificationSubscriptionDAO.findUserSubscriptions(userId));
    }
}
