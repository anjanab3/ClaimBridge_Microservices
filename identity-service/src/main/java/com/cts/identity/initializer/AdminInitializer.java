package com.cts.identity.initializer;

import com.cts.identity.entity.User;
import com.cts.identity.repository.UserRepository;
import com.cts.identity.util.Role;
import com.cts.identity.util.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userRepository.findByUsername("admin");
        if (admin == null) {
            admin = new User();
            admin.setUserId("ADM-0001");
            admin.setUsername("admin");
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
        }
        // Always overwrite — guarantees a clean known state on every startup
        admin.setEmail("admin@claimbridge.com");
        admin.setPhone("0000000000");
        admin.setPassword(passwordEncoder.encode("admin123"));
        userRepository.save(admin);
        System.out.println("✓ Admin ready — email: admin@claimbridge.com  password: admin123");
    }
}
