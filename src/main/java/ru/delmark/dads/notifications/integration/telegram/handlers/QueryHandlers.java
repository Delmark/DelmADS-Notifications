package ru.delmark.dads.notifications.integration.telegram.handlers;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.CallbackHandler;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.requests.edit.EditMessageReplyMarkup;
import io.github.natanimn.telebof.requests.edit.EditMessageText;
import io.github.natanimn.telebof.requests.send.SendMessage;
import io.github.natanimn.telebof.spring.Bot;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import ru.delmark.dads.notifications.data.dto.NotificationTopicFilter;
import ru.delmark.dads.notifications.data.model.BotUser;
import ru.delmark.dads.notifications.data.model.NotificationSubscription;
import ru.delmark.dads.notifications.data.model.NotificationTopic;
import ru.delmark.dads.notifications.exception.TelegramCommandHandleException;
import ru.delmark.dads.notifications.integration.telegram.MessageBuilder;
import ru.delmark.dads.notifications.integration.telegram.TelegramService;
import ru.delmark.dads.notifications.integration.telegram.dto.TelegramNotificationTopicsInfo;
import ru.delmark.dads.notifications.integration.telegram.dto.TopicOps;
import ru.delmark.dads.notifications.integration.telegram.handlers.filters.GlobalSilentModeFilter;
import ru.delmark.dads.notifications.integration.telegram.handlers.filters.TopicSettingsFilter;
import ru.delmark.dads.notifications.integration.telegram.handlers.filters.TopicSilentModeEditFilter;
import ru.delmark.dads.notifications.integration.telegram.handlers.filters.TopicSubControlFilter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Bot
@RequiredArgsConstructor
@Slf4j
public class QueryHandlers {

    private final CommonHandlerOperations ops;
    private final TelegramService telegramService;

    @CallbackHandler(data = "help_callback")
    public void openHelpCallback(BotContext ctx, CallbackQuery callback) {
        cleanOldMarkup(ctx, callback);
        ops.sendHelpMessage(ctx, callback.getMessage().getChat().getId());
        ctx.answerCallbackQuery(callback.getId()).exec();
    }

    @CallbackHandler(data = "topic_list")
    public void openTopicsCallback(BotContext ctx, CallbackQuery callback) {
        cleanOldMarkup(ctx, callback);
        ops.sendUserTopicActions(ctx, callback.getMessage().getChat().getId(), callback.getFrom().getId());
        ctx.answerCallbackQuery(callback.getId()).exec();
    }

    @CallbackHandler(data = "settings")
    public void openSettingsCallback(BotContext ctx, CallbackQuery callback) {
        cleanOldMarkup(ctx, callback);
        ops.openSettingsPanel(ctx, callback.getFrom().getId(), callback.getMessage().getChat().getId());
        ctx.answerCallbackQuery(callback.getId()).exec();
    }

    @CallbackHandler(data = "topic_config")
    public void openTopicConfigCallback(BotContext ctx, CallbackQuery callback) {
        cleanOldMarkup(ctx, callback);

        Long recipientChatId = callback.getMessage().getChat().getId();
        Set<NotificationSubscription> userSubs = telegramService.getUserSubscribedTopics(callback.getFrom().getId());
        if (CollectionUtils.isNotEmpty(userSubs)) {
            ctx.sendMessage(recipientChatId, "Вы не подписаны на какие-либо рассылки").exec();
            ctx.answerCallbackQuery(callback.getId()).exec();
            return;
        }

        String messageText = "Выберите рассылки которые хотите настроить";
        SendMessage messageReq = ctx.sendMessage(recipientChatId, messageText);
        messageReq.replyMarkup(buildTopicConfigList(userSubs));
        messageReq.exec();

        ctx.answerCallbackQuery(callback.getId()).exec();
    }

    private InlineKeyboardMarkup buildTopicConfigList(Set<NotificationSubscription> userSubs) {
        List<Long> topicIds = userSubs.stream().map(NotificationSubscription::getTopicId).collect(Collectors.toList());

        Map<Long, String> topicNames = telegramService.getTopics(
                NotificationTopicFilter.builder()
                        .topicIds(topicIds)
                        .build()
        ).stream().collect(Collectors.toMap(NotificationTopic::getId, NotificationTopic::getName));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setRowWidth(1);
        userSubs.forEach(sub -> {
                String topicName = topicNames.get(sub.getTopicId());
                inlineKeyboardMarkup.addKeyboard(
                        new InlineKeyboardButton(
                                topicName,
                                "topic_settings_" + sub.getTopicId()
                        )
                );
            }
        );

        return inlineKeyboardMarkup;
    }

    @CallbackHandler(filter = GlobalSilentModeFilter.class)
    public void handleGlobalSilentModeCallback(BotContext ctx, CallbackQuery callback) {
        cleanOldMarkup(ctx, callback);
        BotUser user = telegramService.getUser(callback.getFrom().getId());

        String cbData = callback.getData();
        boolean newSilentModeStatus = Boolean.parseBoolean(cbData.replace("silent_mode_", ""));

        telegramService.updateUserSilentMode(user.getId(), newSilentModeStatus);

        ctx.sendMessage(callback.getMessage().getChat().getId(), "Предпочитаемый режим отправки уведомлений обновлён");
        ctx.answerCallbackQuery(callback.getId()).exec();
    }

    @CallbackHandler(filter = TopicSettingsFilter.class)
    public void handleTopicSettingsCallback(BotContext ctx, CallbackQuery callback) {
        cleanOldMarkup(ctx, callback);
        String cbData = callback.getData();
        Long topicId = Long.parseLong(cbData.replace("topic_settings_", ""));
        Long userId = callback.getFrom().getId();

        try {
            openTopicSettings(ctx, topicId, userId, callback.getMessage().getChat().getId());
        } catch (TelegramCommandHandleException e) {
            log.error(e.getMessage(), e);
            ctx.sendMessage(callback.getMessage().getChat().getId(), "Произошла непредвиденная ошибка: " + e.getMessage());
        } finally {
            ctx.answerCallbackQuery(callback.getId()).exec();
        }
    }

    @CallbackHandler(filter = TopicSilentModeEditFilter.class)
    public void handleTopicSilentModeEditCallback(BotContext ctx, CallbackQuery callback) {
        cleanOldMarkup(ctx, callback);
        String cbData = callback.getData();

        String[] postfixData = cbData.replace("topic_silent_mode_", "").split("_");
        Long topicId = Long.parseLong(postfixData[0]);
        boolean newSilentModeStatus = Boolean.parseBoolean(postfixData[1]);

        Long userId = callback.getFrom().getId();
        telegramService.updateSubscriptionSilentMode(topicId, userId, newSilentModeStatus);

        try {
            openTopicSettings(ctx, topicId, userId, callback.getMessage().getChat().getId());
        } catch (TelegramCommandHandleException e) {
            log.error(e.getMessage(), e);
            ctx.sendMessage(callback.getMessage().getChat().getId(), "Произошла непредвиденная ошибка: " + e.getMessage());
        } finally {
            ctx.answerCallbackQuery(callback.getId()).exec();
        }
    }

    private void openTopicSettings(BotContext ctx, Long topicId, Long userId, Long recipientChatId) {
        NotificationTopic topic = telegramService.getTopics(
                        NotificationTopicFilter.builder()
                                .topicIds(Collections.singletonList(topicId))
                                .build())
                .stream()
                .findFirst()
                .orElseThrow(() -> new TelegramCommandHandleException("Выбранная рассылка не найдена, попробуйте позже"));

        NotificationSubscription subscription = telegramService.getUserSubscribedTopics(userId).stream()
                .filter(sub -> sub.getTopicId().equals(topicId))
                .findFirst().orElseThrow(() -> new TelegramCommandHandleException("Вы не подписаны на данную рассылку"));

        String message = MessageBuilder.getTopicSettings(topic, subscription);
        SendMessage messageReq = ctx.sendMessage(recipientChatId, message);
        messageReq.replyMarkup(buildTopicSettingsMarkup(subscription));
        messageReq.exec();
    }

    private InlineKeyboardMarkup buildTopicSettingsMarkup(NotificationSubscription subscription) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setRowWidth(1);

        String silentModeLabel = (subscription.getSilentMode()) ? "Выключить тихую отправку" : "Включить тихую отправку";
        String callbackPostfix = subscription.getTopicId() + "_" + !subscription.getSilentMode();
        inlineKeyboardMarkup.addKeyboard(
                new InlineKeyboardButton(silentModeLabel, "topic_silent_mode_" + callbackPostfix)
        );

        return inlineKeyboardMarkup;
    }

    @CallbackHandler(filter = TopicSubControlFilter.class)
    public void handleTopicOperationCallback(BotContext ctx, CallbackQuery callback) {
        // извлекаем инфу об операции колбэка над топиком
        String data = callback.getData();

        String topicOp = data.substring(0, data.indexOf("_") + 1);
        TopicOps operation = TopicOps.fromOpTag(topicOp);
        String topicTag = callback.getData()
                .replaceFirst("sub_|unsub_", "");

        Long chatId = callback.getMessage().getChat().getId();
        Long userId = callback.getFrom().getId();
        switch (operation) {
            case SUBSCRIBE   -> ops.subscribeToTopic(ctx, topicTag, chatId, userId, false);
            case UNSUBSCRIBE -> ops.unsubscribeFromTopic(ctx, topicTag, chatId, userId, false);
        }

        // обновляем старое сообщение
        List<TelegramNotificationTopicsInfo> availableTopics = telegramService.getNotificationTopics(userId);

        String newMessage = "К сожалению, на данный момент нет рассылок на которые вы можете подписаться";
        InlineKeyboardMarkup markup = null;
        if (CollectionUtils.isNotEmpty(availableTopics)) {
            newMessage = ops.buildTopicListMessage(availableTopics);
            markup = ops.buildTopicControlMarkup(availableTopics);
        }

        Integer messageId = callback.getMessage().getMessageId();
        EditMessageText messageEdit = ctx
                .editMessageText(chatId, newMessage, messageId)
                .parseMode(ParseMode.MARKDOWNV2);
        if (markup != null) {
            messageEdit = messageEdit.replyMarkup(markup);
        }
        messageEdit.exec();
        ctx.answerCallbackQuery(callback.getId()).exec();
    }

    private void cleanOldMarkup(BotContext ctx, CallbackQuery callback) {
        Message queryMessage = callback.getMessage();
        EditMessageReplyMarkup oldMarkUp = ctx
                .editMessageReplyMarkup(
                        queryMessage.getChat().getId(),
                        queryMessage.getMessageId()
                );
        oldMarkUp.replyMarkup(null); // null - стирает старую клавиатуру
        oldMarkUp.exec();
    }
}
