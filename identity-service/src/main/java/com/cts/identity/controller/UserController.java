package com.cts.identity.controller;

import com.cts.identity.dto.MessageDTO;
import com.cts.identity.dto.ResponseDTO;
import com.cts.identity.dto.UpdateProfileDTO;
import com.cts.identity.dto.UserDTO;
import com.cts.identity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity/users")
public class UserController {

    @Autowired private UserService userService;

    // Returns active users for a given role — used by fraud analyst adjuster dropdown
    @GetMapping("/by-role")
    public ResponseEntity<?> getUsersByRole(@RequestParam String role) {
        try {
            return ResponseEntity.ok(userService.findActiveByRole(role));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO("Unknown role: " + role));
        }
    }

    // Returns the currently authenticated user's profile (any role)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            return ResponseEntity.ok(userService.findUserByUsername(authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String userid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {

        if ("ALL_USERS".equalsIgnoreCase(type))
            return ResponseEntity.ok(userService.findAllUsers(page, size, role, search));

        if (userid != null) {
            try {
                return ResponseEntity.ok(userService.findUserById(userid));
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
            }
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Provide type=ALL_USERS or userid=<id>"));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable String userId, @RequestBody UpdateProfileDTO req) {
        try {
            UserDTO updated = userService.updateProfile(userId, req.getEmail(), req.getPhone(), req.getStatus());
            return ResponseEntity.ok(new MessageDTO(updated, "Profile updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            userService.findUserById(userId); // throws if not found
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ResponseDTO("User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }
}
