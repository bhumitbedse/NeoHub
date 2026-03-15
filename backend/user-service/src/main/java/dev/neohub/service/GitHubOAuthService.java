package dev.neohub.service;

import dev.neohub.dto.GitHubUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubOAuthService {

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    // Step 1: Exchange code for access token
    public String exchangeCodeForToken(String code) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://github.com/login/oauth/access_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class);

        if (response.getBody() == null || response.getBody().containsKey("error")) {
            throw new RuntimeException("Failed to exchange GitHub code: "
                    + (response.getBody() != null ? response.getBody().get("error_description") : "null response"));
        }

        return (String) response.getBody().get("access_token");
    }

    // Step 2: Fetch GitHub user profile using the access token
    public GitHubUserDto fetchGitHubUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        ResponseEntity<GitHubUserDto> response = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GitHubUserDto.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Failed to fetch GitHub user profile");
        }

        return response.getBody();
    }
}