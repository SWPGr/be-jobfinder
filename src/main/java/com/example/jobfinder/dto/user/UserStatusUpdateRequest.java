package com.example.jobfinder.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusUpdateRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Active status cannot be null")
    private Boolean isActive;
}