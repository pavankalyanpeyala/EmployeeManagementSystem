package com.ems.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100)
    private String password;

    @Email(message = "Valid email is required")
    @NotBlank
    private String email;

    // Role name: ROLE_ADMIN, ROLE_MANAGER, ROLE_EMPLOYEE
    private String roleName = "ROLE_EMPLOYEE";
}