package ru.delmark.dads.notifications.integration.telegram;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.CallbackHandler;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.requests.send.SendMessage;
import io.github.natanimn.telebof.spring.Bot;
import io.github.natanimn.telebof.types.chat_and_user.User;
import io.github.natanimn.telebof.types.keyboard.ReplyKeyboardMarkup;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Message;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

@Bot
@RequiredArgsConstructor
@Slf4j
public class TelegramBot {

    private final BotClient botClient;
    private User self;

    @PostConstruct
    public void getSelf() {
        self = botClient.context.getMe().exec();
        log.info("Get self {}", self.getFirstName());
    }

    @CallbackHandler
    public void callback(BotContext context, CallbackQuery callbackQuery) {
        context.sendMessage(
                callbackQuery.getMessage().getChat().getId(),
                ToStringBuilder.reflectionToString(callbackQuery)
        ).exec();
    }

    @MessageHandler(commands = "start", filter = BotMessageFilter.class, priority = 1)
    public void startCommandHandler(BotContext botContext, Message message) {
        String str = """
                Привет, %s\\! Данный бот предназначен для рассылок файлов/уведомлений/сообщений
                из системы DelmADS\\.
                
                Бот в данный момент предоставляет возможности получать ежедневные и еженедельные,
                новостные и еженедельные дайджесты, сборники мемов\\.
                Выбери рассылки которые хочешь получать из списка рассылок \\(по кнопке или вызвав команду __/list__\\) и начнёшь получать уведомления с прикреплёнными сообщениями или файлами\\!
                
                Дополнительная информация \\- __/help__
                """.formatted(message.getChat().getUsername());
        SendMessage messages = botContext.sendMessage(message.getChat().getId(), str);
        log.info("chat id {}", message.getChat().getId());
        messages.parseMode(ParseMode.MARKDOWNV2);
        messages.exec();
    }

    @MessageHandler(commands = "help", filter = BotMessageFilter.class, priority = 1)
    public void echoCommand(BotContext botContext, Message message) {
        String helpMessage = """
                Данный бот предназначен для получения рассылок из системы DelmADS\\.
                
                Рассылки в данный момент делаются в *произвольное* время по мере поступления событий\\.
                Пользователям приходят только те сообщения, на которые они подписаны из _доступных_ им подписок на рассылки\\.
                
                Помощь \\(вы сейчас здесь\\!\\) /help
                Список предоставляемых рассылок можно получить по команде: /list
                Подписаться на рассылку: /subscribe *\\<тег рассылки\\>*
                Отписаться от рассылки: /unsubscribe *\\<тег рассылки\\>*
                """;
        SendMessage messageToSend = botContext.sendMessage(message.getChat().getId(), helpMessage);
        messageToSend.parseMode(ParseMode.MARKDOWNV2).exec();
//        botContext.sendMessage(message.getChat().getId(), ).exec();
    }

//    @MessageHandler(commands = "config", filter = BotMessageFilter.class, priority = 1) {
//
//    }
}
