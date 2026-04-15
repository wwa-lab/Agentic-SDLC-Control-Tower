package com.sdlctower.platform.workspace;

import com.sdlctower.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceContextService {

    private final WorkspaceContextRepository repository;

    public WorkspaceContextService(WorkspaceContextRepository repository) {
        this.repository = repository;
    }

    public WorkspaceContextDto getCurrentWorkspaceContext() {
        WorkspaceContext entity = repository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Workspace context not found"));
        return WorkspaceContextDto.fromEntity(entity);
    }
}
