package com.cts.report.repository;

import com.cts.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByScopeIgnoreCase(String scope, Pageable pageable);

    Page<Report> findByScopeIgnoreCaseAndGeneratedAtBetween(
            String scope, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
