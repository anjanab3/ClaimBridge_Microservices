package com.cts.identity.service;

import com.cts.identity.dto.UserDTO;
import com.cts.identity.entity.User;
import com.cts.identity.repository.UserRepository;
import com.cts.identity.util.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Page<UserDTO> findAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(this::toDTO);
    }

    public UserDTO findUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
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

    private UserDTO toDTO(User u) {
        return new UserDTO(u.getUserId(), u.getUsername(), u.getEmail(),
                u.getPhone(), u.getRole().name(), u.getStatus().name());
    }
}
