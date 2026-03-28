package com.ems.repository;

import com.ems.entity.Employee;
import com.ems.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.position) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Employee> searchEmployees(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(EmployeeStatus status);

    long countByDepartmentId(Long departmentId);

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.status.name = 'ACTIVE'")
    BigDecimal findAverageSalary();

    @Query("SELECT d.name, COUNT(e) FROM Employee e JOIN e.department d GROUP BY d.name")
    List<Object[]> countEmployeesByDepartment();

    @Query("SELECT e.status.name, COUNT(e) FROM Employee e GROUP BY e.status.name")
    List<Object[]> countEmployeesByStatus();

    @Query("SELECT SUM(e.salary) FROM Employee e WHERE e.status.name = 'ACTIVE'")
    BigDecimal findTotalSalaryBudget();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status.name = :statusName")
    long countByStatusName(@Param("statusName") String statusName);

    boolean existsByEmail(String email);
}
