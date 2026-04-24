package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.InvestigationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestigationNoteRepository extends JpaRepository<InvestigationNote, Long> {
    List<InvestigationNote> findByInvestigation_InvestigationIdOrderByCreatedAtAsc(Long investigationId); //A
    Optional<InvestigationNote> findByNoteIdAndInvestigation_InvestigationId(Long noteId,Long investigationId); //A
}