package com.example.lms.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileController {

    @Value("${app.upload-dir:/tmp/lms-uploads}")
    private String uploadDir;

    @GetMapping("/files/submissions/{filename}")
    public ResponseEntity<Resource> downloadSubmissionFile(@PathVariable String filename) {
        if (!filename.matches("[a-zA-Z0-9._-]+")) {
            return ResponseEntity.badRequest().build();
        }

        Path dir = Paths.get(uploadDir, "submissions").toAbsolutePath().normalize();
        Path file = dir.resolve(filename).normalize();
        if (!file.startsWith(dir)) {
            return ResponseEntity.badRequest().build();
        }

        Resource resource = new FileSystemResource(file);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String detected = Files.probeContentType(file);
            if (detected != null && !detected.isBlank()) {
                mediaType = MediaType.parseMediaType(detected);
            }
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(resource);
    }
}
