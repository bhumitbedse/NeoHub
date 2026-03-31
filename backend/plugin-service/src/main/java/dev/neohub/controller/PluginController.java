package dev.neohub.controller;

import dev.neohub.dto.PluginDetailDto;
import dev.neohub.dto.PluginSummaryDto;
import dev.neohub.service.PluginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/plugins")
@RequiredArgsConstructor
@Tag(name = "Plugins", description = "Plugin listing, search and detail endpoints")
public class PluginController {

    private final PluginService pluginService;

    @GetMapping
    @Operation(summary = "List all plugins with pagination")
    public ResponseEntity<Page<PluginSummaryDto>> listPlugins(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0") int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Size must be >= 1")
            @Max(value = 100, message = "Size must be <= 100") int size,

            @RequestParam(defaultValue = "githubStars") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return ResponseEntity.ok(
                pluginService.getAllPlugins(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter plugins")
    public ResponseEntity<Page<PluginSummaryDto>> searchPlugins(
            @RequestParam(required = false)
            @Size(max = 100, message = "Query must be <= 100 characters") String q,

            @RequestParam(required = false)
            @Size(max = 50, message = "Category slug must be <= 50 characters") String category,

            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(
                pluginService.filterPlugins(q, category, PageRequest.of(page, size)));
    }

    @GetMapping("/colorschemes")
    @Operation(summary = "List all colorschemes and themes")
    public ResponseEntity<Page<PluginSummaryDto>> listColorschemes(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(
                pluginService.getColorschemes(PageRequest.of(page, size)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get full plugin details by slug")
    public ResponseEntity<PluginDetailDto> getPlugin(
            @PathVariable
            @Size(min = 1, max = 200, message = "Slug must be between 1 and 200 characters")
            String slug
    ) {
        return ResponseEntity.ok(pluginService.getBySlug(slug));
    }
}