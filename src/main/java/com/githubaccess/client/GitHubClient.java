package com.githubaccess.client;

import com.githubaccess.config.GitHubConstants;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GitHubClient {

    private final RestTemplate restTemplate;

    public GitHubClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Accept", GitHubConstants.ACCEPT_HEADER);
        return headers;
    }

    public List<Map<String, Object>> getRepos(String org, String token) {
        List<Map<String, Object>> allRepos = new ArrayList<>();
        int page = 1;
        while (true) {
            String url = GitHubConstants.GITHUB_API_BASE + "/orgs/" + org + "/repos?per_page=" + GitHubConstants.PAGE_SIZE + "&page=" + page;
            HttpEntity<Void> entity = new HttpEntity<>(getHeaders(token));
            try {
                ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
                List<Map<String, Object>> batch = response.getBody();
                if (batch == null || batch.isEmpty()) break;
                allRepos.addAll(batch);
                if (batch.size() < GitHubConstants.PAGE_SIZE) break;
                page++;
            } catch (HttpClientErrorException.Unauthorized e) {
                throw new RuntimeException("Invalid GitHub token");
            } catch (HttpClientErrorException.NotFound e) {
                throw new RuntimeException("Organization not found: " + org);
            }
        }
        return allRepos;
    }

    public List<Map<String, Object>> getCollaborators(String org, String repo, String token) {
        List<Map<String, Object>> allCollaborators = new ArrayList<>();
        int page = 1;
        while (true) {
            String url = GitHubConstants.GITHUB_API_BASE + "/repos/" + org + "/" + repo + "/collaborators?per_page=" + GitHubConstants.PAGE_SIZE + "&page=" + page;
            HttpEntity<Void> entity = new HttpEntity<>(getHeaders(token));
            try {
                ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
                List<Map<String, Object>> batch = response.getBody();
                if (batch == null || batch.isEmpty()) break;
                allCollaborators.addAll(batch);
                if (batch.size() < GitHubConstants.PAGE_SIZE) break;
                page++;
            } catch (HttpClientErrorException.Unauthorized e) {
                throw new RuntimeException("Invalid GitHub token");
            } catch (HttpClientErrorException.NotFound e) {
                throw new RuntimeException("Repository not found: " + repo);
            }
        }
        return allCollaborators;
    }
}