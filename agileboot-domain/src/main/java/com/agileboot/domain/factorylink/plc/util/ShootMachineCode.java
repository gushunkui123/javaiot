package com.agileboot.domain.factorylink.plc.util;

/**
 * 射出机机台名工具类。
 * dataCode / configCode 映射已迁移到数据库表 plc_machine_data_config，
 * 运行时由 PlcDataSyncService 加载，本类仅保留机台名相关的判断工具。
 */
public final class ShootMachineCode {

    private ShootMachineCode() {
    }

    /** 判断机台名是否为5号机（形如"射出机5号机"/"射出机五号机"，避免"15号机"误命中） */
    public static boolean isShootFive(String machineName) {
        return machineName != null
                && (machineName.matches(".*[^\\d]5号机$") || machineName.matches(".*五号机$"));
    }

    /** 判断机台名是否为9号机 */
    public static boolean isShootNine(String machineName) {
        return machineName != null
                && (machineName.matches(".*[^\\d]9号机$") || machineName.matches(".*九号机$"));
    }
}
