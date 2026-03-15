package dev.neohub.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "plugins")
public class Plugin {

    @Id
    private UUID id;

    @Column(name = "github_owner")
    private String githubOwner;

    @Column(name = "github_repo")
    private String githubRepo;

    @Column(name = "full_name")
    private String fullName;

    private String name;

    @Column(name = "github_stars")
    private Integer githubStars;

    @Column(name = "github_forks")
    private Integer githubForks;

    @Column(name = "open_issues")
    private Integer openIssues;

    private String license;

    @Column(name = "readme_raw", columnDefinition = "TEXT")
    private String readmeRaw;

    @Column(name = "install_guide", columnDefinition = "TEXT")
    private String installGuide;

    @Column(name = "config_example", columnDefinition = "TEXT")
    private String configExample;

    @Column(name = "keymaps_section", columnDefinition = "TEXT")
    private String keymapsSection;

    @Column(name = "last_scraped_at")
    private OffsetDateTime lastScrapedAt;

    @Column(name = "last_commit_at")
    private OffsetDateTime lastCommitAt;

    @Column(name = "is_active")
    private Boolean isActive;
}