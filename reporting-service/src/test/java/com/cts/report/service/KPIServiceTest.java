package com.cts.report.service;

import com.cts.report.entity.KPI;
import com.cts.report.repository.KPIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KPIServiceTest {

    @Mock private KPIRepository kpiRepository;

    @InjectMocks private KPIService kpiService;

    private KPI kpi;

    @BeforeEach
    void setUp() {
        kpi = new KPI();
        kpi.setKpiId(1L);
        kpi.setName("CLAIM_BACKLOG");
        kpi.setCurrentValue(BigDecimal.valueOf(10));
        kpi.setTarget(BigDecimal.valueOf(50));
        kpi.setReportingPeriod("DAILY");
    }

    @Test
    void seedKPIs_ShouldNotSeed_WhenKPIsAlreadyExist() {
        when(kpiRepository.count()).thenReturn(9L);

        kpiService.seedKPIs();

        verify(kpiRepository, never()).saveAll(anyList());
    }

    @Test
    void seedKPIs_ShouldSeedAll_WhenRepositoryIsEmpty() {
        when(kpiRepository.count()).thenReturn(0L);

        kpiService.seedKPIs();

        verify(kpiRepository).saveAll(anyList());
    }

    @Test
    void getAllKPIs_ShouldReturnKPIList() {
        when(kpiRepository.findAll()).thenReturn(List.of(kpi));

        var result = kpiService.getAllKPIs();

        assertEquals(1, result.size());
        verify(kpiRepository).findAll();
    }

    @Test
    void incrementKpi_ShouldAddOne_WhenKPIExists() {
        when(kpiRepository.findByNameIgnoreCase("CLAIM_BACKLOG")).thenReturn(Optional.of(kpi));

        kpiService.incrementKpi("CLAIM_BACKLOG");

        assertEquals(BigDecimal.valueOf(11), kpi.getCurrentValue());
        verify(kpiRepository).save(kpi);
    }

    @Test
    void incrementKpi_ShouldDoNothing_WhenKPINotFound() {
        when(kpiRepository.findByNameIgnoreCase("UNKNOWN_KPI")).thenReturn(Optional.empty());

        // Should not throw
        assertDoesNotThrow(() -> kpiService.incrementKpi("UNKNOWN_KPI"));
        verify(kpiRepository, never()).save(any());
    }

    @Test
    void updateTarget_ShouldUpdateAndSave_WhenKPIExists() {
        when(kpiRepository.findById(1L)).thenReturn(Optional.of(kpi));

        kpiService.updateTarget(1L, BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(100), kpi.getTarget());
        verify(kpiRepository).save(kpi);
    }

    @Test
    void updateTarget_ShouldThrow_WhenKPINotFound() {
        when(kpiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> kpiService.updateTarget(99L, BigDecimal.valueOf(100)));
    }
}
