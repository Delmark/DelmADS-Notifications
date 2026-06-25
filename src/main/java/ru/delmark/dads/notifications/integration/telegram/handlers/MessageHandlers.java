package ru.delmark.dads.notifications.integration.telegram.handlers;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.requests.send.SendMessage;
import io.github.natanimn.telebof.spring.Bot;
import io.github.natanimn.telebof.types.chat_and_user.User;
import io.github.natanimn.telebof.types.updates.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import ru.delmark.dads.notifications.exception.TelegramCommandHandleException;
import ru.delmark.dads.notifications.integration.telegram.TelegramService;
import ru.delmark.dads.notifications.integration.telegram.dto.MessageConstants;
import ru.delmark.dads.notifications.integration.telegram.dto.TelegramNotificationTopicsInfo;

import java.util.List;



@Bot
@RequiredArgsConstructor
@Slf4j
public class MessageHandlers {

    private final TelegramService telegramService;

    @MessageHandler(filter = BotMessageFilter.class, priority = 2)
    public void commonMessageHandler(BotContext botContext, Message message) {
        User user = message.getFrom();
        if (!telegramService.isUserRegistered(user.getId())) {
            telegramService.registerNewUser(user.getId(), user.getUsername());
        }
        telegramService.registerNewChatIfNew(message.getChat().getId(), user.getId());
    }

    @MessageHandler(commands = "start", filter = BotMessageFilter.class, priority = 1)
    public void startCommand(BotContext botContext, Message message) {
        String startMessage = MessageConstants.getStartMessage(message.getFrom().getUsername());
        SendMessage messageToSend = buildBaseMessage(botContext, message, startMessage);
        messageToSend.exec();
    }

    @MessageHandler(commands = "help", filter = BotMessageFilter.class, priority = 1)
    public void helpCommand(BotContext botContext, Message message) {
        String helpMessage = MessageConstants.getHelpMessage();
        SendMessage messageToSend = buildBaseMessage(botContext, message, helpMessage);
        messageToSend.exec();
    }

    @MessageHandler(commands = "list", filter = BotMessageFilter.class, priority = 1)
    public void listCommand(BotContext botContext, Message message) {
        User user = message.getFrom();
        List<TelegramNotificationTopicsInfo> availableTopics =
                telegramService.getNotificationTopics(user.getId());

        StringBuilder messageText = new StringBuilder();
        if (CollectionUtils.isEmpty(availableTopics)) {
            messageText.append("К сожалению, на данный момент нет рассылок на которые вы можете подписаться");
            botContext.sendMessage(message.getChat().getId(), messageText.toString()).exec();
            return;
        }

        messageText.append("В данный момент список всех рассылок: \n");
        availableTopics.forEach(topic ->
                messageText.append(
                        "\n%s \\- %s".formatted(
                                topic.getTopic(),
                                topic.isUserSubscribed()
                                        ? "✅ Подписан"
                                        : "❌ Не подписан"
                        )
                )
        );

        buildBaseMessage(botContext, message, messageText.toString()).exec();
    }

    @MessageHandler(commands = "subscribe", filter = BotMessageFilter.class, priority = 1)
    public void subscribeCommand(BotContext botContext, Message message) {
        User user = message.getFrom();
        String messageText = message.getText().replaceFirst("/subscribe", "").trim();
        Long chatId = message.getChat().getId();
        try {
            telegramService.subscribeToNotification(messageText, user.getId());
            botContext.sendMessage(chatId, "Успешно подписались на %s".formatted(messageText)).exec();
        } catch (TelegramCommandHandleException e) {
            String errorMessage = "Произошла ошибка %s".formatted(e.getMessage());
            botContext.sendMessage(message.getChat().getId(), errorMessage).exec();
        }
    }

    @MessageHandler(commands = "unsubscribe", filter = BotMessageFilter.class, priority = 1)
    public void unsubscribeCommand(BotContext botContext, Message message) {
        User user = message.getFrom();
        String messageText = message.getText().replaceFirst("/unsubscribe", "").trim();
        Long chatId = message.getChat().getId();
        try {
            telegramService.unsubscribeFromNotification(messageText, user.getId());
            botContext.sendMessage(chatId, "Успешно отписались от %s".formatted(messageText)).exec();
        } catch (TelegramCommandHandleException e) {
            String errorMessage = "Произошла ошибка %s".formatted(e.getMessage());
            botContext.sendMessage(message.getChat().getId(), errorMessage).exec();
        }
    }

    private SendMessage buildBaseMessage(BotContext botContext, Message message, String messageText) {
        return botContext
                .sendMessage(message.getChat().getId(), messageText)
                .parseMode(ParseMode.MARKDOWNV2);
    }
}
