package com.githubaccess.config;

public class GitHubConstants {

    private GitHubConstants() {}

    public static final String GITHUB_API_BASE = "https://api.github.com";
    public static final String GITHUB_GRAPHQL_URL = "https://api.github.com/graphql";
    public static final String ACCEPT_HEADER = "application/vnd.github+json";
    public static final int PAGE_SIZE = 100;
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ORGS_REPOS_ENDPOINT = "/orgs/%s/repos?per_page=%d&page=%d";
    public static final String REPO_COLLABORATORS_ENDPOINT = "/repos/%s/%s/collaborators?per_page=%d&page=%d";
}
