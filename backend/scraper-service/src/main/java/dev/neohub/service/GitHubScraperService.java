package dev.neohub.service;

import dev.neohub.dto.GitHubRepoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubScraperService {

    @Value("${github.token}")
    private String githubToken;

    private final RestTemplate restTemplate;

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    // Fetch repo metadata — stars, forks, license etc.
    public GitHubRepoDto fetchRepo(String owner, String repo) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo;
        try {
            ResponseEntity<GitHubRepoDto> response = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(authHeaders()),
                    GitHubRepoDto.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.warn("Failed to fetch repo {}/{}: {}", owner, repo, e.getStatusCode());
            return null;
        }
    }

    // Fetch and decode README content
    public String fetchReadme(String owner, String repo) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/readme";
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(authHeaders()),
                    Map.class);
            if (response.getBody() != null && response.getBody().containsKey("content")) {
                String encoded = (String) response.getBody().get("content");
                // GitHub returns base64 encoded content
                byte[] decoded = Base64.getMimeDecoder().decode(encoded);
                return new String(decoded);
            }
        } catch (HttpClientErrorException e) {
            log.warn("Failed to fetch README for {}/{}: {}", owner, repo, e.getStatusCode());
        }
        return null;
    }
}