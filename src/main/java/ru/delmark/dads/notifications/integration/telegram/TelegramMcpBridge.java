package ru.delmark.dads.notifications.integration.telegram;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.requests.send.SendDocument;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.delmark.dads.notifications.data.files.FileManager;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class TelegramMcpBridge {

    private final BotClient botClient;
    private final FileManager fileManager;
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
            // сначала всегда отправляем текст, потом файлы
            botClient.context.sendMessage(chatId, request.getMessage()).exec();
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
                .peek(this::validateAttachedFile)
                .flatMap(resolveChatDocuments(chatsToSend, botClient.context))
                .filter(doc -> doc.sendDocument() != null)
                .toList();
    }

    private Function<AttachedFile, Stream<IdentifiableDocument>> resolveChatDocuments(List<Long> chatIds, BotContext context) {
        return file ->
                switch (file.getFileSource().toLowerCase(Locale.ROOT)) {
                    case "telegram" -> chatIds.stream().map(createTGDocumentForSend(context, file.getFileId()));
                    case "local" -> chatIds.stream().map(createFileDocumentForSend(context, () -> fileManager.getLocalFile(file.getFileId())));
                    case "server" -> chatIds.stream().map(createFileDocumentForSend(context, () -> fileManager.getServerFile(file.getFileId())));
                    default -> throw new McpEventHandleException("Unsupported file source: " + file.getFileSource());
                };
    }

    // input -> chatId
    private Function<Long, IdentifiableDocument> createFileDocumentForSend(BotContext context, Supplier<File> fileSupplier) {
        return chatId -> {
            File documentFile = fileSupplier.get();
            SendDocument sendDoc = (documentFile != null) ? context.sendDocument(chatId, documentFile) : null;
            return new IdentifiableDocument(chatId, sendDoc);
        };
    }

    private Function<Long, IdentifiableDocument> createTGDocumentForSend(BotContext context, String fileId) {
        return chatId -> new IdentifiableDocument(chatId, context.sendDocument(chatId, fileId));
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
