package ru.delmark.dads.notifications.integration.telegram.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum TopicOps {
    SUBSCRIBE("sub_"), UNSUBSCRIBE("unsub_");

    private final String operation;

    @Getter
    private static final Set<String> stringTopicOps = EnumSet.allOf(TopicOps.class).stream()
            .map(TopicOps::getOperation).collect(Collectors.toSet());

    public String getFormattedCallbackDate(String topicName) {
        return (operation + "%s").formatted(topicName);
    }

    public static TopicOps fromOpTag(String operation) {
        for (TopicOps op : TopicOps.values()) {
            if (op.getOperation().equals(operation)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown operation: " + operation);
    }
}
