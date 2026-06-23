package ru.delmark.dads.notifications.integration.telegram;

import io.github.natanimn.telebof.BotClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.subscribtion.EventType;
import ru.delmark.dads.notifications.subscribtion.McpEvent;
import ru.delmark.dads.notifications.subscribtion.subscribers.EventSubscriber;
import ru.delmark.dads.notifications.subscribtion.subscribers.SubscriberType;

@Component
@RequiredArgsConstructor
public class TelegramMcpSubscriber implements EventSubscriber {

    private final BotClient telegramBot;

    @Override
    public SubscriberType getSubscriberType() {
        return SubscriberType.TELEGRAM;
    }

    @Override
    public boolean supportsEvent(EventType eventType) {
        return true;
    }

    @Override
    public void handleEvent(McpEvent event) {
        telegramBot.context.sendMessage(5579520443L, event.getNotificationMessage());
    }
}
