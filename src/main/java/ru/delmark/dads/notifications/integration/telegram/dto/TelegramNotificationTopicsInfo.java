package ru.delmark.dads.notifications.integration.telegram.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TelegramNotificationTopicsInfo {
    String topic;
    String alias;
    String description;
    boolean isUserSubscribed;
}
