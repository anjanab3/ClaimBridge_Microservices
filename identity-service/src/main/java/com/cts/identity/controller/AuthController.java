package com.cts.identity.controller;

import com.cts.identity.dto.AuthRequestDTO;
import com.cts.identity.dto.AuthResponseDTO;
import com.cts.identity.dto.CreateUserRequestDTO;
import com.cts.identity.dto.MessageDTO;
import com.cts.identity.dto.PasswordChangeDTO;
import com.cts.identity.dto.ResponseDTO;
import com.cts.identity.entity.User;
import com.cts.identity.security.JwtService;
import com.cts.identity.service.AuthService;
import com.cts.identity.util.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/identity/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO req) {
        User user = authService.findByEmail(req.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO("No account found with email: " + req.getEmail()));
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO("Incorrect password"));
        }
        return ResponseEntity.ok(new AuthResponseDTO(jwtService.generateToken(user)));
    }

    // Public self-registration — no auth required.
    // Only USER role is permitted; admin creates other roles via /register.
    @PostMapping("/register/public")
    public ResponseEntity<?> registerPublic(@RequestBody CreateUserRequestDTO req) {
        // Force USER role — prevent privilege escalation via self-registration
        req.setRole(com.cts.identity.util.Role.USER);
        return register(req);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequestDTO req) {
        if (authService.findByUsername(req.getUsername()) != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDTO("Username already exists"));
        if (authService.findByEmail(req.getEmail()) != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDTO("Email already exists"));

        try {
            User user = new User();
            user.setUsername(req.getUsername());
            user.setEmail(req.getEmail());
            user.setPhone(req.getPhone());
            user.setPassword(req.getPassword());
            user.setRole(req.getRole());
            user.setStatus(UserStatus.ACTIVE);
            if (req.getHolderId() != null) {
                user.setHolderId(req.getHolderId());
            }
            // If name is provided this is a claimant registration — save to both users and claimants tables
            User saved = (req.getName() != null && !req.getName().isBlank())
                    ? authService.saveWithClaimant(user, req.getName())
                    : authService.save(user);
            return ResponseEntity.ok(new MessageDTO(saved, "User created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    @PutMapping("/password-reset")
    public ResponseEntity<?> passwordReset(@RequestBody PasswordChangeDTO req) {
        String result = authService.changePassword(req.getUsername(), req.getOldPassword(), req.getNewPassword());
        return switch (result) {
            case "success"                           -> ResponseEntity.ok(new ResponseDTO("Password changed successfully"));
            case "old and new password should be different" -> ResponseEntity.badRequest().body(new ResponseDTO("New password must differ from old password"));
            case "invalid credentials"               -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("Old password is incorrect"));
            default                                  -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(result));
        };
    }
}
