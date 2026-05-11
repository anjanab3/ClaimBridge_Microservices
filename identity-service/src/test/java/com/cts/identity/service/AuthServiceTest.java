package com.cts.identity.service;

import com.cts.identity.entity.RoleSequence;
import com.cts.identity.entity.User;
import com.cts.identity.repository.ClaimantRepository;
import com.cts.identity.repository.RoleSequenceRepository;
import com.cts.identity.repository.UserRepository;
import com.cts.identity.util.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleSequenceRepository roleSequenceRepository;
    @Mock private ClaimantRepository claimantRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPhone("9999999999");
        user.setPassword("plainPassword");
        user.setRole(Role.USER);
    }

    @Test
    void save_ShouldEncodePasswordAndGenerateUserId() {
        RoleSequence seq = new RoleSequence(Role.USER, 0);
        when(roleSequenceRepository.findById(Role.USER)).thenReturn(Optional.of(seq));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.save(user);

        assertEquals("encodedPassword", saved.getPassword());
        assertNotNull(saved.getUserId());
        verify(userRepository).save(user);
    }

    @Test
    void save_ShouldGenerateUserId_WhenNoSequenceExists() {
        when(roleSequenceRepository.findById(Role.USER)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.save(user);

        // Sequence starts at 1 when no prior record exists
        assertTrue(saved.getUserId().contains("0001"));
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        when(userRepository.findByUsername("john")).thenReturn(user);

        User result = authService.findByUsername("john");

        assertNotNull(result);
        assertEquals("john", result.getUsername());
    }

    @Test
    void findByUsername_ShouldReturnNull_WhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);

        User result = authService.findByUsername("unknown");

        assertNull(result);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(user);

        User result = authService.findByEmail("john@example.com");

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void changePassword_ShouldReturnSuccess_WhenCredentialsAreValid() {
        user.setPassword("encodedOld");
        when(userRepository.findByUsername("john")).thenReturn(user);
        when(passwordEncoder.matches("oldPass", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");

        String result = authService.changePassword("john", "oldPass", "newPass");

        assertEquals("success", result);
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ShouldReturnError_WhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(null);

        String result = authService.changePassword("ghost", "old", "new");

        assertEquals("user not found", result);
    }

    @Test
    void changePassword_ShouldReturnError_WhenOldAndNewPasswordAreSame() {
        when(userRepository.findByUsername("john")).thenReturn(user);

        String result = authService.changePassword("john", "samePass", "samePass");

        assertEquals("old and new password should be different", result);
    }

    @Test
    void changePassword_ShouldReturnError_WhenOldPasswordIsWrong() {
        user.setPassword("encodedOld");
        when(userRepository.findByUsername("john")).thenReturn(user);
        when(passwordEncoder.matches("wrongPass", "encodedOld")).thenReturn(false);

        String result = authService.changePassword("john", "wrongPass", "newPass");

        assertEquals("invalid credentials", result);
    }
}
