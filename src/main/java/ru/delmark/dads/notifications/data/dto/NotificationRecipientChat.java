package ru.delmark.dads.notifications.data.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRecipientChat {
    private Long chatId;
    private Boolean silentSend;
}
