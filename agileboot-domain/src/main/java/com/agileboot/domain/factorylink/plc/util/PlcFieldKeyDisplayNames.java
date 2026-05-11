package com.agileboot.domain.factorylink.plc.util;

import cn.hutool.core.util.StrUtil;
import java.util.List;
import java.util.Map;

/** fieldKey 中文名；前缀加拼接。 */
public final class PlcFieldKeyDisplayNames {

    private static final List<Map.Entry<String, String>> PREFIXES =
            List.of(
                    Map.entry("dang_qian_jia_liu_time_", "当前加硫时间"),
                    Map.entry("she_ding_jia_liu_time_", "设定加硫时间"),
                    Map.entry("zuo_mo_she_ding_", "左模料量设定"),
                    Map.entry("you_mo_she_ding_", "右模料量设定"),
                    Map.entry("zuo_mo_shi_liang_", "左模实时料量"),
                    Map.entry("you_mo_shi_liang_", "右模实时料量"),
                    Map.entry("zuo_mo_time_", "左模实时时间"),
                    Map.entry("you_mo_time_", "右模实时时间"),
                    Map.entry("kai_mo_", "开模"),
                    Map.entry("zeng_ya_", "增压"));

    private PlcFieldKeyDisplayNames() {}

    public static String resolve(String fieldKey) {
        if (StrUtil.isBlank(fieldKey)) {
            return "";
        }
        String key = fieldKey.trim().toLowerCase();
        for (Map.Entry<String, String> e : PREFIXES) {
            String p = e.getKey();
            if (key.startsWith(p)) {
                return e.getValue() + key.substring(p.length());
            }
        }
        return "";
    }
}
