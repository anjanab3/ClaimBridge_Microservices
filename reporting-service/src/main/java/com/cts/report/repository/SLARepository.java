package com.cts.report.repository;

import com.cts.report.entity.SLA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SLARepository extends JpaRepository<SLA, Long> {

    List<SLA> findByActiveTrue();

    List<SLA> findByMonitoredEntityIgnoreCase(String monitoredEntity);
}
