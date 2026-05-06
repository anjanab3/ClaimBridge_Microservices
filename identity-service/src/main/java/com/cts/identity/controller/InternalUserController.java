package com.cts.identity.controller;

import com.cts.identity.dto.UserDTO;
import com.cts.identity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal service-to-service endpoints for user lookups — no JWT required.
 * Only called by other microservices (e.g. claims-service via Feign).
 */
@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

    @Autowired
    private UserService userService;

    /** Get a single user by their userId (e.g. "CA-0001") */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(userService.findUserById(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Get a single user by their username */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userService.findUserByUsername(username));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Get all active users for a given role — e.g. CLAIMS_ADJUSTER, FRAUD_ANALYST */
    @GetMapping("/by-role")
    public List<UserDTO> getUsersByRole(@RequestParam String role) {
        return userService.findActiveByRole(role);
    }
}
