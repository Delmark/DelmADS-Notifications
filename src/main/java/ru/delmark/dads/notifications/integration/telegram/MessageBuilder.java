package ru.delmark.dads.notifications.integration.telegram;

import lombok.experimental.UtilityClass;
import ru.delmark.dads.notifications.data.model.NotificationSubscription;
import ru.delmark.dads.notifications.data.model.NotificationTopic;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class MessageBuilder {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String getStartMessage(String username) {
        return """
                Привет, %s\\! Данный бот предназначен для рассылок файлов/уведомлений/сообщений
                из системы DelmADS\\.
                
                Бот в данный момент предоставляет возможности получать ежедневные и еженедельные,
                новостные и еженедельные дайджесты, сборники мемов\\.
                Выбери рассылки которые хочешь получать из списка рассылок \\(~~по кнопке или~~ вызвав команду __/list__\\) и начнёшь получать уведомления с прикреплёнными сообщениями или файлами\\!
                
                Дополнительная информация \\- __/help__
                """.formatted(username);
    }

    public String getHelpMessage() {
        return """
                Данный бот предназначен для получения рассылок из системы DelmADS\\.
                
                Рассылки в данный момент делаются в *произвольное* время по мере поступления событий\\.
                Пользователям приходят только те сообщения, на которые они подписаны из _доступных_ им подписок на рассылки\\.
                
                Помощь \\(вы сейчас здесь\\!\\) /help
                Список предоставляемых рассылок можно получить по команде: /list
                Подписаться на рассылку: /subscribe *\\<тег рассылки\\>*
                Отписаться от рассылки: /unsubscribe *\\<тег рассылки\\>*
                """;
    }

    public String getTopicSettings(NotificationTopic topic, NotificationSubscription userSubscription) {
        return """
                Настройки рассылки: %s (%s)
                
                Дата подписки: %s
                
                Тихая отправка сообщений для этой рассылки: %s
                """.formatted(
                        topic.getAlias(),
                        topic.getName(),
                        formatter.format(userSubscription.getCreatedAt()),
                        userSubscription.getSilentMode() ? "вкл" : "выкл"
                );
    }

    public String getSettingsMessage(boolean isGlobalSilentModeOn) {
        return """
                Настройки получения уведомлений:
                
                Предпочитать отправку сообщений без push уведомлений: %s
                """.formatted((isGlobalSilentModeOn) ? "да" : "нет");
    }
}
