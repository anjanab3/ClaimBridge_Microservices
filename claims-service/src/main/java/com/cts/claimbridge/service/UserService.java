package com.cts.claimbridge.service;

import com.cts.claimbridge.dto.UpdateProfileDTO;
import com.cts.claimbridge.dto.UserDTO;
import com.cts.claimbridge.entity.User;
import com.cts.claimbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;


    public Page<UserDTO> findAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findAll(pageable)
                .map(user -> new UserDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole().name()
                ));
    }

    public UserDTO findUserById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        return new UserDTO(user.getUserId() , user.getUsername() , user.getEmail() , user.getPhone() , user.getRole().name() );
    }

    public UserDTO updateUserProfile(Integer userId, UpdateProfileDTO request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        if (request.getEmail() != null) {
            existingUser.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            existingUser.setPhone(request.getPhone());
        }
        User updatedUser = userRepository.save(existingUser);
        return new UserDTO(updatedUser.getUserId(), updatedUser.getUsername() , updatedUser.getEmail() , updatedUser.getPhone(), updatedUser.getRole().name());
    }

    public void DeleteUser(int userId){
        userRepository.deleteById(userId);
    }

}
