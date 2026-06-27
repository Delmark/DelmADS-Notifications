package ru.delmark.dads.notifications;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NotificationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationsApplication.class, args);
	}
}
