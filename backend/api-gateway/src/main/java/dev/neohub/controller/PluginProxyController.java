package dev.neohub.controller;

import dev.neohub.proxy.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PluginProxyController {

    private final ProxyService proxyService;

    @Value("${services.plugin}")
    private String pluginServiceUrl;

    @GetMapping("/plugins")
    public ResponseEntity<String> listPlugins(
            HttpServletRequest request) {
        String query = request.getQueryString() != null
                ? "?" + request.getQueryString()
                : "";
        return proxyService.forward(request,
                pluginServiceUrl + "/api/v1/plugins" + query, null);
    }

    @GetMapping("/plugins/search")
    public ResponseEntity<String> searchPlugins(
            HttpServletRequest request) {
        String query = request.getQueryString() != null
                ? "?" + request.getQueryString()
                : "";
        return proxyService.forward(request,
                pluginServiceUrl + "/api/v1/plugins/search" + query, null);
    }

    @GetMapping("/plugins/{slug}")
    public ResponseEntity<String> getPlugin(
            HttpServletRequest request,
            @PathVariable String slug) {
        return proxyService.forward(request,
                pluginServiceUrl + "/api/v1/plugins/" + slug, null);
    }

    @GetMapping("/plugins/colorschemes")
    public ResponseEntity<String> getColorschemes(
            HttpServletRequest request) {
        String query = request.getQueryString() != null
                ? "?" + request.getQueryString()
                : "";
        return proxyService.forward(request,
                pluginServiceUrl + "/api/v1/plugins/colorschemes" + query, null);
    }

    @GetMapping("/categories")
    public ResponseEntity<String> listCategories(
            HttpServletRequest request) {
        return proxyService.forward(request,
                pluginServiceUrl + "/api/v1/categories", null);
    }
}