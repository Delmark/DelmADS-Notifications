package ru.delmark.dads.notifications.integration.telegram;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.requests.send.SendDocument;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.data.model.NotificationTopic;
import ru.delmark.dads.notifications.data.repository.ChatDAO;
import ru.delmark.dads.notifications.data.repository.NotificationTopicDAO;
import ru.delmark.dads.notifications.exception.McpEventHandleException;
import ru.delmark.dads.notifications.mcp.request.AttachedFile;
import ru.delmark.dads.notifications.mcp.request.MessageNotificationRequest;
import ru.delmark.dads.notifications.mcp.response.McpResponse;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class TelegramMcpBridge {

    private final BotClient botClient;
    private final NotificationTopicDAO notificationTopicDAO;
    private final ChatDAO chatDAO;

    public McpResponse sendNotification(MessageNotificationRequest request) {
        NotificationTopic topic = notificationTopicDAO
                .getNotificationTopicByName(request.getNotificationTopic())
                .orElseThrow(() -> new McpEventHandleException("Notification topic not found"));

        List<Long> chatsForSend = chatDAO.getChatIdsForTopicNotificationSend(topic.getName());
        if (CollectionUtils.isEmpty(chatsForSend)) {
            return new McpResponse(
                    "Success",
                    "Notification processed. Warning: Nobody actually was subscribed to this topic."
            );
        }

        Map<Long, List<SendDocument>> documentsForChats = new HashMap<>();
        if (CollectionUtils.isNotEmpty(request.getAttachedFiles())) {
            documentsForChats = resolveTelegramDocs(chatsForSend, request.getAttachedFiles())
                    .stream().collect(Collectors.groupingBy(
                            IdentifiableDocument::chatId,
                            Collectors.mapping(
                                    IdentifiableDocument::sendDocument,
                                    Collectors.toList()
                            )
                    ));
        }

        for (Long chatId : chatsForSend) {
            botClient.context.sendMessage(chatId, request.getMessage()).await();
            if (documentsForChats.containsKey(chatId)) {
                documentsForChats.get(chatId).forEach(SendDocument::await);
            }
        }

        return new McpResponse(
                "Success",
                "Notification has been processed"
        );
    }

    private List<IdentifiableDocument> resolveTelegramDocs(List<Long> chatsToSend, List<AttachedFile> attachedFiles) {
        return attachedFiles.stream()
                .flatMap(file -> {
                    validateAttachedFile(file);
                    BotContext context = botClient.context;
                    Stream<Long> chatIdsStream = chatsToSend.stream();
                    return switch (file.getFileSource().toLowerCase(Locale.ROOT)) {
                        case "telegram" -> chatIdsStream.map(chatId ->
                                new IdentifiableDocument(chatId, context.sendDocument(chatId, file.getFileId()))
                        );
                        case "local" -> chatIdsStream.map(chatId ->
                                new IdentifiableDocument(chatId, context.sendDocument(chatId, new File(file.getFileId())))
                        );
                        default -> throw new McpEventHandleException("Unsupported file source: " + file.getFileSource());
                    };
                }).collect(Collectors.toList());
    }

    private void validateAttachedFile(AttachedFile file) {
        if (StringUtils.isBlank(file.getFileId())) {
            throw new McpEventHandleException("File id is required");
        }

        if (StringUtils.isBlank(file.getFileSource())) {
            throw new McpEventHandleException("Attached file source not found");
        }
    }

    record IdentifiableDocument(Long chatId, SendDocument sendDocument) {}
}
