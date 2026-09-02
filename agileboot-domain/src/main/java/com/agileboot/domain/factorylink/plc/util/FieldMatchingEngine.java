package com.agileboot.domain.factorylink.plc.util;

import com.agileboot.domain.factorylink.plc.entity.FieldMappingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * 第三方字段 ↔ internal_key 的匹配引擎。
 * <p>
 * 核心思路：field_mapping.match_pattern 支持占位符：
 * <ul>
 *   <li>{N} → 站位号 stationNo（纯数字）</li>
 *   <li>{gun} → 射枪号（纯数字）</li>
 *   <li>{stage} → 阶段号（纯数字）</li>
 *   <li>{side} → 模具侧（左/右）</li>
 * </ul>
 * 无占位符时用 equals 快速匹配；有占位符时转成命名正则分组并提取维度值。
 * </p>
 */
@Slf4j
public final class FieldMatchingEngine {

    private FieldMatchingEngine() {}

    /** 占位符名 → 对应的正则片段（编译阶段转换，避免第三方值里的标点干扰） */
    static final Map<String, String> PLACEHOLDER_REGEX = Map.ofEntries(
            Map.entry("N", "(?<N>\\d+)"),
            Map.entry("gun", "(?<gun>\\d+)"),
            Map.entry("idx", "(?<idx>\\d+)"),
            Map.entry("stage", "(?<stage>一|二|三|四|五|[1-5])"),
            Map.entry("side", "(?<side>左|右)")
    );

    static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z_]+)\\}");

    /**
     * 用预编译后的 mappings 列表匹配第三方中文原始字段名。
     * @param candidates 预编译列表（按顺序，第一个命中即返回）
     * @param thirdFieldKey 第三方返回的 fieldKey（中文，可能带站位号/阶段号）
     * @return 命中结果；未命中返回 null
     */
    public static MatchResult matchField(List<CompiledMapping> candidates, String thirdFieldKey) {
        if (candidates == null || thirdFieldKey == null) {
            return null;
        }
        for (CompiledMapping c : candidates) {
            if (c.pattern == null) {
                if (thirdFieldKey.equals(c.mapping.getMatchPattern())) {
                    return new MatchResult(c.mapping, 0, null, null, null, null);
                }
                continue;
            }
            Matcher m = c.pattern.matcher(thirdFieldKey);
            if (m.matches()) {
                int stationNo = 0;
                Integer gunNo = null;
                Integer stage = null;
                Integer idx = null;
                String side = null;
                for (String name : c.placeholderNames) {
                    String v = m.group(name);
                    if (v == null) {
                        continue;
                    }
                    switch (name) {
                        case "side" -> side = v;
                        case "stage" -> stage = chineseStageCharToInt(v);
                        case "N" -> stationNo = Integer.parseInt(v);
                        case "gun" -> gunNo = Integer.parseInt(v);
                        case "idx" -> idx = Integer.parseInt(v);
                        default -> log.warn("[FieldMatching] 未知占位符: {}", name);
                    }
                }
                return new MatchResult(c.mapping, stationNo, gunNo, stage, idx, side);
            }
        }
        return null;
    }

    /**
     * 反查：根据 mapping + 维度参数，拼出第三方中文 fieldKey（用于报警侧查询 plc_data_latest.field_key）。
     */
    public static String renderPattern(FieldMappingEntity mapping,
                                       Integer stationNo,
                                       Integer gunNo,
                                       Integer stage,
                                       Integer idx,
                                       String side) {
        if (mapping == null || mapping.getMatchPattern() == null) {
            return null;
        }
        String out = mapping.getMatchPattern();
        if (stationNo != null) out = out.replace("{N}", String.valueOf(stationNo));
        if (gunNo != null)     out = out.replace("{gun}", String.valueOf(gunNo));
        if (stage != null)     out = out.replace("{stage}", intToStageChar(stage));
        if (idx != null)       out = out.replace("{idx}", String.valueOf(idx));
        if (side != null)      out = out.replace("{side}", side);
        return out;
    }

    /**
     * 根据 category_template 拼出 plc_data_latest.category_name。
     * @param categoryTemplate field_mapping.category_template：STATION / GUN_TEMP_CAT
     * @param stationNo 站位号（STATION 时必填）
     * @param gunCount 射枪数量（GUN_TEMP_CAT 时必填）
     */
    public static String buildCategoryName(String categoryTemplate, int stationNo, int gunCount) {
        if (categoryTemplate == null) {
            return "站台" + stationNo;
        }
        return switch (categoryTemplate) {
            case "GUN_TEMP_CAT" -> gunCount + "射枪温度";
            case "STATION" -> "站台" + stationNo;
            default -> "站台" + stationNo;
        };
    }

    /** 单个汉字数字/阿拉伯数字 → int（匹配阶段占位符用） */
    static int chineseStageCharToInt(String v) {
        if (v == null || v.isEmpty()) return 0;
        return switch (v) {
            case "一", "1" -> 1;
            case "二", "2" -> 2;
            case "三", "3" -> 3;
            case "四", "4" -> 4;
            case "五", "5" -> 5;
            default -> 0;
        };
    }

    /** int → 中文数字单字（renderPattern 里 {stage} 用） */
    static String intToStageChar(int stage) {
        return switch (stage) {
            case 1 -> "一";
            case 2 -> "二";
            case 3 -> "三";
            case 4 -> "四";
            case 5 -> "五";
            default -> String.valueOf(stage);
        };
    }

    /**
     * 根据匹配结果构建英文 field_key（internal_key + 维度后缀）。
     * 维度顺序：side → gun → stage → idx
     * 示例：MOLD_SET_TEMP_L_1, GUN_TEMP_2_4, INJECT_PRESS_R_3
     */
    public static String buildFieldKey(MatchResult match) {
        if (match == null || match.getMapping() == null) return null;
        String internalKey = match.getMapping().getInternalKey();
        if (internalKey == null) return null;
        List<String> suffixParts = new ArrayList<>();
        String side = match.getSide();
        if (side != null) {
            suffixParts.add("左".equals(side) ? "L" : "R");
        }
        Integer gunNo = match.getGunNo();
        if (gunNo != null) {
            suffixParts.add(String.valueOf(gunNo));
        }
        Integer stage = match.getStage();
        if (stage != null && stage > 0) {
            suffixParts.add(String.valueOf(stage));
        }
        Integer idx = match.getIdx();
        if (idx != null && idx > 0) {
            suffixParts.add(String.valueOf(idx));
        }
        if (suffixParts.isEmpty()) {
            return internalKey;
        }
        return internalKey + "_" + String.join("_", suffixParts);
    }

    /** 从维度上限配置中取必填维度，缺失或非法直接抛错（禁止在代码里写默认值） */
    public static int requireDimensionMax(Map<String, Integer> dimensionMax, String dimensionName) {
        if (dimensionMax == null) {
            throw new IllegalArgumentException("field_dimension_config 未加载，缺少维度: " + dimensionName);
        }
        Integer max = dimensionMax.get(dimensionName);
        if (max == null || max <= 0) {
            throw new IllegalArgumentException("field_dimension_config 缺少维度配置或配置非法: " + dimensionName);
        }
        return max;
    }

}
