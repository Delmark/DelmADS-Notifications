package ru.delmark.dads.notifications.integration.telegram.handlers;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.requests.send.SendMessage;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.exception.TelegramCommandHandleException;
import ru.delmark.dads.notifications.integration.telegram.TelegramService;
import ru.delmark.dads.notifications.integration.telegram.dto.MessageConstants;
import ru.delmark.dads.notifications.integration.telegram.dto.TelegramNotificationTopicsInfo;
import ru.delmark.dads.notifications.integration.telegram.dto.TopicOps;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommonHandlerOperations {

    private final TelegramService telegramService;

    public void sendUserTopicActions(BotContext ctx, Long chatId, Long userId) {
        List<TelegramNotificationTopicsInfo> availableTopics =
                telegramService.getNotificationTopics(userId);

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
        messageText.append("В данный момент список всех рассылок: \n");
        availableTopics.forEach(topic ->
                messageText.append(
                        "\n%s \\- %s".formatted(
                                topic.isUserSubscribed()
                                        ? "✅ Подписан"
                                        : "❌ Не подписан",
                                topic.getTopic()
                        )
                )
        );
        return messageText.toString();
    }

    public InlineKeyboardMarkup buildTopicControlMarkup(List<TelegramNotificationTopicsInfo> availableTopics) {
        InlineKeyboardMarkup topicControlMarkup = new InlineKeyboardMarkup();
        topicControlMarkup.setRowWidth(1);
        topicControlMarkup.addKeyboard(
                availableTopics.stream()
                        .map(topic -> {
                            String action = topic.isUserSubscribed() ? "❌ отписаться" : "✍️ подписаться";
                            String buttonText = "%s - %s".formatted(topic.getTopic(), action);

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
        String startMessage = MessageConstants.getHelpMessage();
        SendMessage messageToSend = buildBaseMessage(ctx, chatId, startMessage);
        messageToSend.replyMarkup(buildDefaultMenuMarkup());
        messageToSend.exec();
    }

    public void subscribeToTopic(BotContext ctx, String topicName, Long chatId, Long userId) {
        try {
            telegramService.subscribeToNotification(topicName, userId);
            ctx.sendMessage(chatId, "Успешно подписались на %s".formatted(topicName)).exec();
        } catch (TelegramCommandHandleException e) {
            String errorMessage = "Произошла ошибка %s".formatted(e.getMessage());
            ctx.sendMessage(chatId, errorMessage).exec();
        }
    }

    public void unsubscribeFromTopic(BotContext ctx, String topicName, Long chatId, Long userId) {
        try {
            telegramService.unsubscribeFromNotification(topicName, userId);
            ctx.sendMessage(chatId, "Успешно отписались от %s".formatted(topicName)).exec();
        } catch (TelegramCommandHandleException e) {
            String errorMessage = "Произошла ошибка %s".formatted(e.getMessage());
            ctx.sendMessage(chatId, errorMessage).exec();
        }
    }

    public InlineKeyboardMarkup buildDefaultMenuMarkup() {
        InlineKeyboardMarkup menuMarkup = new InlineKeyboardMarkup();
        menuMarkup.setRowWidth(1);
        menuMarkup.addKeyboard(
                new InlineKeyboardButton("Помощь", "help_callback"),
                new InlineKeyboardButton("Список рассылок", "topic_list")
        );
        return menuMarkup;
    }

    public SendMessage buildBaseMessage(BotContext botContext, Long chatId, String messageText) {
        return botContext.sendMessage(chatId, messageText).parseMode(ParseMode.MARKDOWNV2);
    }
}
