package com.agileboot.domain.factorylink.plc.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 射出机 dataCode 与机台归属映射。
 * 业务规则：dataCode 本身即决定所属机台（第三方 areaName 仅作一致性校验，不再用于反查机台）。
 * - 9号机：kkb756、7JTAVe
 * - 5号机：2WSK6z、QLjnd7、Tq5xxP
 */
public enum ShootMachineCode {

    /** 射出机九号机 */
    MACHINE_9("射出机九号机", "kkb756", "7JTAVe"),
    /** 射出机五号机 */
    MACHINE_5("射出机五号机", "2WSK6z", "QLjnd7", "Tq5xxP");

    /** 机台名称（与 shoot_machine.machine_name 一致，用于查库获取 machineId） */
    private final String machineName;

    /** 该机台下需要同步的 dataCode 列表 */
    private final List<String> dataCodes;

    ShootMachineCode(String machineName, String... dataCodes) {
        this.machineName = machineName;
        this.dataCodes = List.of(dataCodes);
    }

    public String getMachineName() {
        return machineName;
    }

    public List<String> getDataCodes() {
        return dataCodes;
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
            if (machine.dataCodes.contains(dataCode)) {
                return machine;
            }
        }
        return null;
    }

    /** 全部需要同步的 dataCode（扁平化） */
    public static List<String> allDataCodes() {
        return Arrays.stream(values())
                .flatMap(m -> m.dataCodes.stream())
                .collect(Collectors.toList());
    }
}
