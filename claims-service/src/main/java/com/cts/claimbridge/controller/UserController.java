package com.cts.claimbridge.controller;
import com.cts.claimbridge.dto.MessageDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.dto.UpdateProfileDTO;
import com.cts.claimbridge.dto.UserDTO;
import com.cts.claimbridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping()
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer userid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if ("ALL_USERS".equalsIgnoreCase(type)) {
            if (!hasAuthority("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
            }
            return ResponseEntity.ok(userService.findAllUsers(page, size));
        }

        if (userid != null) {
            try {
                return ResponseEntity.ok(userService.findUserById(userid));
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Provide either type=ALL_USERS or userid=<id>");
    }

    private boolean hasAuthority(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PutMapping("/{userid}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Integer userid,@RequestBody UpdateProfileDTO request) {
        try {
            return ResponseEntity.ok().body(new MessageDTO(userService.updateUserProfile(userid, request),"Profile updated successfully !!!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO("No User Id found"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId)
    {
         UserDTO checkuser = userService.findUserById(userId);
         if(checkuser == null)
         {
             return ResponseEntity.badRequest().body("User Not Found");
         }
         else
         {
             userService.DeleteUser(userId);
         }
         return ResponseEntity.ok().body("User Deleted Successfully !!!");
    }
}
