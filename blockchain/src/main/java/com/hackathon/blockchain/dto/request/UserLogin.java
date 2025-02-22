package com.hackathon.blockchain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserLogin(@NotBlank String username,
                        @NotBlank String password) {
}
