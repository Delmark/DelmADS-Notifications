package ru.delmark.dads.notifications.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileController {

    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return file.getOriginalFilename();
    }
}
