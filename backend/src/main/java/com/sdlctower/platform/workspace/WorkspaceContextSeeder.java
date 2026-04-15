package com.sdlctower.platform.workspace;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class WorkspaceContextSeeder implements CommandLineRunner {

    private final WorkspaceContextRepository repository;

    public WorkspaceContextSeeder(WorkspaceContextRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        WorkspaceContext context = new WorkspaceContext();
        context.setWorkspace("Global SDLC Tower");
        context.setApplication("Payment-Gateway-Pro");
        context.setSnowGroup("FIN-TECH-OPS");
        context.setProject("Q2-Cloud-Migration");
        context.setEnvironment("Production");
        repository.save(context);
    }
}
