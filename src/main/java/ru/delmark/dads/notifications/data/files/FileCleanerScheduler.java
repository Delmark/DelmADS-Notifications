package ru.delmark.dads.notifications.data.files;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileCleanerScheduler {

    private final FileManager fileManager;

    @Scheduled(cron = "${file.cleaner.cron}")
    public void clearOldFiles() {
        fileManager.clearOldFiles();
    }
}
