package com.agileboot.domain.factorylink.plc.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 射出机 dataCode 与机台归属映射。
 * 业务规则：dataCode 本身即决定所属机台（第三方 areaName 仅作一致性校验，不再用于反查机台）。
 * 每个 dataCode 需配套 MQTT 配置编码 configCode（调用外部 PLC 接口必传），configCode 留空待补充。
 * - 9号机：kkb756、7JTAVe
 * - 5号机：2WSK6z、QLjnd7、Tq5xxP
 */
public enum ShootMachineCode {

    /** 射出机九号机 */
    MACHINE_9("射出机九号机",
            entry("kkb756", "3DVDjR"),
            entry("7JTAVe", "kwhu2E")),
    /** 射出机五号机 */
    MACHINE_5("射出机五号机",
            entry("2WSK6z", "CdyBuv"),
            entry("QLjnd7", "TLVh1z"),
            entry("Tq5xxP", "TLVh1z"));

    /** 单条需同步的数据：dataCode + 对应 MQTT 配置编码 configCode */
    public static final class DataCodeEntry {
        private final String dataCode;
        private final String configCode;

        public DataCodeEntry(String dataCode, String configCode) {
            this.dataCode = dataCode;
            this.configCode = configCode;
        }

        public String getDataCode() {
            return dataCode;
        }

        public String getConfigCode() {
            return configCode;
        }
    }

    /** 便捷构造 DataCodeEntry */
    private static DataCodeEntry entry(String dataCode, String configCode) {
        return new DataCodeEntry(dataCode, configCode);
    }

    /** 机台名称（与 shoot_machine.machine_name 一致，用于查库获取 machineId） */
    private final String machineName;

    /** 该机台下需要同步的 (dataCode, configCode) 列表 */
    private final List<DataCodeEntry> entries;

    ShootMachineCode(String machineName, DataCodeEntry... entries) {
        this.machineName = machineName;
        this.entries = List.of(entries);
    }

    public String getMachineName() {
        return machineName;
    }

    /** 该机台下全部 (dataCode, configCode) 条目 */
    public List<DataCodeEntry> getEntries() {
        return entries;
    }

    /** 兼容旧调用：仅返回 dataCode 列表 */
    public List<String> getDataCodes() {
        return entries.stream().map(DataCodeEntry::getDataCode).collect(Collectors.toList());
    }

    /** 判断是否为5号机（机台名形如"射出机5号机"/"射出机五号机"，避免"15号机"误命中） */
    public boolean isShootFive() {
        return machineName.matches(".*[^\\d]5号机$") || machineName.matches(".*五号机$");
    }

    /** 判断是否为9号机 */
    public boolean isShootNine() {
        return machineName.matches(".*[^\\d]9号机$") || machineName.matches(".*九号机$");
    }

    /** 根据 dataCode 反查所属机台（未匹配返回 null） */
    public static ShootMachineCode of(String dataCode) {
        if (dataCode == null) {
            return null;
        }
        for (ShootMachineCode machine : values()) {
            if (machine.getDataCodes().contains(dataCode)) {
                return machine;
            }
        }
        return null;
    }

    /** 根据 dataCode 获取对应的 configCode（未匹配返回 null） */
    public static String configCodeOf(String dataCode) {
        if (dataCode == null) {
            return null;
        }
        for (ShootMachineCode machine : values()) {
            for (DataCodeEntry e : machine.entries) {
                if (e.dataCode.equals(dataCode)) {
                    return e.configCode;
                }
            }
        }
        return null;
    }

    /** 全部需要同步的 dataCode（扁平化） */
    public static List<String> allDataCodes() {
        return Arrays.stream(values())
                .flatMap(m -> m.getDataCodes().stream())
                .collect(Collectors.toList());
    }

    /** 全部需要同步的 (dataCode, configCode) 条目（扁平化） */
    public static List<DataCodeEntry> allEntries() {
        return Arrays.stream(values())
                .flatMap(m -> m.entries.stream())
                .collect(Collectors.toList());
    }
}
