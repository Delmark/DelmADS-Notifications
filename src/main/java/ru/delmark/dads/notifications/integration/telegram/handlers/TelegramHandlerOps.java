package ru.delmark.dads.notifications.integration.telegram.handlers;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.requests.send.SendMessage;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.integration.telegram.TelegramService;
import ru.delmark.dads.notifications.integration.telegram.dto.MessageConstants;
import ru.delmark.dads.notifications.integration.telegram.dto.TelegramNotificationTopicsInfo;
import ru.delmark.dads.notifications.integration.telegram.dto.TopicOps;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramHandlerOps {

    private final TelegramService telegramService;

    public void sendUserTopicActions(BotContext ctx, Long chatId, Long userId) {
        List<TelegramNotificationTopicsInfo> availableTopics =
                telegramService.getNotificationTopics(userId);

        StringBuilder messageText = new StringBuilder();
        if (CollectionUtils.isEmpty(availableTopics)) {
            messageText.append("К сожалению, на данный момент нет рассылок на которые вы можете подписаться");
            ctx.sendMessage(chatId, messageText.toString()).exec();
            return;
        }

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

        SendMessage send = buildBaseMessage(ctx, chatId, messageText.toString());

        InlineKeyboardMarkup topicControlMarkup = new InlineKeyboardMarkup();
        topicControlMarkup.setRowWidth(1);
        topicControlMarkup.addKeyboard(
                availableTopics.stream()
                        .map(topic -> {
                            String action = topic.isUserSubscribed() ? "❌ отписаться" : "✍️ подписаться";
                            String buttonText = "%s - %s".formatted(topic.getTopic(), action);

                            TopicOps topicOperation = topic.isUserSubscribed()
                                    ? TopicOps.UNSUBSCRIBE : TopicOps.SUBSCRIBE;
                            String callback = topicOperation.getFormattedCallbackDate(topic.getTopic());

                            return new InlineKeyboardButton(buttonText, callback);
                        })
                        .toArray(InlineKeyboardButton[]::new)
        );

        send.replyMarkup(topicControlMarkup);
        send.exec();
    }

    public void sendHelpMessage(BotContext ctx, Long chatId, String username) {
        String startMessage = MessageConstants.getStartMessage(username);
        SendMessage messageToSend = buildBaseMessage(ctx, chatId, startMessage);
        messageToSend.replyMarkup(buildDefaultMenuMarkup());
        messageToSend.exec();
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
