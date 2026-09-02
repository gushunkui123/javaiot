package com.agileboot.domain.factorylink.plc.util;

import com.agileboot.domain.factorylink.plc.entity.FieldMappingEntity;
import lombok.Data;

/**
 * 字段匹配结果：命中的 mapping + 解析到的维度值。
 * 由 FieldMatchingEngine.matchField 产生。
 */
@Data
public final class MatchResult {
    private final FieldMappingEntity mapping;
    private final int stationNo;
    private final Integer gunNo;
    private final Integer stage;
    private final Integer idx;
    private final String side;

    public MatchResult(FieldMappingEntity mapping, int stationNo, Integer gunNo,
                       Integer stage, Integer idx, String side) {
        this.mapping = mapping;
        this.stationNo = stationNo;
        this.gunNo = gunNo;
        this.stage = stage;
        this.idx = idx;
        // side 优先从维度取，兜底从 match_pattern 前缀推导
        String resolvedSide = side;
        if (resolvedSide == null && mapping != null && mapping.getMatchPattern() != null) {
            String pat = mapping.getMatchPattern();
            if (pat.startsWith("左")) resolvedSide = "左";
            else if (pat.startsWith("右")) resolvedSide = "右";
        }
        this.side = resolvedSide;
    }
}
