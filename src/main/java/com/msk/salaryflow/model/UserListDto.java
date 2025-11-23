package com.msk.salaryflow.model;

import com.msk.salaryflow.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserListDto implements Serializable {
    private UUID id;
    private String username;
    private boolean enabled;
    private Instant createdAt;
    private String roles;
    public UserListDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.enabled = user.isEnabled();
        this.createdAt = user.getCreatedAt();
        this.roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.joining(", "));
    }
}