package com.githubaccess.service;

import com.githubaccess.client.GitHubClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AccessReportService {

    private final GitHubClient gitHubClient;

    public AccessReportService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    public Map<String, List<Map<String, String>>> generateReport(String org, String token) {
        return gitHubClient.getReposWithCollaborators(org, token);
    }
}