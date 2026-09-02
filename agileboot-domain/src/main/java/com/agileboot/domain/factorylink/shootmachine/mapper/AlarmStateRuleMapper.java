package com.agileboot.domain.factorylink.shootmachine.mapper;

import com.agileboot.domain.factorylink.shootmachine.entity.AlarmStateRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AlarmStateRuleMapper extends BaseMapper<AlarmStateRuleEntity> {

    @Select("SELECT * FROM alarm_state_rule WHERE enabled = 1 ORDER BY priority, id")
    List<AlarmStateRuleEntity> listEnabled();

    @Select("SELECT * FROM alarm_state_rule WHERE rule_code = #{ruleCode} LIMIT 1")
    AlarmStateRuleEntity selectByRuleCode(@Param("ruleCode") String ruleCode);
}
