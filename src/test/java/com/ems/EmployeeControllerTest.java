package com.ems;

import com.ems.controller.EmployeeController;
import com.ems.dto.EmployeeRequest;
import com.ems.dto.EmployeeResponse;
import com.ems.entity.EmployeeStatus;
import com.ems.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(com.ems.config.SecurityConfig.class)
class EmployeeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EmployeeService employeeService;
    @MockBean private com.ems.security.JwtAuthFilter jwtAuthFilter;
    @MockBean private com.ems.security.JwtUtils jwtUtils;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private EmployeeResponse sampleResponse;
    private EmployeeRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = EmployeeResponse.builder()
                .id(1L)
                .firstName("John").lastName("Doe")
                .email("john.doe@ems.com")
                .phone("1234567890")
                .position("Developer")
                .salary(new BigDecimal("75000"))
                .hireDate(LocalDate.now())
                .status(EmployeeStatus.ACTIVE)
                .departmentId(1L)
                .departmentName("Engineering")
                .createdAt(LocalDateTime.now())
                .build();

        sampleRequest = new EmployeeRequest();
        sampleRequest.setFirstName("John");
        sampleRequest.setLastName("Doe");
        sampleRequest.setEmail("john.doe@ems.com");
        sampleRequest.setPhone("1234567890");
        sampleRequest.setPosition("Developer");
        sampleRequest.setSalary(new BigDecimal("75000"));
        sampleRequest.setHireDate(LocalDate.now());
        sampleRequest.setStatus(EmployeeStatus.ACTIVE);
        sampleRequest.setDepartmentId(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllEmployees_shouldReturn200() throws Exception {
        Page<EmployeeResponse> page = new PageImpl<>(List.of(sampleResponse));
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].email").value("john.doe@ems.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployeeById_shouldReturn200_whenExists() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_shouldReturn201_whenValid() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@ems.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_shouldReturn400_whenMissingFields() throws Exception {
        EmployeeRequest bad = new EmployeeRequest(); // all blank

        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEmployee_shouldReturn200_whenValid() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEmployee_shouldReturn200_whenExists() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Employee deleted"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deleteEmployee_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/employees/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
