package dev.neohub.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PluginSummaryDto {
    private UUID id;
    private String name;
    private String slug;
    private String fullName;
    private String description;
    private String categoryName;
    private String categorySlug;
    private String[] tags;
    private Integer githubStars;
    private String githubUrl;
    private Boolean isVerified;
    private Boolean isColorscheme;
    private OffsetDateTime lastCommitAt;
}