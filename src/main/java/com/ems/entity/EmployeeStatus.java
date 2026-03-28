package com.ems.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeStatus {

    public static EmployeeStatus ACTIVE;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // ACTIVE, INACTIVE, ON_LEAVE, TERMINATED

    private String description;
}
