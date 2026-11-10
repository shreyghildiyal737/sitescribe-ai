package com.sitescribe.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "sitescribe.storage.mode", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path root;

    public LocalStorageService(@Value("${sitescribe.storage.local-dir:./uploads}") String dir) throws IOException {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    @Override
    public String store(String filename, String contentType, byte[] bytes) throws IOException {
        String safeName = UUID.randomUUID() + "_" + filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path target = root.resolve(safeName);
        Files.write(target, bytes);
        return target.toString();
    }

    @Override
    public byte[] retrieve(String reference) throws IOException {
        return Files.readAllBytes(Path.of(reference));
    }

    @Override
    public void delete(String reference) throws IOException {
        Files.deleteIfExists(Path.of(reference));
    }
}
