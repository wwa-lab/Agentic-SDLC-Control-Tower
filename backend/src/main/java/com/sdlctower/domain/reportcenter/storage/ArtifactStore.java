package com.sdlctower.domain.reportcenter.storage;

import java.io.IOException;
import org.springframework.core.io.Resource;

public interface ArtifactStore {

    String save(String filename, byte[] bytes) throws IOException;

    Resource read(String storagePath) throws IOException;

    void delete(String storagePath) throws IOException;
}
