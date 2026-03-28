package com.ems.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Valid email is required")
    @NotBlank
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Position is required")
    private String position;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", message = "Salary must be positive")
    private BigDecimal salary;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    // Status name: ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
    private String statusName = "ACTIVE";

    @NotNull(message = "Department ID is required")
    private Long departmentId;
}