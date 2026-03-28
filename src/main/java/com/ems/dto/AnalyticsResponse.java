package com.ems.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {
    private long totalEmployees;
    private long activeEmployees;
    private long inactiveEmployees;
    private long onLeaveEmployees;
    private long terminatedEmployees;
    private BigDecimal averageSalary;
    private BigDecimal totalSalaryBudget;
    private long totalDepartments;
    private Map<String, Long> employeesByDepartment;
    private Map<String, Long> employeesByStatus;
}