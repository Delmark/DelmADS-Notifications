package ru.delmark.dads.notifications.data.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BotUser {
    Long id;
    String username;
    AccessMode accessLevel;
    Boolean preferredSilentMode;
    OffsetDateTime createdAt;
}
