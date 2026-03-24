package com.githubaccess.controller;

import com.githubaccess.config.GitHubConstants;
import com.githubaccess.service.AccessReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccessReportController {

    private final AccessReportService service;

    public AccessReportController(AccessReportService service) {
        this.service = service;
    }

    @GetMapping("/access-report")
    public Map<String, List<Map<String, String>>> getAccessReport(
            @RequestParam String org,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace(GitHubConstants.BEARER_PREFIX, "");
        return service.generateReport(org, token);
    }
}