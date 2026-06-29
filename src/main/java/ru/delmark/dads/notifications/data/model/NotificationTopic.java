package ru.delmark.dads.notifications.data.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NotificationTopic {
    Long id;
    String name;
    String alias;
    String description;
    AccessMode minAccessLevel;
    OffsetDateTime createdAt;
}
