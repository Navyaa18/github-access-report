package com.githubaccess.client;

import com.githubaccess.config.GitHubConstants;
import com.githubaccess.dto.RepoAccessDto;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
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
            String url = GitHubConstants.GITHUB_API_BASE + GitHubConstants.ORGS_REPOS_ENDPOINT.formatted(org, GitHubConstants.PAGE_SIZE, page);
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

    @SuppressWarnings("unchecked")
    public Map<String, List<RepoAccessDto>> getReposWithCollaborators(String org, String token) {
        Map<String, List<RepoAccessDto>> userRepoMap = new HashMap<>();
        String cursor = null;
        boolean hasNextPage = true;

        while (hasNextPage) {
            String afterClause = cursor != null ? ", after: \"" + cursor + "\"" : "";
            String query = """
                    {
                      organization(login: "%s") {
                        repositories(first: 100%s) {
                          nodes {
                            name
                            collaborators(first: 100) {
                              edges {
                                node { login }
                                permission
                              }
                            }
                          }
                          pageInfo {
                            hasNextPage
                            endCursor
                          }
                        }
                      }
                    }
                    """.formatted(org, afterClause);

            HttpHeaders headers = getHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> body = Map.of("query", query);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        GitHubConstants.GITHUB_GRAPHQL_URL, HttpMethod.POST, entity, Map.class);
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                Map<String, Object> organization = (Map<String, Object>) data.get("organization");
                Map<String, Object> repositories = (Map<String, Object>) organization.get("repositories");
                List<Map<String, Object>> repoNodes = (List<Map<String, Object>>) repositories.get("nodes");
                Map<String, Object> pageInfo = (Map<String, Object>) repositories.get("pageInfo");

                for (Map<String, Object> repo : repoNodes) {
                    String repoName = (String) repo.get("name");
                    Map<String, Object> collaborators = (Map<String, Object>) repo.get("collaborators");
                    if (collaborators == null) continue;
                    List<Map<String, Object>> edges = (List<Map<String, Object>>) collaborators.get("edges");
                    for (Map<String, Object> edge : edges) {
                        Map<String, Object> user = (Map<String, Object>) edge.get("node");
                        String login = (String) user.get("login");
                        String role = (String) edge.get("permission");
                        userRepoMap.computeIfAbsent(login, k -> new ArrayList<>()).add(new RepoAccessDto(repoName, role));
                    }
                }

                hasNextPage = (Boolean) pageInfo.get("hasNextPage");
                cursor = (String) pageInfo.get("endCursor");

            } catch (HttpClientErrorException.Unauthorized e) {
                throw new RuntimeException("Invalid GitHub token");
            }
        }
        return userRepoMap;
    }

    public List<Map<String, Object>> getCollaborators(String org, String repo, String token) {
        List<Map<String, Object>> allCollaborators = new ArrayList<>();
        int page = 1;
        while (true) {
            String url = GitHubConstants.GITHUB_API_BASE + GitHubConstants.REPO_COLLABORATORS_ENDPOINT.formatted(org, repo, GitHubConstants.PAGE_SIZE, page);
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