package dev.neohub.service;

import dev.neohub.dto.PluginDetailDto;
import dev.neohub.dto.PluginSummaryDto;
import dev.neohub.entity.Plugin;
import dev.neohub.repository.PluginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PluginService {

    private final PluginRepository pluginRepository;

    public Page<PluginSummaryDto> getAllPlugins(Pageable pageable) {
        return pluginRepository.findByIsActiveTrue(pageable)
                .map(this::toSummaryDto);
    }

    public PluginDetailDto getBySlug(String slug) {
        Plugin plugin = pluginRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Plugin not found: " + slug));
        return toDetailDto(plugin);
    }

    public Page<PluginSummaryDto> filterPlugins(String query, String categorySlug, Pageable pageable) {
        return pluginRepository.filterPlugins(query, categorySlug, pageable)
                .map(this::toSummaryDto);
    }

    public Page<PluginSummaryDto> getColorschemes(Pageable pageable) {
        return pluginRepository.findByIsColorschemeTrue(pageable)
                .map(this::toSummaryDto);
    }

    // ── mappers ──────────────────────────────────

    private PluginSummaryDto toSummaryDto(Plugin p) {
        PluginSummaryDto dto = new PluginSummaryDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        dto.setFullName(p.getFullName());
        dto.setDescription(p.getDescription());
        dto.setTags(p.getTags());
        dto.setGithubStars(p.getGithubStars());
        dto.setGithubUrl(p.getGithubUrl());
        dto.setIsVerified(p.getIsVerified());
        dto.setIsColorscheme(p.getIsColorscheme());
        dto.setLastCommitAt(p.getLastCommitAt());
        if (p.getCategory() != null) {
            dto.setCategoryName(p.getCategory().getName());
            dto.setCategorySlug(p.getCategory().getSlug());
        }
        return dto;
    }

    private PluginDetailDto toDetailDto(Plugin p) {
        PluginDetailDto dto = new PluginDetailDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        dto.setFullName(p.getFullName());
        dto.setGithubOwner(p.getGithubOwner());
        dto.setGithubRepo(p.getGithubRepo());
        dto.setDescription(p.getDescription());
        dto.setTags(p.getTags());
        dto.setGithubStars(p.getGithubStars());
        dto.setGithubForks(p.getGithubForks());
        dto.setOpenIssues(p.getOpenIssues());
        dto.setLicense(p.getLicense());
        dto.setHomepageUrl(p.getHomepageUrl());
        dto.setGithubUrl(p.getGithubUrl());
        dto.setNeovimMinVersion(p.getNeovimMinVersion());
        dto.setIsVerified(p.getIsVerified());
        dto.setIsColorscheme(p.getIsColorscheme());
        dto.setInstallGuide(p.getInstallGuide());
        dto.setConfigExample(p.getConfigExample());
        dto.setKeymapsSection(p.getKeymapsSection());
        dto.setLastCommitAt(p.getLastCommitAt());
        dto.setLastScrapedAt(p.getLastScrapedAt());
        dto.setCreatedAt(p.getCreatedAt());
        if (p.getCategory() != null) {
            dto.setCategoryName(p.getCategory().getName());
            dto.setCategorySlug(p.getCategory().getSlug());
        }
        return dto;
    }
}