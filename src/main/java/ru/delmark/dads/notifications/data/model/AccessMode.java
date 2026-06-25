package ru.delmark.dads.notifications.data.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccessMode {
    PUBLIC(1),
    FRIEND(2),
    OWNER(3);

    private final Integer level;
}
