package dev.neohub.service;

import dev.neohub.dto.GitHubRepoDto;
import dev.neohub.entity.Plugin;
import dev.neohub.repository.PluginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PluginSyncService {

    public final PluginRepository pluginRepository;
    private final GitHubScraperService scraperService;
    private final ReadmeParserService parserService;

    // Sync all active plugins
    public void syncAll() {
        List<Plugin> plugins = pluginRepository
                .findAllActiveOrderByLastScrapedAsc();

        log.info("Starting sync for {} plugins", plugins.size());
        int success = 0, failed = 0;

        for (Plugin plugin : plugins) {
            try {
                syncPlugin(plugin);
                success++;
                // Small delay to avoid GitHub rate limiting
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Failed to sync {}: {}", plugin.getFullName(), e.getMessage());
                failed++;
            }
        }

        log.info("Sync complete — success: {}, failed: {}", success, failed);
    }

    // Sync a single plugin
    @Transactional
    public void syncPlugin(Plugin plugin) {
        log.info("Syncing {}", plugin.getFullName());

        // 1. Fetch repo metadata
        GitHubRepoDto repo = scraperService.fetchRepo(
                plugin.getGithubOwner(), plugin.getGithubRepo());

        if (repo != null) {
            plugin.setGithubStars(repo.getStargazersCount());
            plugin.setGithubForks(repo.getForksCount());
            plugin.setOpenIssues(repo.getOpenIssuesCount());
            plugin.setLastCommitAt(repo.getPushedAt());

            if (repo.getLicense() != null) {
                plugin.setLicense(repo.getLicense().getName());
            }
        }

        // 2. Fetch and parse README
        String readme = scraperService.fetchReadme(
                plugin.getGithubOwner(), plugin.getGithubRepo());

        if (readme != null) {
            plugin.setReadmeRaw(readme.substring(
                    0, Math.min(readme.length(), 50000)));
            plugin.setInstallGuide(
                    parserService.extractInstallGuide(readme));
            plugin.setConfigExample(
                    parserService.extractConfigExample(readme));
            plugin.setKeymapsSection(
                    parserService.extractKeymaps(readme));
        }

        plugin.setLastScrapedAt(OffsetDateTime.now());
        pluginRepository.save(plugin);

        log.info("Synced {} — ⭐ {}", plugin.getFullName(),
                plugin.getGithubStars());
    }
}