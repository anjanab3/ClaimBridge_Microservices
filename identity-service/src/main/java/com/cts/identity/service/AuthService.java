package com.cts.identity.service;

import com.cts.identity.entity.PolicyHolder;
import com.cts.identity.entity.RoleSequence;
import com.cts.identity.entity.User;
import com.cts.identity.repository.PolicyHolderRepository;
import com.cts.identity.repository.RoleSequenceRepository;
import com.cts.identity.repository.UserRepository;
import com.cts.identity.util.Role;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PolicyHolderRepository policyHolderRepository;

    @Autowired
    private RoleSequenceRepository roleSequenceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User save(User user) {
        // Link to PolicyHolder if holderId provided
        if (user.getPolicyHolder() != null && user.getPolicyHolder().getHolderId() != null) {
            PolicyHolder holder = policyHolderRepository.findById(user.getPolicyHolder().getHolderId())
                    .orElseThrow(() -> new RuntimeException("PolicyHolder not found"));
            user.setPolicyHolder(holder);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserId(generateUserId(user.getRole()));
        return userRepository.save(user);
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
        User user = userRepository.findByUsername(username);
        if (user == null) return "user not found";
        if (oldPassword.equals(newPassword)) return "old and new password should be different";
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) return "invalid credentials";
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "success";
    }
}
