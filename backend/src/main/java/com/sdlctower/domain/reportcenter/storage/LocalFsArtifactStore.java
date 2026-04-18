package com.sdlctower.domain.reportcenter.storage;

import com.sdlctower.domain.reportcenter.config.ReportCenterProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class LocalFsArtifactStore implements ArtifactStore {

    private final Path root;

    public LocalFsArtifactStore(ReportCenterProperties properties) throws IOException {
        this.root = Path.of(properties.artifactDir());
        Files.createDirectories(root);
    }

    @Override
    public String save(String filename, byte[] bytes) throws IOException {
        Path target = root.resolve(filename);
        Files.write(target, bytes);
        return target.toString();
    }

    @Override
    public Resource read(String storagePath) {
        return new FileSystemResource(storagePath);
    }

    @Override
    public void delete(String storagePath) throws IOException {
        if (storagePath != null) {
            Files.deleteIfExists(Path.of(storagePath));
        }
    }
}
