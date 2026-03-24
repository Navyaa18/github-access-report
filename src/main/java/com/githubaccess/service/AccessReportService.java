package com.githubaccess.service;

import com.githubaccess.client.GitHubClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccessReportService {

    private final GitHubClient gitHubClient;

    public AccessReportService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    public Map<String, List<String>> generateReport(String org, String token) {

        List<Map<String, Object>> repos = gitHubClient.getRepos(org, token);

        // thread-safe map
        Map<String, List<String>> userRepoMap = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map<String, Object> repo : repos) {

            String repoName = (String) repo.get("name");

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

                List<Map<String, Object>> collaborators =
                        gitHubClient.getCollaborators(org, repoName, token);

                for (Map<String, Object> user : collaborators) {

                    String username = (String) user.get("login");

                    userRepoMap
                            .computeIfAbsent(username,
                                    k -> Collections.synchronizedList(new ArrayList<>()))
                            .add(repoName);
                }
            });

            futures.add(future);
        }

        // wait for all threads
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return userRepoMap;
    }
}