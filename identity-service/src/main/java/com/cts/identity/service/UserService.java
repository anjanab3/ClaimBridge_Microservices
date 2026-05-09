package com.cts.identity.service;

import com.cts.identity.dto.UserDTO;
import com.cts.identity.entity.User;
import com.cts.identity.repository.UserRepository;
import com.cts.identity.util.Role;
import com.cts.identity.util.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Page<UserDTO> findAllUsers(int page, int size, String roleName, String search) {
        Role role = (roleName != null && !roleName.isBlank()) ? Role.valueOf(roleName.toUpperCase()) : null;
        String searchTerm = (search != null && !search.isBlank()) ? search.trim() : null;
        return userRepository.findByFilters(role, searchTerm, PageRequest.of(page, size))
                .map(this::toDTO);
    }

    public UserDTO findUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return toDTO(user);
    }

    public UserDTO findUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("User not found: " + username);
        return toDTO(user);
    }

    public UserDTO updateProfile(String userId, String email, String phone, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        if (email  != null) user.setEmail(email);
        if (phone  != null) user.setPhone(phone);
        if (status != null) user.setStatus(UserStatus.valueOf(status));
        return toDTO(userRepository.save(user));
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    /** Returns all active users with the given role — used by fraud analyst adjuster dropdown */
    public List<UserDTO> findActiveByRole(String roleName) {
        Role role = Role.valueOf(roleName.toUpperCase());
        return userRepository.findByRoleAndStatus(role, UserStatus.ACTIVE)
                .stream().map(this::toDTO).toList();
    }

    private UserDTO toDTO(User u) {
        return new UserDTO(u.getUserId(), u.getUsername(), u.getEmail(),
                u.getPhone(), u.getRole().name(), u.getStatus().name(), u.getHolderId());
    }
}
