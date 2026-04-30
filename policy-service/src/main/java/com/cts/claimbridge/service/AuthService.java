package com.cts.claimbridge.service;

import com.cts.claimbridge.dto.UserDTO;
import com.cts.claimbridge.entity.PolicyHolder;
import com.cts.claimbridge.entity.RoleSequence;
import com.cts.claimbridge.entity.User;
import com.cts.claimbridge.repository.PolicyHolderRepository;
import com.cts.claimbridge.repository.RoleSequenceRepository;
import com.cts.claimbridge.repository.UserRepository;
import com.cts.claimbridge.util.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PolicyHolderRepository policyHolderRepository;
    private final RoleSequenceRepository roleSequenceRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Register / Save User ──
    @Transactional
    public UserDTO save(User user) {
        // if (user.getPolicyHolder() != null && user.getPolicyHolder().getHolderId() != null) {
        //     PolicyHolder managedHolder = policyHolderRepository
        //             .findById(user.getPolicyHolder().getHolderId())
        //             .orElseThrow(() -> new RuntimeException(
        //                 "PolicyHolder not found with ID: " + user.getPolicyHolder().getHolderId()
        //             ));
        //     user.setPolicyHolder(managedHolder);
        // }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoleCode(generateRoleCode(user.getRole()));
        User saved = userRepository.save(user);
        return toDTO(saved);                    // ← return DTO not raw User
    }

    // ── Generate role code e.g. CA-0001 ──
    private String generateRoleCode(Role role) {
        RoleSequence seq = roleSequenceRepository.findById(role)
                .orElse(new RoleSequence(role, 0));
        int next = seq.getLastSequence() + 1;
        seq.setLastSequence(next);
        roleSequenceRepository.save(seq);
        return String.format("%s-%04d", role.getPrefix(), next);
    }

    // ── Find by username — used by Spring Security ──
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ── Find by email ──
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ── Change password ──
    public String changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return "User not found";
        }
        if (oldPassword.equals(newPassword)) {
            return "Old and new password should be different";
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "Invalid credentials";
        }

        user.setPassword(passwordEncoder.encode(newPassword));  // ← use injected encoder
        userRepository.save(user);
        return "Password changed successfully";
    }

    // ── Get all users as DTOs ──
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Private helper — User → UserDTO ──
    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getStatus() != null ? user.getStatus().name() : null,
                user.getRoleCode(),
                user.getHolderId()
        );
    }
}