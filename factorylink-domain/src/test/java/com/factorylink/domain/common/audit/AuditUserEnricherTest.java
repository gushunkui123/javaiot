package com.factorylink.domain.common.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factorylink.domain.business.formula.dto.FormulaDTO;
import com.factorylink.domain.business.material.dto.MaterialDTO;
import com.factorylink.domain.common.cache.CacheCenter;
import com.factorylink.infrastructure.cache.redis.RedisCacheTemplate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditUserEnricherTest {

    @SuppressWarnings("unchecked")
    private final RedisCacheTemplate<String> usernameCache = mock(RedisCacheTemplate.class);

    private final AuditUserEnricher auditUserEnricher = new AuditUserEnricher();

    private RedisCacheTemplate<String> originalUsernameCache;

    @BeforeEach
    void setUp() {
        originalUsernameCache = CacheCenter.usernameCache;
        CacheCenter.usernameCache = usernameCache;
    }

    @AfterEach
    void tearDown() {
        CacheCenter.usernameCache = originalUsernameCache;
    }

    @Test
    void enrichShouldPopulateAuditUserNamesByDistinctUserIds() {
        MaterialDTO materialDTO = new MaterialDTO();
        materialDTO.setCreatorId(10L);
        materialDTO.setUpdaterId(20L);

        FormulaDTO formulaDTO = new FormulaDTO();
        formulaDTO.setCreatorId(10L);
        formulaDTO.setUpdaterId(30L);

        when(usernameCache.getObjectById(10L)).thenReturn("creator-user");
        when(usernameCache.getObjectById(20L)).thenReturn("updater-user");
        when(usernameCache.getObjectById(30L)).thenReturn("reviewer-user");

        auditUserEnricher.enrich(List.<AuditableDTO>of(materialDTO, formulaDTO));

        assertEquals("creator-user", materialDTO.getCreatorName());
        assertEquals("updater-user", materialDTO.getUpdaterName());
        assertEquals("creator-user", formulaDTO.getCreatorName());
        assertEquals("reviewer-user", formulaDTO.getUpdaterName());
        verify(usernameCache, times(1)).getObjectById(10L);
        verify(usernameCache, times(1)).getObjectById(20L);
        verify(usernameCache, times(1)).getObjectById(30L);
    }

}
