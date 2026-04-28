package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.RoleSequence;
import com.cts.claimbridge.entity.User;
import com.cts.claimbridge.repository.RoleSequenceRepository;
import com.cts.claimbridge.repository.UserRepository;
import com.cts.claimbridge.util.Role;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleSequenceRepository roleSequenceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User save(User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setRoleCode(generateRoleCode(user.getRole()));
        return userRepository.save(user);
    }

    private String generateRoleCode(Role role) {
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
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "user not found";
        }
        if (oldPassword.equals(newPassword)) {
            return "old and new password should be different";
        }
        if (!new BCryptPasswordEncoder().matches(oldPassword, user.getPassword())) {
            return "invalid credentials";
        }
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);
        return "success";
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}