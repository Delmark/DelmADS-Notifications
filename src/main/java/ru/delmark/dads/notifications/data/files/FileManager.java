package ru.delmark.dads.notifications.data.files;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.delmark.dads.notifications.exception.LocalFileExtractException;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class FileManager {

    @Value("${file.upload.directory:/data/files}")
    private String filePath;

    @Value("${mcp.roots}")
    private List<String> mcpRoots;

    @Value("${file.ttl:1d}")
    private Duration fileTTL;

    public File getLocalFile(String localFilePath) {
        if (CollectionUtils.isEmpty(mcpRoots)) {
            throw new LocalFileExtractException("MCP roots not specified, for local file resolving" +
                    " you need to create and specify roots directories");
        }

        mcpRoots.stream()
                .filter(localFilePath::startsWith)
                .map(File::new).filter(File::exists)
                .findFirst()
                .orElseThrow(() -> new LocalFileExtractException(
                        "No mcp roots matching your local found, user should create them manually")
                );

        return new File(localFilePath);
    }

    public File getServerFile(String fileUUID) {
        return (Files.exists(Paths.get(filePath, fileUUID)))
                ? new File(filePath + fileUUID)
                : null;
    }

    @SneakyThrows
    public String uploadFile(MultipartFile multipartFile) {
        Path fileRepo = Paths.get(filePath);
        if (!Files.exists(fileRepo)) {
            Files.createDirectories(fileRepo);
        }

        String fileId = UUID.randomUUID().toString();
        Path savePath = fileRepo.resolve(fileId);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, savePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return fileId;
    }

    public void clearOldFiles() {
        Path serverFiles = Paths.get(filePath);
        deleteExpiredDirectoryFiles(serverFiles);

        mcpRoots.forEach(root -> deleteExpiredDirectoryFiles(Paths.get(root)));
    }

    private void deleteExpiredDirectoryFiles(Path directoryPath) {
        if (Files.exists(directoryPath) && Files.isDirectory(directoryPath)) {
            File dir = directoryPath.toFile();
            Optional.ofNullable(dir.listFiles())
                    .ifPresent(list ->
                            Arrays.stream(list)
                                    .filter(isFileTTLExpired())
                                    .forEach(File::delete)
                    );
        }
    }

    private Predicate<File> isFileTTLExpired() {
        return file -> {
            long currentTime = System.currentTimeMillis();
            long ttl = fileTTL.toMillis();
            return file.lastModified() < currentTime - ttl;
        };
    }
}
