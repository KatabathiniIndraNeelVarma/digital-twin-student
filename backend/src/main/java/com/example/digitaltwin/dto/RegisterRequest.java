package com.example.digitaltwin.dto;
import jakarta.validation.constraints.*;
public record RegisterRequest(@Email @NotBlank String email, @NotBlank @Size(min=12,max=128) String password,
                              @NotBlank @Size(max=180) String fullName,
                              @NotBlank @Size(max=80) String studentNumber,
                              @Size(max=160) String program, @Min(1) @Max(16) Integer semester) {}
