package com.sdlctower.platform.workspace;

import com.sdlctower.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceContextService {

    private final WorkspaceContextRepository repository;

    public WorkspaceContextService(WorkspaceContextRepository repository) {
        this.repository = repository;
    }

    public WorkspaceContext getCurrentWorkspaceContext() {
        return repository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Workspace context was not found"));
    }
}
