package dev.neohub.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PluginDetailDto {
    private UUID id;
    private String name;
    private String slug;
    private String fullName;
    private String githubOwner;
    private String githubRepo;
    private String description;
    private String categoryName;
    private String categorySlug;
    private String[] tags;
    private Integer githubStars;
    private Integer githubForks;
    private Integer openIssues;
    private String license;
    private String homepageUrl;
    private String githubUrl;
    private String neovimMinVersion;
    private Boolean isVerified;
    private Boolean isColorscheme;
    private String installGuide;
    private String configExample;
    private String keymapsSection;
    private OffsetDateTime lastCommitAt;
    private OffsetDateTime lastScrapedAt;
    private OffsetDateTime createdAt;
}