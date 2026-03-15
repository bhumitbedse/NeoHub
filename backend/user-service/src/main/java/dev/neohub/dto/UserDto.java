package dev.neohub.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String githubUrl;
    private String bio;
    private String role;
    private OffsetDateTime createdAt;
}