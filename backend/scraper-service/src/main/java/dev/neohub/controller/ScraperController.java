package dev.neohub.controller;

import dev.neohub.service.PluginSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scraper")
@RequiredArgsConstructor
@Tag(name = "Scraper", description = "Manual scraper triggers")
public class ScraperController {

    private final PluginSyncService pluginSyncService;

    @PostMapping("/sync")
    @Operation(summary = "Trigger full sync of all plugins")
    public ResponseEntity<String> syncAll() {
        // Run in background so endpoint returns immediately
        new Thread(pluginSyncService::syncAll).start();
        return ResponseEntity.ok("Sync started for all plugins");
    }

    @PostMapping("/sync/{owner}/{repo}")
    @Operation(summary = "Sync a single plugin by owner/repo")
    public ResponseEntity<String> syncOne(
            @PathVariable String owner,
            @PathVariable String repo) {
        pluginSyncService.pluginRepository
                .findAllActiveOrderByLastScrapedAsc()
                .stream()
                .filter(p -> p.getGithubOwner().equals(owner)
                        && p.getGithubRepo().equals(repo))
                .findFirst()
                .ifPresent(pluginSyncService::syncPlugin);
        return ResponseEntity.ok("Synced " + owner + "/" + repo);
    }
}