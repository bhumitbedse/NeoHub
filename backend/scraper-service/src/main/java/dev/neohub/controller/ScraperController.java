package dev.neohub.controller;

import dev.neohub.entity.Plugin;
import dev.neohub.repository.PluginRepository;
import dev.neohub.service.PluginSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/scraper")
@RequiredArgsConstructor
@Tag(name = "Scraper", description = "Manual scraper triggers")
public class ScraperController {

    private final PluginSyncService pluginSyncService;
    private final PluginRepository pluginRepository;

    @PostMapping("/sync")
    @Operation(summary = "Trigger full sync of all plugins")
    public ResponseEntity<String> syncAll() {
        new Thread(pluginSyncService::syncAll).start();
        return ResponseEntity.ok("Sync started for all plugins");
    }

    @PostMapping("/sync/{owner}/{repo}")
    @Operation(summary = "Sync a single plugin by owner/repo")
    public ResponseEntity<String> syncOne(
            @PathVariable String owner,
            @PathVariable String repo) {

        Optional<Plugin> plugin = pluginRepository
                .findAllActiveOrderByLastScrapedAsc()
                .stream()
                .filter(p -> p.getGithubOwner().equalsIgnoreCase(owner)
                        && p.getGithubRepo().equalsIgnoreCase(repo))
                .findFirst();

        if (plugin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        pluginSyncService.syncPlugin(plugin.get());
        return ResponseEntity.ok("Synced " + owner + "/" + repo);
    }
}