package ru.delmark.dads.notifications.integration.telegram.handlers;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.requests.send.SendMessage;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.data.model.BotUser;
import ru.delmark.dads.notifications.exception.TelegramCommandHandleException;
import ru.delmark.dads.notifications.integration.telegram.TelegramService;
import ru.delmark.dads.notifications.integration.telegram.MessageBuilder;
import ru.delmark.dads.notifications.integration.telegram.dto.TelegramNotificationTopicsInfo;
import ru.delmark.dads.notifications.integration.telegram.dto.TopicOps;
import ru.delmark.dads.notifications.utils.MarkdownV2Escaper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommonHandlerOperations {

    private final TelegramService telegramService;

    public void sendUserTopicActions(BotContext ctx, Long chatId, Long userId) {
        List<TelegramNotificationTopicsInfo> availableTopics = telegramService.getNotificationTopics(userId);

        if (CollectionUtils.isEmpty(availableTopics)) {
            ctx.sendMessage(chatId,"К сожалению, на данный момент нет рассылок на которые вы можете подписаться").exec();
            return;
        }

        SendMessage send = buildBaseMessage(ctx, chatId, buildTopicListMessage(availableTopics));
        send.replyMarkup(buildTopicControlMarkup(availableTopics));
        send.exec();
    }

    public String buildTopicListMessage(List<TelegramNotificationTopicsInfo> availableTopics) {
        StringBuilder messageText = new StringBuilder();
        messageText.append("Предоставлен следующий список доступных рассылок в формате *Название \\- Статус \\- Тэг*: \n");
        availableTopics.forEach(topic -> {
            messageText.append(
                    "\n%s \\- %s".formatted(
                            topic.isUserSubscribed()
                                    ? "✅ Подписан"
                                    : "❌ Не подписан",
                            MarkdownV2Escaper.escape(topic.getAlias())
                    )
            );
            if (StringUtils.isNotBlank(topic.getTopic())) {
                messageText.append(" \\(%s\\) "
                        .formatted(MarkdownV2Escaper.escape(topic.getTopic())));
            }
            if (StringUtils.isNotBlank(topic.getDescription())) {
                messageText.append(" \\- ").append(MarkdownV2Escaper.escape(topic.getDescription()));
            }
        });
        return messageText.toString();
    }

    public InlineKeyboardMarkup buildTopicControlMarkup(List<TelegramNotificationTopicsInfo> availableTopics) {
        InlineKeyboardMarkup topicControlMarkup = new InlineKeyboardMarkup();
        topicControlMarkup.setRowWidth(1);
        topicControlMarkup.addKeyboard(
                availableTopics.stream()
                        .map(topic -> {
                            String action = topic.isUserSubscribed() ? "❌ отписаться" : "✍️ подписаться";
                            String buttonText = "%s - %s".formatted(topic.getAlias(), action);

                            TopicOps topicOperation = topic.isUserSubscribed()
                                    ? TopicOps.UNSUBSCRIBE : TopicOps.SUBSCRIBE;
                            String callback = topicOperation.getFormattedCallbackData(topic.getTopic());

                            return new InlineKeyboardButton(buttonText, callback);
                        })
                        .toArray(InlineKeyboardButton[]::new)
        );
        return topicControlMarkup;
    }

    public void sendHelpMessage(BotContext ctx, Long chatId) {
        String startMessage = MessageBuilder.getHelpMessage();
        SendMessage messageToSend = buildBaseMessage(ctx, chatId, startMessage);
        messageToSend.replyMarkup(buildDefaultMenuMarkup());
        messageToSend.exec();
    }

    public void subscribeToTopic(BotContext ctx, String topicName, Long chatId, Long userId, boolean sendNotification) {
        try {
            telegramService.subscribeToNotification(topicName, userId);
            if (sendNotification) {
                ctx.sendMessage(chatId, "Успешно подписались на %s".formatted(topicName)).exec();
            }
        } catch (TelegramCommandHandleException e) {
            String errorMessage = "Произошла ошибка %s".formatted(e.getMessage());
            ctx.sendMessage(chatId, errorMessage).exec();
        }
    }

    public void unsubscribeFromTopic(BotContext ctx, String topicName, Long chatId, Long userId, boolean sendNotification) {
        try {
            telegramService.unsubscribeFromNotification(topicName, userId);
            if (sendNotification) {
                ctx.sendMessage(chatId, "Успешно отписались от %s".formatted(topicName)).exec();
            }
        } catch (TelegramCommandHandleException e) {
            String errorMessage = "Произошла ошибка %s".formatted(e.getMessage());
            ctx.sendMessage(chatId, errorMessage).exec();
        }
    }

    public void openSettingsPanel(BotContext ctx, Long userId, Long recipientChatId) {
        BotUser user = telegramService.getUser(userId);
        boolean userPreferredSilentMode = user.getPreferredSilentMode();

        String settingsMessage = MessageBuilder.getSettingsMessage(userPreferredSilentMode);
        SendMessage messageRequest = ctx.sendMessage(recipientChatId, settingsMessage);
        messageRequest.replyMarkup(buildSettingsMarkup(userPreferredSilentMode));
        messageRequest.exec();
    }

    public InlineKeyboardMarkup buildSettingsMarkup(boolean userPreferredSilentMode) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        String globalSilentModeLabel = (!userPreferredSilentMode)
                ? "\uD83D\uDD07 Предпочитать тихую отправку уведомлений"
                : "\uD83D\uDD0A Предпочитать отправку с push уведомлениями";

        keyboardMarkup.setRowWidth(1);
        keyboardMarkup.addKeyboard(
                new InlineKeyboardButton(globalSilentModeLabel, "silent_mode_" + !userPreferredSilentMode),
                new InlineKeyboardButton("Настройка получения рассылок", "topic_config")
        );
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup buildDefaultMenuMarkup() {
        InlineKeyboardMarkup menuMarkup = new InlineKeyboardMarkup();
        menuMarkup.setRowWidth(1);
        menuMarkup.addKeyboard(
                new InlineKeyboardButton("Помощь", "help_callback"),
                new InlineKeyboardButton("Список рассылок", "topic_list"),
                new InlineKeyboardButton("Настройки", "settings")
        );
        return menuMarkup;
    }

    public SendMessage buildBaseMessage(BotContext botContext, Long chatId, String messageText) {
        return botContext.sendMessage(chatId, messageText).parseMode(ParseMode.MARKDOWNV2);
    }
}
