package ru.delmark.dads.notifications.exception;

public class McpEventHandleException extends RuntimeException {
  public McpEventHandleException(Throwable cause) {
    super(cause);
  }

  public McpEventHandleException(String message) {
    super(message);
  }
}
