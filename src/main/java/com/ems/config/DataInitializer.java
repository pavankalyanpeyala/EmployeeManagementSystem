package com.ems.config;

import com.ems.entity.Department;
import com.ems.entity.EmployeeStatus;
import com.ems.entity.Role;
import com.ems.entity.User;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeStatusRepository;
import com.ems.repository.RoleRepository;
import com.ems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedEmployeeStatuses();
        seedAdminUsers();
        seedDepartments();
    }

    // Step 1: seed roles table first
    private void seedRoles() {
        String[][] roles = {
            {"ROLE_ADMIN",    "Full system access"},
            {"ROLE_MANAGER",  "Read and write access"},
            {"ROLE_EMPLOYEE", "Read-only access"}
        };
        for (String[] r : roles) {
            if (roleRepository.findByName(r[0]).isEmpty()) {
                roleRepository.save(
                    Role.builder().name(r[0]).description(r[1]).build()
                );
            }
        }
        log.info("Roles seeded.");
    }

    // Step 2: seed employee_statuses table
    private void seedEmployeeStatuses() {
        String[][] statuses = {
            {"ACTIVE",     "Currently employed"},
            {"INACTIVE",   "Temporarily not working"},
            {"ON_LEAVE",   "On approved leave"},
            {"TERMINATED", "No longer employed"}
        };
        for (String[] s : statuses) {
            if (employeeStatusRepository.findByName(s[0]).isEmpty()) {
                employeeStatusRepository.save(
                    EmployeeStatus.builder().name(s[0]).description(s[1]).build()
                );
            }
        }
        log.info("Employee statuses seeded.");
    }

    // Step 3: seed users — fetch Role entity from DB by name
    private void seedAdminUsers() {
        Role adminRole   = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
        Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                .orElseThrow(() -> new RuntimeException("ROLE_MANAGER not found"));

        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@ems.com")
                    .role(adminRole)
                    .build());
            log.info("Default admin user created: username=admin, password=admin123");
        }

        if (!userRepository.existsByUsername("manager")) {
            userRepository.save(User.builder()
                    .username("manager")
                    .password(passwordEncoder.encode("manager123"))
                    .email("manager@ems.com")
                    .role(managerRole)
                    .build());
            log.info("Default manager user created: username=manager, password=manager123");
        }
    }

    // Step 4: seed departments
    private void seedDepartments() {
        String[][] departments = {
            {"Engineering",     "Software development and infrastructure"},
            {"Human Resources", "Recruitment and employee relations"},
            {"Finance",         "Accounting and financial planning"},
            {"Marketing",       "Brand and digital marketing"},
            {"Operations",      "Business operations and logistics"}
        };
        for (String[] dept : departments) {
            if (!departmentRepository.existsByName(dept[0])) {
                departmentRepository.save(
                    Department.builder().name(dept[0]).description(dept[1]).build()
                );
            }
        }
        log.info("Default departments seeded.");
    }
}