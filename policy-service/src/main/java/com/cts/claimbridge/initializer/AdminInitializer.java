package com.cts.claimbridge.initializer;

import com.cts.claimbridge.entity.User;
import com.cts.claimbridge.repository.UserRepository;
import com.cts.claimbridge.util.Role;
import com.cts.claimbridge.util.UserStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

     @Override
    public void run(String... args)
     {
          if(userRepository.findByUsername("admin") == null)
          {
               User admin = new User();
               admin.setUsername("admin");
               admin.setPassword(passwordEncoder.encode("admin123"));
               admin.setRole(Role.ADMIN);
               admin.setRoleCode("ADMIN-0001");
               admin.setEmail("admin@gmail.com");
               admin.setPhone("123456789");
               admin.setStatus(UserStatus.ACTIVE);
               userRepository.save(admin);
               System.out.println("Default Admin created Successfully - admin / admin123");
          }
          else {
              System.out.println("Admin already exists. skipping initialization");
          }
     }

}
