package com.cts.report.repository;

import com.cts.report.entity.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KPIRepository extends JpaRepository<KPI, Long> {

    Optional<KPI> findByNameIgnoreCase(String name);
}
