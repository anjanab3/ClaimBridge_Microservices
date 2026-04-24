package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface  ReportRepository extends JpaRepository<Report, Long> {

    Report findByReportId(Long reportId);
    Page<Report> findByScope(String scope, Pageable pageable);
    Page<Report> findByScopeIgnoreCaseAndGeneratedAtBetween(String scope, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

}
