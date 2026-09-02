package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootBoardFieldEntity;
import java.util.List;
import java.util.Map;

/** 看板固定字段配置服务 */
public interface ShootBoardFieldConfigService {

    /**
     * 查询指定机器的看板固定字段，并合并 plc_data_latest 实时值与未处理报警状态。
     * 若该机器无专属配置，回退到通用模板（machine_id is null）。
     *
     * @param machineId 机器ID
     * @param stationNo 站台编号。传入时（看板按站台展示）所有固定字段严格按该站台取最新值
     *                  （category_name="站台"+stationNo），取不到即为空、绝不回退到全局值；
     *                  null 时（无站台机器的机器级展示）才取全局最新值
     */
    List<ShootBoardFieldEntity> listBoardFixedFields(Long machineId, Integer stationNo);

    /**
     * 批量查询指定机器多个站台的看板固定字段（一次调用返回所有站台，避免每站台一次 HTTP）。
     * 配置与未处理报警只需查询一次，PLC 实时值按 (field_key, category_name) 批量查询。
     *
     * @param machineId  机器ID
     * @param stationNos 站台编号列表（非空）。每个站台严格取该站台自己的最新值
     * @return stationNo -> 该站台固定字段列表
     */
    Map<Integer, List<ShootBoardFieldEntity>> listBoardFixedFieldsForStations(Long machineId, List<Integer> stationNos);
}
