package ru.delmark.dads.notifications.subscribtion;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class McpEvent {
    String eventType;
    String notificationMessage;
}
