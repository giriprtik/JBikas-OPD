package com.hospital.lab.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores attachment bytes on a local volume. Files are named by random UUID, never by the
 * user-supplied name, so uploads cannot overwrite each other or escape the directory.
 * Swap this class for an S3/MinIO implementation when running more than one lab-service instance.
 */
@Component
public class AttachmentStorage {

    private static final Pattern KEY = Pattern.compile("^[0-9a-f]{32}$");

    private final Path root;

    public AttachmentStorage(@Value("${lab.attachments.dir:/data/lab-attachments}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create attachment directory " + root, e);
        }
    }

    /** Writes the stream to a new file and returns its storage key. */
    public String save(InputStream content) {
        String key = UUID.randomUUID().toString().replace("-", "");
        Path target = resolve(key);
        Path temp = root.resolve(key + ".part");
        try {
            Files.copy(content, temp, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            return key;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // best effort cleanup
            }
            throw new UncheckedIOException("Could not store attachment", e);
        }
    }

    public Resource load(String key) {
        Path path = resolve(key);
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Attachment file missing on disk: " + key);
        }
        return new FileSystemResource(path);
    }

    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete attachment " + key, e);
        }
    }

    private Path resolve(String key) {
        if (!KEY.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return root.resolve(key);
    }
}
