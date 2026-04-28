package com.cts.identity.repository;

import com.cts.identity.entity.RoleSequence;
import com.cts.identity.util.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleSequenceRepository extends JpaRepository<RoleSequence, Role> {
}
