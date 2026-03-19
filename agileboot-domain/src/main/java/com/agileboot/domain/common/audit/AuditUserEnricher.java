package com.agileboot.domain.common.audit;

import com.agileboot.domain.common.cache.CacheCenter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * 统一回填 DTO 中的创建人和更新人用户名
 *
 * @author Codex
 */
@Component
public class AuditUserEnricher {

    public void enrich(AuditableDTO dto) {
        if (dto == null) {
            return;
        }
        enrich(java.util.List.of(dto));
    }

    public void enrich(Collection<? extends AuditableDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        Set<Long> userIds = new LinkedHashSet<>();
        for (AuditableDTO dto : dtos) {
            if (dto == null) {
                continue;
            }
            Stream.of(dto.getCreatorId(), dto.getUpdaterId())
                .filter(Objects::nonNull)
                .forEach(userIds::add);
        }

        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, String> userNameMap = new LinkedHashMap<>();
        for (Long userId : userIds) {
            userNameMap.put(userId, resolveUsername(userId));
        }

        for (AuditableDTO dto : dtos) {
            if (dto == null) {
                continue;
            }
            dto.setCreatorName(userNameMap.get(dto.getCreatorId()));
            dto.setUpdaterName(userNameMap.get(dto.getUpdaterId()));
        }
    }

    private String resolveUsername(Long userId) {
        return CacheCenter.getUsernameById(userId);
    }

}
