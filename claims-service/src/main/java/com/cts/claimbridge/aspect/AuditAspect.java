package com.cts.claimbridge.aspect;

import com.cts.claimbridge.client.ReportingServiceClient;
import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.FraudAlertRepository;
import com.cts.claimbridge.repository.InvestigationRepository;
import com.cts.claimbridge.service.JwtService;
import com.cts.claimbridge.util.ClaimStatus;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Centralised audit aspect — intercepts key service methods and fires
 * audit events to reporting-service. Zero changes required in any service class.
 * All audit data is stored exclusively in reporting-service's database.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)   // outer proxy → fires AFTER @Transactional commits
public class AuditAspect {

    @Autowired private ReportingServiceClient  reportingServiceClient;
    @Autowired private JwtService              jwtService;
    @Autowired private ClaimRepository         claimRepository;
    @Autowired private InvestigationRepository investigationRepository;
    @Autowired private FraudAlertRepository    fraudAlertRepository;

    // ── Claim submitted ───────────────────────────────────────────────────────
    @AfterReturning(
            pointcut = "execution(* com.cts.claimbridge.service.ClaimService.save(..))",
            returning = "result")
    public void afterClaimSaved(Object result) {
        try {
            Claim claim = (Claim) result;
            fireAudit("Claim", claim.getClaimId(), "CREATE",
                    "Claim " + claim.getClaimId() + " submitted with status " + claim.getStatus());
        } catch (Exception ignored) {}
    }

    // ── Claim status changed (updateClaimStatus / validateClaim / updateStatus) ─
    @Around("execution(* com.cts.claimbridge.service.ClaimService.updateClaimStatus(..)) || " +
            "execution(* com.cts.claimbridge.service.ClaimService.validateClaim(..))     || " +
            "execution(* com.cts.claimbridge.service.ClaimService.updateStatus(..))")
    public Object aroundClaimStatusChange(ProceedingJoinPoint pjp) throws Throwable {
        Long       claimId   = (Long)       pjp.getArgs()[0];
        ClaimStatus newStatus = (ClaimStatus) pjp.getArgs()[1];

        String prevStatus = claimRepository.findById(claimId)
                .map(c -> c.getStatus() != null ? c.getStatus().name() : "UNKNOWN")
                .orElse("UNKNOWN");

        Object result = pjp.proceed();

        try {
            Claim claim = claimRepository.findById(claimId).orElse(null);
            fireClaimEvent(claimId, prevStatus, newStatus.name(),
                    claim != null ? claim.getLossType() : null,
                    claim != null ? claim.getEstimatedAmount() : null);
        } catch (Exception ignored) {}

        return result;
    }

    // ── Triage decision created ───────────────────────────────────────────────
    @Around("execution(* com.cts.claimbridge.service.TriageDecisionService.createDecision(..))")
    public Object aroundCreateDecision(ProceedingJoinPoint pjp) throws Throwable {
        TriageDecisionRequestDTO request = (TriageDecisionRequestDTO) pjp.getArgs()[0];

        // Capture previous claim status before the method runs
        String prevStatus = claimRepository.findById(request.getClaimId())
                .map(c -> c.getStatus() != null ? c.getStatus().name() : "UNKNOWN")
                .orElse("UNKNOWN");

        Object result = pjp.proceed();

        TriageDecisionResponseDTO dto = (TriageDecisionResponseDTO) result;

        // 1. Claim moved to IN_REVIEW — fires KPI update + notification too
        // Isolated: KPI failure in reporting-service must NOT prevent the triage audit entry
        try {
            Claim claim = claimRepository.findById(dto.getClaimId()).orElse(null);
            fireClaimEvent(dto.getClaimId(), prevStatus, "IN_REVIEW",
                    claim != null ? claim.getLossType() : null,
                    claim != null ? claim.getEstimatedAmount() : null);
        } catch (Exception ignored) {}

        // 2. Triage action itself — always fires regardless of step 1
        try {
            fireAudit("Claim", dto.getClaimId(), "TRIAGE",
                    "Triage decision " + dto.getDecisionId() + " created for Claim " + dto.getClaimId()
                            + " — routed to " + dto.getAssignedQueue()
                            + " queue (assigned to " + dto.getAssignedTo() + ")");
        } catch (Exception ignored) {}

        // 3. If FRAUD queue, log the auto-created FraudAlert — always fires regardless of step 2
        try {
            if ("FRAUD".equalsIgnoreCase(dto.getAssignedQueue())) {
                fraudAlertRepository.findByClaim_ClaimId(dto.getClaimId())
                        .stream().findFirst()
                        .ifPresent(alert -> fireAudit("FraudAlert", alert.getAlertId(), "CREATE",
                                "FraudAlert " + alert.getAlertId() + " created for Claim "
                                        + dto.getClaimId() + " — flagged and routed to FRAUD queue"));
            }
        } catch (Exception ignored) {}

        return result;
    }

    // ── Investigation status changed ──────────────────────────────────────────
    @Around("execution(* com.cts.claimbridge.service.InvestigationService.updateInvestigationAndCreateSettlement(..))")
    public Object aroundInvestigationUpdate(ProceedingJoinPoint pjp) throws Throwable {
        Long investigationId = (Long) pjp.getArgs()[0];

        String prevStatus = investigationRepository.findById(investigationId)
                .map(i -> i.getStatus() != null ? i.getStatus().name() : "UNKNOWN")
                .orElse("UNKNOWN");

        Long claimId = investigationRepository.findById(investigationId)
                .map(i -> i.getClaim().getClaimId())
                .orElse(null);

        Object result = pjp.proceed();

        try {
            InvUpdateResponseDTO dto = (InvUpdateResponseDTO) result;
            fireInvestigationEvent(investigationId, claimId, prevStatus, dto.getStatus());
        } catch (Exception ignored) {}

        return result;
    }

    // ── FraudAlert: takeDecision / updateAlert / reassignToAdjuster ───────────
    @AfterReturning(
            pointcut = "execution(* com.cts.claimbridge.service.FraudAnalystService.takeDecision(..))       || " +
                       "execution(* com.cts.claimbridge.service.FraudAnalystService.updateAlert(..))        || " +
                       "execution(* com.cts.claimbridge.service.FraudAnalystService.reassignToAdjuster(..))",
            returning = "result")
    public void afterFraudAlertAction(JoinPoint jp, Object result) {
        try {
            FraudAnalystResponseDTO dto    = (FraudAnalystResponseDTO) result;
            String                  method = jp.getSignature().getName();

            String details = switch (method) {
                case "takeDecision" -> {
                    FraudAnalystRequestDTO req = (FraudAnalystRequestDTO) jp.getArgs()[1];
                    yield switch (req.getDecision().toUpperCase()) {
                        case "CLEAR"    -> "FraudAlert " + dto.getAlertId() + " (Claim " + dto.getClaimId()
                                            + ") cleared as non-fraudulent — routed to ADJUSTER queue";
                        case "ESCALATE" -> "FraudAlert " + dto.getAlertId() + " (Claim " + dto.getClaimId()
                                            + ") escalated to senior fraud analyst review";
                        case "REJECT"   -> "FraudAlert " + dto.getAlertId() + " (Claim " + dto.getClaimId()
                                            + ") rejected — claim confirmed fraudulent and denied";
                        default         -> "FraudAlert " + dto.getAlertId() + " decision: " + req.getDecision();
                    };
                }
                case "reassignToAdjuster" -> "FraudAlert " + dto.getAlertId() + " (Claim " + dto.getClaimId()
                                              + ") reassigned to adjuster";
                default -> "FraudAlert " + dto.getAlertId() + " (Claim " + dto.getClaimId()
                            + ") status updated to " + dto.getStatus();
            };

            fireAudit("FraudAlert", dto.getAlertId(), "STATUS_CHANGE", details);
        } catch (Exception ignored) {}
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void fireClaimEvent(Long claimId, String previousStatus, String newStatus,
                                String lossType, Double estimatedAmount) {
        ClaimEventDTO event = new ClaimEventDTO();
        event.setClaimId(claimId);
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setLossType(lossType);
        event.setEstimatedAmount(estimatedAmount);
        event.setUserId(getCurrentUserId());
        event.setChangedAt(LocalDateTime.now());
        reportingServiceClient.onClaimStatusChanged(event);
    }

    private void fireInvestigationEvent(Long investigationId, Long claimId,
                                        String previousStatus, String newStatus) {
        InvestigationEventDTO event = new InvestigationEventDTO();
        event.setInvestigationId(investigationId);
        event.setClaimId(claimId);
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setUserId(getCurrentUserId());
        event.setChangedAt(LocalDateTime.now());
        reportingServiceClient.onInvestigationChanged(event);
    }

    private void fireAudit(String resource, Long resourceId, String action, String details) {
        AuditEventDTO event = new AuditEventDTO();
        event.setUserId(getCurrentUserId());
        event.setResource(resource);
        event.setResourceId(resourceId);
        event.setAction(action);
        event.setDetails(details);
        reportingServiceClient.logAudit(event);
    }

    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            String auth = attrs.getRequest().getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) return null;
            String userId = jwtService.extractUserId(auth.substring(7));
            return userId != null ? Long.parseLong(userId) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
