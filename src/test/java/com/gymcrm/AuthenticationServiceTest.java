package com.gymcrm;

import com.gymcrm.entity.User;
import com.gymcrm.exception.AuthenticationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.UserRepository;
import com.gymcrm.security.jwt.JwtService;
import com.gymcrm.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymMetricsService gymMetricsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticate_shouldPassWhenPasswordMatches() {
        User user = new User() {};
        user.setUsername("Oscar.Piastri");
        user.setPassword("$2a$10$encodedPassword");

        when(userRepository.findByUsername("Oscar.Piastri")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", "$2a$10$encodedPassword")).thenReturn(true);

        assertDoesNotThrow(() -> authenticationService.authenticate("Oscar.Piastri", "plainPassword"));

        verify(gymMetricsService).incrementLoginSuccess();
    }

    @Test
    void authenticate_shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("Missing.User", "pass"));

        verify(gymMetricsService).incrementLoginFailed();
    }

    @Test
    void authenticate_shouldThrowWhenPasswordDoesNotMatch() {
        User user = new User() {};
        user.setUsername("Oscar.Piastri");
        user.setPassword("$2a$10$encodedPassword");

        when(userRepository.findByUsername("Oscar.Piastri")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "$2a$10$encodedPassword")).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("Oscar.Piastri", "wrongPass"));

        verify(gymMetricsService).incrementLoginFailed();
    }

    @Test
    void login_shouldReturnJwtToken() {
        User user = new User() {};
        user.setUsername("Oscar.Piastri");
        user.setPassword("$2a$10$encodedPassword");

        when(userRepository.findByUsername("Oscar.Piastri")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("Oscar.Piastri")).thenReturn("jwt-token");

        String token = authenticationService.login("Oscar.Piastri", "plainPassword");

        assertEquals("jwt-token", token);
    }

    @Test
    void changePassword_shouldEncodeAndSaveNewPassword() {
        User user = new User() {};
        user.setUsername("Oscar.Piastri");
        user.setPassword("$2a$10$encodedOld");

        when(userRepository.findByUsername("Oscar.Piastri")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "$2a$10$encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("$2a$10$encodedNew");

        authenticationService.changePassword("Oscar.Piastri", "oldPass", "newPass");

        assertEquals("$2a$10$encodedNew", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void logout_shouldBlacklistToken() {
        authenticationService.logout("Bearer abc.def.ghi");

        assertTrue(authenticationService.isTokenBlacklisted("abc.def.ghi"));
    }

    @Test
    void logout_shouldThrowWhenHeaderIsInvalid() {
        assertThrows(AuthenticationException.class,
                () -> authenticationService.logout("abc.def.ghi"));
    }
}