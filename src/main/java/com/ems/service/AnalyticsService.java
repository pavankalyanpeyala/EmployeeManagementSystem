package com.ems.service;

import com.ems.dto.AnalyticsResponse;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public AnalyticsResponse getDashboardAnalytics() {
        long total      = employeeRepository.count();
        long active     = employeeRepository.countByStatusName("ACTIVE");
        long inactive   = employeeRepository.countByStatusName("INACTIVE");
        long onLeave    = employeeRepository.countByStatusName("ON_LEAVE");
        long terminated = employeeRepository.countByStatusName("TERMINATED");

        BigDecimal avgSalary   = employeeRepository.findAverageSalary();
        BigDecimal totalBudget = employeeRepository.findTotalSalaryBudget();
        long totalDepts        = departmentRepository.count();

        Map<String, Long> byDept   = toMap(employeeRepository.countEmployeesByDepartment());
        Map<String, Long> byStatus = toMap(employeeRepository.countEmployeesByStatus());

        log.debug("Analytics fetched: total={}, active={}", total, active);

        return AnalyticsResponse.builder()
                .totalEmployees(total)
                .activeEmployees(active)
                .inactiveEmployees(inactive)
                .onLeaveEmployees(onLeave)
                .terminatedEmployees(terminated)
                .averageSalary(avgSalary != null ? avgSalary : BigDecimal.ZERO)
                .totalSalaryBudget(totalBudget != null ? totalBudget : BigDecimal.ZERO)
                .totalDepartments(totalDepts)
                .employeesByDepartment(byDept)
                .employeesByStatus(byStatus)
                .build();
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return result;
    }
}