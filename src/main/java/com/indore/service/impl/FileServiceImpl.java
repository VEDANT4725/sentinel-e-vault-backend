package com.indore.service.impl;

import com.indore.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final String uploadDir = "uploads/profiles";

    @Override
    public String uploadProfileImage(MultipartFile file) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // Save the file
            Path path = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), path);

            // Return relative path or just filename
            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Could not store file. Please try again!", e);
        }
    }
}
