package com.hackathon.blockchain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegistration(@NotBlank @Email String email,
                               @NotBlank String username,
                               @NotBlank String password) {
}
