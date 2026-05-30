package com.agileboot.domain.factorylink.plc.util;

import cn.hutool.core.util.StrUtil;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** fieldKey 中文名及站位号解析工具 */
public final class PlcFieldKeyDisplayNames {

    private static final Map<String, String> FIELD_NAME_MAP = Map.ofEntries(
            Map.entry("dang_qian_jia_liu_time", "当前加硫时间"),
            Map.entry("she_ding_jia_liu_time", "设定加硫时间"),
            Map.entry("zuo_mo_she_ding", "左模料量设定"),
            Map.entry("you_mo_she_ding", "右模料量设定"),
            Map.entry("zuo_mo_shi_liang", "左模实时料量"),
            Map.entry("you_mo_shi_liang", "右模实时料量"),
            Map.entry("zuo_mo_time", "左模实时时间"),
            Map.entry("you_mo_time", "右模实时时间"),
            Map.entry("kai_mo", "开模"),
            Map.entry("zeng_ya", "增压")
    );

    private static final Pattern STATION_PATTERN = Pattern.compile("_(\\d+)$");

    private PlcFieldKeyDisplayNames() {}

    /**
     * 根据 fieldKey 解析中文名称
     * 例：dang_qian_jia_liu_time_1 -> 当前加硫时间
     */
    public static String resolve(String fieldKey) {
        if (StrUtil.isBlank(fieldKey)) {
            return "";
        }
        String baseKey = extractBaseKey(fieldKey);
        return FIELD_NAME_MAP.getOrDefault(baseKey, "");
    }

    /** 解析中文名；无映射时退回 fieldKey 本身。 */
    public static String resolveOrCode(String fieldKey) {
        if (StrUtil.isBlank(fieldKey)) {
            return "";
        }
        String name = resolve(fieldKey);
        return StrUtil.isNotBlank(name) ? name : fieldKey.trim();
    }

    /**
     * 解析站位号，解析失败返回 null
     * 例：dang_qian_jia_liu_time_1 -> 1
     */
    public static Integer parseStationNo(String fieldKey) {
        if (StrUtil.isBlank(fieldKey)) {
            return null;
        }
        Matcher matcher = STATION_PATTERN.matcher(fieldKey.trim().toLowerCase());
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }

    /** 从一批 fieldKey 中解析出去重、升序的站位号。 */
    public static Set<Integer> parseDistinctStationNos(Collection<String> fieldKeys) {
        if (fieldKeys == null || fieldKeys.isEmpty()) {
            return Set.of();
        }
        TreeSet<Integer> stationNos = new TreeSet<>();
        for (String fieldKey : fieldKeys) {
            Integer stationNo = parseStationNo(fieldKey);
            if (stationNo != null) {
                stationNos.add(stationNo);
            }
        }
        return stationNos;
    }

    /**
     * 提取基础字段名（去掉末尾的站位号）
     * 例：dang_qian_jia_liu_time_1 -> dang_qian_jia_liu_time
     */
    public static String extractBaseKey(String fieldKey) {
        if (StrUtil.isBlank(fieldKey)) {
            return "";
        }
        String trimmedKey = fieldKey.trim().toLowerCase();
        Matcher matcher = STATION_PATTERN.matcher(trimmedKey);
        return matcher.find() ? matcher.replaceAll("") : trimmedKey;
    }
}
