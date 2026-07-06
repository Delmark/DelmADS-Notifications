package ru.delmark.dads.notifications.data.files;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.delmark.dads.notifications.exception.LocalFileExtractException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
@Slf4j
public class FileManager {

    @Value("${file.upload.directory:/data/files}")
    private String filePath;

    @Value("${mcp.roots:}")
    private List<String> mcpRoots;

    @Value("${file.ttl:1d}")
    private Duration fileTTL;

    public Optional<File> getLocalFile(String localFilePath) {
        if (CollectionUtils.isEmpty(mcpRoots)) {
            throw new LocalFileExtractException("MCP roots not specified, for local file resolving" +
                    " you need to create and specify roots directories");
        }

        Path requestPath = Paths.get(localFilePath).toAbsolutePath().normalize();
        boolean allowed = mcpRoots.stream()
                .map(root -> Path.of(root).toAbsolutePath().normalize())
                .anyMatch(requestPath::startsWith);

        if (!Files.isRegularFile(requestPath, LinkOption.NOFOLLOW_LINKS) || !allowed) {
            throw new LocalFileExtractException("Local file is outside configured MCP roots or does not exist");
        }

        return Optional.of(requestPath.toFile());
    }

    public Optional<File> getServerFile(String fileUUID) {
        try {
            Path baseDir = Paths.get(filePath).toAbsolutePath().normalize();
            Path candidate = baseDir.resolve(fileUUID).normalize();

            if (!candidate.startsWith(baseDir)) {
                log.warn("Attempt to access file outside of server storage directory: {}", fileUUID);
                return Optional.empty();
            }

            return Optional.of(candidate.toRealPath(LinkOption.NOFOLLOW_LINKS).toFile());
        } catch (IOException e) {
            log.error("Failed to extract server file", e);
            return Optional.empty();
        }
    }

    @SneakyThrows
    public String uploadFile(MultipartFile multipartFile) {
        Path fileRepo = Paths.get(filePath);
        if (!Files.exists(fileRepo)) {
            Files.createDirectories(fileRepo);
        }

        String fileId = UUID.randomUUID().toString();

        String fileName = multipartFile.getOriginalFilename();
        int dotIndex = (fileName != null) ? fileName.lastIndexOf(".") : -1;
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            fileId = fileId.concat(fileName.substring(dotIndex));
        }

        Path savePath = fileRepo.resolve(fileId);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, savePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return fileId;
    }

    public void clearOldFiles() {
        Path serverFilesDir = Paths.get(filePath);

        if (Files.exists(serverFilesDir) && Files.isDirectory(serverFilesDir)) {
            File dir = serverFilesDir.toFile();
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
