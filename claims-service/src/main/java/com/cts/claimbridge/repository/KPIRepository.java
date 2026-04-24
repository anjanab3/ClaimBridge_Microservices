package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.KPI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KPIRepository extends JpaRepository<KPI, Long> {

    Page<KPI> findAll(Pageable pageable);

    Page<KPI> findByReportingPeriod(String period, Pageable pageable);

    List<KPI> findByNameContainingIgnoreCase(String name);

}
