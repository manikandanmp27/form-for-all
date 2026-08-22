package com.fillforme.backend.document.service;

import com.fillforme.backend.common.exception.DocumentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    private final Path rootLocation;

    public StorageService(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize upload directory: {}", uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (filename.isEmpty()) {
            throw new DocumentProcessingException("Failed to store empty file.");
        }
        if (filename.contains("..")) {
            throw new DocumentProcessingException("Cannot store file with relative path outside current directory " + filename);
        }

        String storedFilename = UUID.randomUUID() + "_" + filename;
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, this.rootLocation.resolve(storedFilename), StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to store file " + filename, e);
        }
    }

    public String storeBytes(byte[] bytes, String filename) {
        String cleanName = StringUtils.cleanPath(Objects.requireNonNull(filename));
        String storedFilename = cleanName.contains("_") ? cleanName : UUID.randomUUID() + "_" + cleanName;
        try {
            Files.write(this.rootLocation.resolve(storedFilename), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return storedFilename;
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to store bytes for file " + filename, e);
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    public InputStream loadAsInputStream(String filename) throws IOException {
        Path file = load(filename);
        return Files.newInputStream(file);
    }
}
