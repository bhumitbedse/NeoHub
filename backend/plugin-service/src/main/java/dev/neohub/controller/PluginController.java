package dev.neohub.controller;

import dev.neohub.dto.PluginDetailDto;
import dev.neohub.dto.PluginSummaryDto;
import dev.neohub.service.PluginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plugins")
@RequiredArgsConstructor
@Tag(name = "Plugins", description = "Plugin listing, search and detail endpoints")
public class PluginController {

    private final PluginService pluginService;

    @GetMapping
    @Operation(summary = "List all plugins with pagination")
    public ResponseEntity<Page<PluginSummaryDto>> listPlugins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "githubStars") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return ResponseEntity.ok(
                pluginService.getAllPlugins(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter plugins")
    public ResponseEntity<Page<PluginSummaryDto>> searchPlugins(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                pluginService.filterPlugins(q, category, PageRequest.of(page, size)));
    }

    @GetMapping("/colorschemes")
    @Operation(summary = "List all colorschemes and themes")
    public ResponseEntity<Page<PluginSummaryDto>> listColorschemes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                pluginService.getColorschemes(PageRequest.of(page, size)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get full plugin details by slug")
    public ResponseEntity<PluginDetailDto> getPlugin(@PathVariable String slug) {
        return ResponseEntity.ok(pluginService.getBySlug(slug));
    }
}