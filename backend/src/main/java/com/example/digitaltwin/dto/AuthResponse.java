package com.example.digitaltwin.dto;
public record AuthResponse(String token,String tokenType,long expiresIn,String email,String role,Long studentId) {}
