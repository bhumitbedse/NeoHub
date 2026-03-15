package dev.neohub.service;

import dev.neohub.config.JwtConfig;
import dev.neohub.dto.AuthResponse;
import dev.neohub.dto.GitHubUserDto;
import dev.neohub.dto.UserDto;
import dev.neohub.entity.User;
import dev.neohub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final GitHubOAuthService gitHubOAuthService;
    private final JwtConfig jwtConfig;

    @Transactional
    public AuthResponse loginWithGitHub(String code) {
        // 1. Exchange code for GitHub access token
        String githubToken = gitHubOAuthService.exchangeCodeForToken(code);

        // 2. Fetch GitHub user profile
        GitHubUserDto githubUser = gitHubOAuthService.fetchGitHubUser(githubToken);

        // 3. Find or create user in our DB
        User user = findOrCreateUser(githubUser);

        // 4. Generate our JWT
        String jwt = jwtConfig.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name());

        log.info("User logged in: {}", user.getUsername());
        return new AuthResponse(jwt, toDto(user));
    }

    public UserDto getMe(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(user);
    }

    private User findOrCreateUser(GitHubUserDto githubUser) {
        Optional<User> existing = userRepository.findByGithubId(githubUser.getId());

        if (existing.isPresent()) {
            // Update profile info on every login
            User user = existing.get();
            user.setDisplayName(githubUser.getName());
            user.setEmail(githubUser.getEmail());
            user.setAvatarUrl(githubUser.getAvatarUrl());
            user.setBio(githubUser.getBio());
            return userRepository.save(user);
        }

        // Create new user
        User user = new User();
        user.setGithubId(githubUser.getId());
        user.setUsername(githubUser.getLogin());
        user.setDisplayName(githubUser.getName());
        user.setEmail(githubUser.getEmail());
        user.setAvatarUrl(githubUser.getAvatarUrl());
        user.setGithubUrl(githubUser.getHtmlUrl());
        user.setBio(githubUser.getBio());
        user.setRole(User.Role.USER);
        return userRepository.save(user);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setGithubUrl(user.getGithubUrl());
        dto.setBio(user.getBio());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}