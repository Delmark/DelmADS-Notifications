package ru.delmark.dads.notifications.data.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NotificationTopic {
    String name;
    AccessMode minAccessLevel;
    OffsetDateTime createdAt;
}
