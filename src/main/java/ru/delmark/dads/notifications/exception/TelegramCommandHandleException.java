package ru.delmark.dads.notifications.exception;

public class TelegramCommandHandleException extends RuntimeException {
    public TelegramCommandHandleException(String message) {
        super(message);
    }
}
