package com.agileboot.domain.factorylink.plc.util;

import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    // ====== fieldCode（展示文字）→ PLC字段中文名模式映射 ======
    // 左右字段：前缀 "左模" / "右模"，根据 moldSide 动态替换
    // 注意：PLC实际field_key格式为 {side}{number}设定温度（数字在前），如"左模1设定温度"
    private static final Map<String, List<String>> LEFT_RIGHT_FIELDS = Map.of(
            "设定温度", List.of("左模1设定温度", "左模2设定温度"),
            "射出压力", List.of("左模第一阶段 射出压力", "左模第二阶段 射出压力", "左模第三阶段 射出压力", "左模第四阶段 射出压力", "左模第五阶段 射出压力"),
            "第一阶段 射出速度", List.of("左模第一阶段 射出速度"),
            "第二阶段 射出速度", List.of("左模第二阶段 射出速度"),
            "第三阶段 射出速度", List.of("左模第三阶段 射出速度"),
            "第四阶段 射出速度", List.of("左模第四阶段 射出速度")
    );

    // 右模对应的字段名（用于 RIGHT 方向）
    // PLC实际field_key格式：{side}{number}设定温度（数字在前），如"右模1设定温度"
    private static final Map<String, List<String>> RIGHT_FIELDS = Map.of(
            "设定温度", List.of("右模1设定温度", "右模2设定温度"),
            "射出压力", List.of("右模第一阶段 射出压力", "右模第二阶段 射出压力", "右模第三阶段 射出压力", "右模第四阶段 射出压力", "右模第五阶段 射出压力"),
            "第一阶段 射出速度", List.of("右模第一阶段 射出速度"),
            "第二阶段 射出速度", List.of("右模第二阶段 射出速度"),
            "第三阶段 射出速度", List.of("右模第三阶段 射出速度"),
            "第四阶段 射出速度", List.of("右模第四阶段 射出速度")
    );

    // 全局字段（不区分左右）
    private static final Map<String, List<String>> GLOBAL_FIELDS = Map.of(
            "设定加硫时间", List.of("设定加硫时间"),
            "射枪温度", List.of(
                    "射枪1左上当前温度", "射枪1左下当前温度",
                    "射枪1右上当前温度", "射枪1右下当前温度",
                    "射枪2左上当前温度", "射枪2左下当前温度",
                    "射枪2右上当前温度", "射枪2右下当前温度"
            )
    );

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

    /**
     * 根据 fieldCode（展示文字）和 moldSide 返回需要比对的 PLC 字段中文名列表。
     * moldSide 为 "LEFT" 或 "RIGHT"；全局字段忽略 moldSide。
     *
     * 示例：
     *   resolvePlcFieldKeys("设定温度", "LEFT")  → ["左模1设定温度", "左模2设定温度"]
     *   resolvePlcFieldKeys("射出压力", "RIGHT") → ["右模第一阶段射出压力", ..., "右模第五阶段射出压力"]
     *   resolvePlcFieldKeys("设定加硫时间", "LEFT") → ["设定加硫时间"]
     */
    public static List<String> resolvePlcFieldKeys(String fieldCode, String moldSide) {
        if (StrUtil.isBlank(fieldCode)) {
            return List.of();
        }

        // 1. 全局字段优先
        List<String> globalFields = GLOBAL_FIELDS.get(fieldCode);
        if (globalFields != null) {
            return globalFields;
        }

        // 2. 左右字段根据 moldSide 选择
        boolean isRight = "RIGHT".equalsIgnoreCase(moldSide);
        Map<String, List<String>> source = isRight ? RIGHT_FIELDS : LEFT_RIGHT_FIELDS;
        List<String> fields = source.get(fieldCode);
        if (fields != null) {
            return fields;
        }

        // 3. 未匹配到映射，返回空列表（报警检测时跳过该规则）
        return List.of();
    }

    /**
     * 根据 fieldCode、gunCount、gunNo 返回射枪温度阶段字段列表
     * 用于新格式 PLC 数据：category_name 为 "4枪温度" 或 "2枪温度"，field_key 为 "射枪温度X第Y阶段"
     *
     * @param fieldCode 规则字段编码（如 "射枪温度"）
     * @param gunCount  该机器的射枪数量（4 或 2）
     * @param gunNo     生产计划选择的枪号（1-4 或 1-2），为 null 时返回所有枪号的字段
     * @return 需要比对的 PLC 字段名列表
     */
    public static List<String> resolvePlcFieldKeysForGunTemperature(String fieldCode, int gunCount, Integer gunNo) {
        String normalized = fieldCode == null ? "" : fieldCode.replaceAll("\\s+", "");
        if (!"射枪温度".equals(normalized) || gunCount < 1) {
            return List.of();
        }

        List<String> fieldKeys = new ArrayList<>();
        int startGun = (gunNo != null && gunNo >= 1 && gunNo <= gunCount) ? gunNo : 1;
        int endGun = (gunNo != null && gunNo >= 1 && gunNo <= gunCount) ? gunNo : gunCount;

        // 生成字段名：射枪温度{gunNo}{中文数字}阶段，匹配 PLC 实际 field_key
        for (int g = startGun; g <= endGun; g++) {
            for (int stage = 1; stage <= 4; stage++) {
                fieldKeys.add("射枪温度" + g + STAGE_CHINESE[stage - 1] + "阶段");
            }
        }
        return fieldKeys;
    }

    private static final String[] STAGE_CHINESE = {"一", "二", "三", "四"};

    /**
     * 判断 fieldCode 是否为按阶段的射枪温度规则，如 "第一阶段 射枪温度"、"第四阶段射枪温度"
     * 忽略空格差异，支持 "第一阶段 射枪温度"、"第一阶段  射枪温度"、"第一阶段射枪温度" 等格式
     * @return 阶段号（1-4），非射枪温度阶段规则返回 null
     */
    public static Integer parseGunTemperatureStage(String fieldCode) {
        if (StrUtil.isBlank(fieldCode)) {
            return null;
        }
        String normalized = fieldCode.replaceAll("\\s+", "");
        for (int i = 0; i < STAGE_CHINESE.length; i++) {
            if (normalized.equals("第" + STAGE_CHINESE[i] + "阶段射枪温度")) {
                return i + 1;
            }
        }
        return null;
    }

    /**
     * 根据阶段号返回该阶段的射枪温度字段列表
     * 例如 stage=1, gunCount=2, gunNo=null → ["射枪温度1第一阶段", "射枪温度2第一阶段"]
     */
    public static List<String> resolvePlcFieldKeysForGunTemperatureByStage(int stage, int gunCount, Integer gunNo) {
        List<String> fieldKeys = new ArrayList<>();
        int startGun = (gunNo != null && gunNo >= 1 && gunNo <= gunCount) ? gunNo : 1;
        int endGun = (gunNo != null && gunNo >= 1 && gunNo <= gunCount) ? gunNo : gunCount;

        String stageName = "第" + STAGE_CHINESE[stage - 1] + "阶段";
        for (int g = startGun; g <= endGun; g++) {
            fieldKeys.add("射枪温度" + g + stageName);
        }
        return fieldKeys;
    }

    /**
     * 根据射枪数量返回对应的 category_name
     * @param gunCount 射枪数量（4 或 2）
     * @return "4射枪温度" 或 "2射枪温度"
     */
    public static String resolveGunTemperatureCategoryName(int gunCount) {
        return gunCount + "射枪温度";
    }
}
