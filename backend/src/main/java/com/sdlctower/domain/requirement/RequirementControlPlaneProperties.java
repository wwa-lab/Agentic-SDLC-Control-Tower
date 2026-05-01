package com.sdlctower.domain.requirement;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.requirement-control-plane")
public class RequirementControlPlaneProperties {

    private final SourceIntegration jira = new SourceIntegration();
    private final SourceIntegration confluence = new SourceIntegration();
    private final GitHubIntegration github = new GitHubIntegration();
    private final CliRunner cliRunner = new CliRunner();

    public SourceIntegration getJira() {
        return jira;
    }

    public SourceIntegration getConfluence() {
        return confluence;
    }

    public GitHubIntegration getGithub() {
        return github;
    }

    public CliRunner getCliRunner() {
        return cliRunner;
    }

    public static class SourceIntegration {
        private String provider = "stub";
        private String baseUrl;
        private String email;
        private String apiToken;
        private String bearerToken;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

        public String getBearerToken() {
            return bearerToken;
        }

        public void setBearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
        }
    }

    public static class GitHubIntegration {
        private String provider = "stub";
        private String apiBaseUrl = "https://api.github.com";
        private String token;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static class CliRunner {
        private String backendBaseUrl = "http://localhost:8080";
        private String commandName = "./scripts/control-tower-run";

        public String getBackendBaseUrl() {
            return backendBaseUrl;
        }

        public void setBackendBaseUrl(String backendBaseUrl) {
            this.backendBaseUrl = backendBaseUrl;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }
    }
}
