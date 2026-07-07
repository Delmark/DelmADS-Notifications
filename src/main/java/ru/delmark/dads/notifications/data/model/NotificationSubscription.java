package ru.delmark.dads.notifications.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class NotificationSubscription {
    Long userId;
    Long topicId;
    Boolean silentMode;
    OffsetDateTime createdAt;
}
