package ru.delmark.dads.notifications.integration.telegram.handlers;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.requests.send.SendMessage;
import io.github.natanimn.telebof.spring.Bot;
import io.github.natanimn.telebof.types.chat_and_user.User;
import io.github.natanimn.telebof.types.updates.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import ru.delmark.dads.notifications.integration.telegram.TelegramService;
import ru.delmark.dads.notifications.integration.telegram.MessageBuilder;
import ru.delmark.dads.notifications.integration.telegram.handlers.filters.BotMessageFilter;
import ru.delmark.dads.notifications.utils.MarkdownV2Escaper;


@Bot
@Slf4j
@RequiredArgsConstructor
public class MessageHandlers {

    private final TelegramService telegramService;
    private final CommonHandlerOperations operations;

    @MessageHandler(commands = "start", filter = BotMessageFilter.class, priority = 1)
    public void startCommand(BotContext botContext, Message message) {
        ensureUserRegistered(message);

        String startMessage = MessageBuilder.getStartMessage(
                MarkdownV2Escaper.escape(message.getFrom().getUsername())
        );
        SendMessage messageToSend = operations.buildBaseMessage(botContext, message.getChat().getId(), startMessage);
        messageToSend.replyMarkup(operations.buildDefaultMenuMarkup());
        messageToSend.exec();
    }

    @MessageHandler(commands = "help", filter = BotMessageFilter.class, priority = 1)
    public void helpCommand(BotContext botContext, Message message) {
        ensureUserRegistered(message);
        operations.sendHelpMessage(botContext, message.getChat().getId());
    }

    @MessageHandler(commands = "menu", filter = BotMessageFilter.class, priority = 1)
    public void menuCommand(BotContext botContext, Message message) {
        ensureUserRegistered(message);
        String menuMessage = "Выберите опцию";
        SendMessage messageToSend = operations.buildBaseMessage(botContext, message.getChat().getId(), menuMessage);
        messageToSend.replyMarkup(operations.buildDefaultMenuMarkup());
        messageToSend.exec();
    }

    @MessageHandler(commands = "list", filter = BotMessageFilter.class, priority = 1)
    public void listCommand(BotContext botContext, Message message) {
        ensureUserRegistered(message);
        operations.sendUserTopicActions(
                botContext, message.getChat().getId(),
                message.getFrom().getId()
        );
    }

    @MessageHandler(commands = "settings", filter = BotMessageFilter.class, priority = 1)
    public void settingsCommand(BotContext botContext, Message message) {
        ensureUserRegistered(message);
        operations.openSettingsPanel(botContext, message.getFrom().getId(), message.getChat().getId());
    }

    @MessageHandler(commands = "subscribe", filter = BotMessageFilter.class, priority = 1)
    public void subscribeCommand(BotContext botContext, Message message) {
        ensureUserRegistered(message);
        User user = message.getFrom();
        String topicName = message.getText().replaceFirst("/subscribe", "").trim();
        Long chatId = message.getChat().getId();
        operations.subscribeToTopic(botContext, topicName, chatId, user.getId(), true);
    }

    @MessageHandler(commands = "unsubscribe", filter = BotMessageFilter.class, priority = 1)
    public void unsubscribeCommand(BotContext botContext, Message message) {
        ensureUserRegistered(message);
        User user = message.getFrom();
        String topicName = message.getText().replaceFirst("/unsubscribe", "").trim();
        Long chatId = message.getChat().getId();
        operations.unsubscribeFromTopic(botContext, topicName, chatId, user.getId(), true);
    }

    private void ensureUserRegistered(Message message) {
        User user = message.getFrom();
        if (BooleanUtils.isFalse(telegramService.isUserRegistered(user.getId()))) {
            telegramService.registerNewUser(user.getId(), user.getUsername());
        }
        telegramService.registerNewChatIfNew(message.getChat().getId(), user.getId());
    }
}
