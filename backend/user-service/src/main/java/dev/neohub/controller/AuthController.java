package dev.neohub.controller;

import dev.neohub.config.JwtConfig;
import dev.neohub.dto.AuthResponse;
import dev.neohub.dto.UserDto;
import dev.neohub.service.AuthService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "GitHub OAuth and JWT endpoints")
public class AuthController {

    private final AuthService authService;
    private final JwtConfig jwtConfig;

    @PostMapping("/github")
    @Operation(summary = "Login with GitHub OAuth code")
    public ResponseEntity<AuthResponse> githubLogin(
            @RequestParam
            @NotBlank(message = "OAuth code is required")
            String code) {
        return ResponseEntity.ok(authService.loginWithGitHub(code));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged in user")
    public ResponseEntity<UserDto> getMe(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtConfig.validateToken(token);
            return ResponseEntity.ok(authService.getMe(claims.getSubject()));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }
}