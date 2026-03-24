package com.githubaccess.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepoAccessDto {
    private String repo;
    private String role;
}
