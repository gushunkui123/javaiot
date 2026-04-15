package com.factorylink.common.config;

/**
 * 设备配置提供者接口（由 domain 模块实现，从数据库读取设备配置）
 * <p>
 * 替代 {@link MachineProperties}，使 infrastructure 模块可通过此接口获取设备配置，
 * 避免 infrastructure 直接依赖 domain 产生循环依赖。
 */
public interface MachineConfigProvider {

    /**
     * 获取主磅配置，未找到或已禁用返回 null
     */
    ScaleMachineConfig getMainScale();

    /**
     * 获取微量配置，未找到或已禁用返回 null
     */
    ScaleMachineConfig getMicroScale();

    /**
     * 根据设备编码判断设备是否启用
     */
    boolean isDeviceEnabled(String machineCode);

    /**
     * 根据设备编码判断设备是否在线
     */
    boolean isDeviceOnline(String machineCode);

}
