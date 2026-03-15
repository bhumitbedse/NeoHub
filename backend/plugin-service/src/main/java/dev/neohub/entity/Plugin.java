package dev.neohub.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "plugins")
public class Plugin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "github_owner", nullable = false)
    private String githubOwner;

    @Column(name = "github_repo", nullable = false)
    private String githubRepo;

    @Column(name = "full_name", unique = true, nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "TEXT[]")
    @org.hibernate.annotations.Array(length = 20)
    private String[] tags;

    @Column(name = "github_stars")
    private Integer githubStars = 0;

    @Column(name = "github_forks")
    private Integer githubForks = 0;

    @Column(name = "open_issues")
    private Integer openIssues = 0;

    private String license;

    @Column(name = "homepage_url")
    private String homepageUrl;

    @Column(name = "github_url", nullable = false)
    private String githubUrl;

    @Column(name = "neovim_min_version")
    private String neovimMinVersion;

    @Column(name = "is_colorscheme")
    private Boolean isColorscheme = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}