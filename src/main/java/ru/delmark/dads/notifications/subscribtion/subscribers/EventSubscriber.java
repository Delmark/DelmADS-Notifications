package ru.delmark.dads.notifications.subscribtion.subscribers;

import ru.delmark.dads.notifications.subscribtion.EventType;
import ru.delmark.dads.notifications.subscribtion.McpEvent;

public interface EventSubscriber {
    SubscriberType getSubscriberType();

    boolean supportsEvent(EventType eventType);

    void handleEvent(McpEvent mcpEvent);
}
