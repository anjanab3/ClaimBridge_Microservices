package com.cts.identity.service;

import com.cts.identity.entity.Claimant;
import com.cts.identity.entity.RoleSequence;
import com.cts.identity.entity.User;
import com.cts.identity.repository.ClaimantRepository;
import com.cts.identity.repository.RoleSequenceRepository;
import com.cts.identity.repository.UserRepository;
import com.cts.identity.util.Role;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleSequenceRepository roleSequenceRepository;

    @Autowired
    private ClaimantRepository claimantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User save(User user) {
        log.info("Registering new user with username={} role={}", user.getUsername(), user.getRole());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserId(generateUserId(user.getRole()));
        User saved = userRepository.save(user);
        log.info("User created with userId={}", saved.getUserId());
        return saved;
    }

    @Transactional
    public User saveWithClaimant(User user, String name) {
        log.info("Registering claimant user username={} name={}", user.getUsername(), name);
        User saved = save(user);
        Claimant claimant = new Claimant();
        claimant.setUserId(saved.getUserId());
        claimant.setName(name);
        claimant.setEmail(saved.getEmail());
        claimant.setPhone(saved.getPhone());
        claimantRepository.save(claimant);
        log.info("Claimant record created for userId={}", saved.getUserId());
        return saved;
    }

    private String generateUserId(Role role) {
        RoleSequence seq = roleSequenceRepository.findById(role)
                .orElse(new RoleSequence(role, 0));
        int next = seq.getLastSequence() + 1;
        seq.setLastSequence(next);
        roleSequenceRepository.save(seq);
        return String.format("%s-%04d", role.getPrefix(), next);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String changePassword(String username, String oldPassword, String newPassword) {
        log.info("Password change requested for username={}", username);
        User user = userRepository.findByUsername(username);
        if (user == null) { log.warn("Password change failed — user not found: {}", username); return "user not found"; }
        if (oldPassword.equals(newPassword)) return "old and new password should be different";
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) { log.warn("Password change failed — invalid credentials for username={}", username); return "invalid credentials"; }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for username={}", username);
        return "success";
    }
}
