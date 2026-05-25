package com.agileboot.domain.factorylink.shootmachine.mapper;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ShootMoldRuleMapper extends BaseMapper<ShootMoldRuleEntity> {

    @Select(
            "SELECT r.id, r.mold_id AS moldId, r.field_code AS fieldCode, r.field_name AS fieldName, "
                    + "r.min_value AS `minValue`, r.max_value AS `maxValue`, r.enabled, "
                    + "r.created_at AS createdAt, r.updated_at AS updatedAt, r.deleted, "
                    + "m.mold_model AS moldModel, m.color AS color "
                    + "FROM shoot_mold_rule r "
                    + "INNER JOIN shoot_mold m ON r.mold_id = m.id AND m.deleted = 0 "
                    + "WHERE r.deleted = 0 AND r.mold_id = #{moldId} "
                    + "ORDER BY r.field_code ASC")
    List<ShootMoldRuleEntity> selectListByMoldIdWithMold(@Param("moldId") Long moldId);
}
