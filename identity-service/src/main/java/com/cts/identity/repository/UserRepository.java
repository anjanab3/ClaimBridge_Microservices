package com.cts.identity.repository;

import com.cts.identity.entity.User;
import com.cts.identity.util.Role;
import com.cts.identity.util.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findByRoleAndStatus(Role role, UserStatus status);
}
