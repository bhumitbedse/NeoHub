package dev.neohub.proxy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyService {

    private final RestTemplate restTemplate;

    public ResponseEntity<String> forward(
            HttpServletRequest request,
            String targetUrl,
            Object body) {

        try {
            // Copy headers from original request
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (!name.equalsIgnoreCase("host")) {
                    headers.put(name, Collections.list(
                            request.getHeaders(name)));
                }
            }
            headers.set("X-Forwarded-For", request.getRemoteAddr());

            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(
                    URI.create(targetUrl),
                    method,
                    entity,
                    String.class);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Proxy error forwarding to {}: {}", targetUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"Service unavailable\"}");
        }
    }
}