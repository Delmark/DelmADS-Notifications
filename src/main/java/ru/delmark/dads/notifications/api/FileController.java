package ru.delmark.dads.notifications.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.delmark.dads.notifications.data.files.FileManager;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileManager fileManager;

    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return fileManager.uploadFile(file);
    }
}
