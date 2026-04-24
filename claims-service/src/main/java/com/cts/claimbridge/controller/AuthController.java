package com.cts.claimbridge.controller;
import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.entity.User;
import com.cts.claimbridge.service.JwtService;
import com.cts.claimbridge.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private  AuthService authService;
    @Autowired
    private  JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request)
    {
        User user = authService.findByUsername(request.getUser().getUsername());
        if(user != null &&  new BCryptPasswordEncoder().matches(request.getUser().getPassword() , user.getPassword()))
        {
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(new AuthResponseDTO(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("Invalid Credentials"));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequestDTO request)
    {
        if (authService.findByUsername(request.getUser().getUsername()) != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ResponseDTO("Error: User Already Exists"));
        }
        if(authService.findByEmail(request.getUser().getEmail()) != null)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseDTO("Error : Email Already Exists"));
        }

        try
        {
            return ResponseEntity.ok(new MessageDTO(authService.save(request.getUser()),"User Created Successfully!!!"));
        }
        catch(RuntimeException e)
        {
             return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }

    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PutMapping("/password-reset")
    public ResponseEntity<?> passwordReset(@RequestBody PasswordChangeRequestDTO request)
    {
          String result = authService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
          if(result.equals("success"))
          {
               return ResponseEntity.ok().body(new ResponseDTO("Password Changed Successfully!!!"));
          }

        if (result.equals("old and new password should be different")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO("Password Changed Successfully!!!"));
        }

        if (result.equals("invalid credentials")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO(result));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

}



