package com.ems;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ems.dto.LoginRequest;
import com.ems.dto.LoginResponse;
import com.ems.dto.RegisterRequest;
import com.ems.entity.Role;
import com.ems.entity.User;
import com.ems.exception.DuplicateResourceException;
import com.ems.repository.UserRepository;
import com.ems.security.JwtUtils;
import com.ems.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks private AuthService authService;

    private User user;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("admin")
                .password("encoded_password")
                .email("admin@ems.com")
                .role(Role.ROLE_ADMIN)
                .build();

        userDetails = new org.springframework.security.core.userdetails.User(
                "admin", "encoded_password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtils.generateToken(userDetails)).thenReturn("mocked.jwt.token");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getType()).isEqualTo("Bearer");
    }

    @Test
    void login_shouldThrow_whenBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void register_shouldCreateUser_whenValid() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@ems.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        User newUser = User.builder()
                .id(2L).username("newuser")
                .password("encoded_password")
                .email("new@ems.com")
                .role(Role.ROLE_EMPLOYEE)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("new@ems.com");
        request.setRole(Role.ROLE_EMPLOYEE);

        User result = authService.register(request);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_EMPLOYEE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenUsernameTaken() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setPassword("password123");
        request.setEmail("another@ems.com");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("admin");
    }

    @Test
    void register_shouldThrow_whenEmailTaken() {
        when(userRepository.existsByUsername("uniqueuser")).thenReturn(false);
        when(userRepository.existsByEmail("admin@ems.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("uniqueuser");
        request.setPassword("password123");
        request.setEmail("admin@ems.com");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("admin@ems.com");
    }
}
