package dev.neohub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class GitHubRepoDto {

    private String name;

    @JsonProperty("full_name")
    private String fullName;

    private String description;

    @JsonProperty("stargazers_count")
    private Integer stargazersCount;

    @JsonProperty("forks_count")
    private Integer forksCount;

    @JsonProperty("open_issues_count")
    private Integer openIssuesCount;

    @JsonProperty("pushed_at")
    private OffsetDateTime pushedAt;

    @JsonProperty("html_url")
    private String htmlUrl;

    private GitHubLicense license;

    @Data
    public static class GitHubLicense {
        private String name;
        private String spdxId;
    }
}