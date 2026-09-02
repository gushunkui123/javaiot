package com.agileboot.domain.factorylink.shootmachine.mapper;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootBoardFieldConfigEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 看板固定展示字段配置 Mapper */
@Mapper
public interface ShootBoardFieldConfigMapper extends BaseMapper<ShootBoardFieldConfigEntity> {

    /**
     * 查询指定机器启用的看板字段配置，按 sort_order 升序。
     * 优先返回 machineId 专属配置；若 machineId 专属为空，则返回通用模板（machine_id is null）。
     */
    @Select(
            "<script>"
                    + "SELECT id, machine_id AS machineId, field_key AS fieldKey, display_name AS displayName, "
                    + "group_name AS groupName, sort_order AS sortOrder, is_enabled AS enabled "
                    + "FROM shoot_board_field_config "
                    + "WHERE is_enabled = 1 AND (machine_id = #{machineId} "
                    + "<if test='useTemplate'> OR machine_id IS NULL</if>) "
                    + "ORDER BY sort_order ASC"
                    + "</script>")
    List<ShootBoardFieldConfigEntity> selectEnabledByMachineId(@Param("machineId") Long machineId,
                                                               @Param("useTemplate") boolean useTemplate);
}
