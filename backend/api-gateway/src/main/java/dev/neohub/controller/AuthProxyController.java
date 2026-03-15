package dev.neohub.controller;

import dev.neohub.proxy.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthProxyController {

    private final ProxyService proxyService;

    @Value("${services.user}")
    private String userServiceUrl;

    @PostMapping("/github")
    public ResponseEntity<String> githubLogin(
            HttpServletRequest request) {
        String query = request.getQueryString() != null
                ? "?" + request.getQueryString()
                : "";
        return proxyService.forward(request,
                userServiceUrl + "/api/v1/auth/github" + query, null);
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMe(HttpServletRequest request) {
        return proxyService.forward(request,
                userServiceUrl + "/api/v1/auth/me", null);
    }
}