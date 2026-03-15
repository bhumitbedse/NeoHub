package dev.neohub.scheduler;

import dev.neohub.service.PluginSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScraperScheduler {

    private final PluginSyncService pluginSyncService;

    // Run once on startup so you see data immediately
    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        log.info("Running initial plugin sync on startup...");
        pluginSyncService.syncAll();
    }

    // Then run every day at 2 AM
    @Scheduled(cron = "${github.scrape-cron}")
    public void scheduledSync() {
        log.info("Running scheduled plugin sync...");
        pluginSyncService.syncAll();
    }
}