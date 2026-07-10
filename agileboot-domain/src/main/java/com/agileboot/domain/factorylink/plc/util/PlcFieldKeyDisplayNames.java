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
            // 加硫
            Map.entry("dang_qian_jia_liu_time", "当前加硫时间"),
            Map.entry("she_ding_jia_liu_time", "设定加硫时间"),
            // 料量
            Map.entry("zuo_mo_she_ding", "左模料量设定"),
            Map.entry("you_mo_she_ding", "右模料量设定"),
            Map.entry("zuo_mo_shi_liang", "左模实时料量"),
            Map.entry("you_mo_shi_liang", "右模实时料量"),
            Map.entry("zuo_mo_time", "左模实射时间"),
            Map.entry("you_mo_time", "右模实射时间"),
            // 模具动作
            Map.entry("kai_mo", "开模"),
            Map.entry("zeng_ya", "增压"),
            // 射出速度
            Map.entry("zuo_mo_she_chu_su_du", "左模射出速度"),
            Map.entry("you_mo_she_chu_su_du", "右模射出速度"),
            // 模温
            Map.entry("nei_1_zuo_mo_wen_du", "内1左模温度"),
            Map.entry("nei_1_you_mo_wen_du", "内1右模温度"),
            Map.entry("nei_2_zuo_mo_wen_du", "内2左模温度"),
            Map.entry("nei_2_you_mo_wen_du", "内2右模温度"),
            // 射腔温度
            Map.entry("she_qiang_dang_qian_wen_du", "射枪当前温度"),
            // 射枪温度（8个测点）
            Map.entry("she_qiang_1_zuo_shang_wen_du", "射枪1左上当前温度"),
            Map.entry("she_qiang_1_zuo_xia_wen_du", "射枪1左下当前温度"),
            Map.entry("she_qiang_1_you_shang_wen_du", "射枪1右上当前温度"),
            Map.entry("she_qiang_1_you_xia_wen_du", "射枪1右下当前温度"),
            Map.entry("she_qiang_2_zuo_shang_wen_du", "射枪2左上当前温度"),
            Map.entry("she_qiang_2_zuo_xia_wen_du", "射枪2左下当前温度"),
            Map.entry("she_qiang_2_you_shang_wen_du", "射枪2右上当前温度"),
            Map.entry("she_qiang_2_you_xia_wen_du", "射枪2右下当前温度"),
            // 合模状态
            Map.entry("he_mo_zhuang_tai", "合模状态"),
            // 开模状态
            Map.entry("kai_mo_zhuang_tai", "开模状态"),
            // 站台温度（zt格式）
            Map.entry("r_tr1", "右模实时温度1"),
            Map.entry("r_tr2", "右模实时温度2"),
            Map.entry("l_tr1", "左模实时温度1"),
            Map.entry("l_tr2", "左模实时温度2"),
            Map.entry("r_ts1", "右模设定温度1"),
            Map.entry("r_ts2", "右模设定温度2"),
            Map.entry("l_ts1", "左模设定温度1"),
            Map.entry("l_ts2", "左模设定温度2"),
            // 站台开合模状态（zt格式）
            Map.entry("open", "开模状态"),
            Map.entry("close", "合模状态")
    );

    private static final Pattern STATION_PATTERN = Pattern.compile("_(\\d+)$");
    // 新格式：zt8_r_tr1，站位号在开头
    private static final Pattern ZT_STATION_PATTERN = Pattern.compile("^zt(\\d+)_");

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

    /**
     * 解析中文名，保留站位号后缀
     * 例：dang_qian_jia_liu_time_1 -> 当前加硫时间_1
     */
    public static String resolveWithStationNo(String fieldKey) {
        if (StrUtil.isBlank(fieldKey)) {
            return "";
        }
        String name = resolve(fieldKey);
        Integer stationNo = parseStationNo(fieldKey);
        if (StrUtil.isNotBlank(name) && stationNo != null) {
            return name + "_" + stationNo;
        }
        return StrUtil.isNotBlank(name) ? name : fieldKey.trim();
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
     * 例：zt8_r_tr1 -> 8（优先匹配新格式）
     * 例：dang_qian_jia_liu_time_1 -> 1
     */
    public static Integer parseStationNo(String fieldKey) {
        if (StrUtil.isBlank(fieldKey)) {
            return null;
        }
        String trimmedKey = fieldKey.trim().toLowerCase();
        // 优先匹配新格式 zt8_r_tr1
        Matcher ztMatcher = ZT_STATION_PATTERN.matcher(trimmedKey);
        if (ztMatcher.find()) {
            return Integer.valueOf(ztMatcher.group(1));
        }
        // 兜底匹配旧格式 xxx_1
        Matcher matcher = STATION_PATTERN.matcher(trimmedKey);
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
     * 提取基础字段名（去掉站位号部分）
     * 例：zt8_r_tr1 -> r_tr1（去掉zt8_前缀）
     * 例：dang_qian_jia_liu_time_1 -> dang_qian_jia_liu_time
     */
    public static String extractBaseKey(String fieldKey) {
        if (StrUtil.isBlank(fieldKey)) {
            return "";
        }
        String trimmedKey = fieldKey.trim().toLowerCase();
        // 优先处理新格式 zt8_r_tr1 -> r_tr1
        Matcher ztMatcher = ZT_STATION_PATTERN.matcher(trimmedKey);
        if (ztMatcher.find()) {
            return ztMatcher.replaceFirst("");
        }
        // 兜底处理旧格式 xxx_1 -> xxx
        Matcher matcher = STATION_PATTERN.matcher(trimmedKey);
        return matcher.find() ? matcher.replaceAll("") : trimmedKey;
    }
}
