package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.RoleSequence;
import com.cts.claimbridge.util.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleSequenceRepository extends JpaRepository<RoleSequence, Role> {

}