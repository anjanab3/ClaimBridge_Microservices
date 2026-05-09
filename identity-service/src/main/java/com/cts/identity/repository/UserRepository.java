package com.cts.identity.repository;

import com.cts.identity.entity.User;
import com.cts.identity.util.Role;
import com.cts.identity.util.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findByRoleAndStatus(Role role, UserStatus status);

    /** Server-side filtering: optional role + optional username/email search, paginated */
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByFilters(@Param("role") Role role,
                             @Param("search") String search,
                             Pageable pageable);
}
