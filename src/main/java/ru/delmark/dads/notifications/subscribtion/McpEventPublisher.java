package ru.delmark.dads.notifications.subscribtion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.subscribtion.subscribers.EventSubscriber;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class McpEventPublisher {

    private final Set<EventSubscriber> subscribers;

    public String publish(McpEvent mcpEvent) {
        subscribers.forEach(subscriber -> subscriber.handleEvent(mcpEvent));
        return "Success";
    }
}
