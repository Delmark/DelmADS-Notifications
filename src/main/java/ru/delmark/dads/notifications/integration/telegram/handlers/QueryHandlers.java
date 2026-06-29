package ru.delmark.dads.notifications.integration.telegram.handlers;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.CallbackHandler;
import io.github.natanimn.telebof.requests.edit.EditMessageReplyMarkup;
import io.github.natanimn.telebof.requests.edit.EditMessageText;
import io.github.natanimn.telebof.spring.Bot;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Message;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import ru.delmark.dads.notifications.integration.telegram.TelegramService;
import ru.delmark.dads.notifications.integration.telegram.dto.TelegramNotificationTopicsInfo;
import ru.delmark.dads.notifications.integration.telegram.dto.TopicOps;
import ru.delmark.dads.notifications.integration.telegram.handlers.filters.TopicSubsControlCallbackFilter;

import java.util.List;

@Bot
@RequiredArgsConstructor
public class QueryHandlers {

    private final CommonHandlerOperations ops;
    private final TelegramService telegramService;

    @CallbackHandler(data = "help_callback")
    public void openHelpCallback(BotContext ctx, CallbackQuery callback) {
        cleanOldMarkup(ctx, callback);
        ops.sendHelpMessage(ctx, callback.getMessage().getChat().getId());
    }

    @CallbackHandler(data = "topic_list")
    public void openTopicsCallback(BotContext ctx, CallbackQuery callback) {
        cleanOldMarkup(ctx, callback);
        ops.sendUserTopicActions(ctx, callback.getMessage().getChat().getId(), callback.getFrom().getId());
    }

    @CallbackHandler(filter = TopicSubsControlCallbackFilter.class)
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
            case SUBSCRIBE   -> ops.subscribeToTopic(ctx, topicTag, chatId, userId);
            case UNSUBSCRIBE -> ops.unsubscribeFromTopic(ctx, topicTag, chatId, userId);
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
        EditMessageText messageEdit = ctx.editMessageText(chatId, newMessage, messageId);
        if (markup != null) {
            messageEdit = messageEdit.replyMarkup(markup);
        }
        messageEdit.exec();
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
