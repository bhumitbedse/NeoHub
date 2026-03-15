package dev.neohub.controller;

import dev.neohub.config.JwtConfig;
import dev.neohub.dto.AuthResponse;
import dev.neohub.dto.UserDto;
import dev.neohub.service.AuthService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "GitHub OAuth and JWT endpoints")
public class AuthController {

    private final AuthService authService;
    private final JwtConfig jwtConfig;

    @PostMapping("/github")
    @Operation(summary = "Login with GitHub OAuth code")
    public ResponseEntity<AuthResponse> githubLogin(@RequestParam String code) {
        return ResponseEntity.ok(authService.loginWithGitHub(code));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged in user")
    public ResponseEntity<UserDto> getMe(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtConfig.validateToken(token);
        return ResponseEntity.ok(authService.getMe(claims.getSubject()));
    }
}