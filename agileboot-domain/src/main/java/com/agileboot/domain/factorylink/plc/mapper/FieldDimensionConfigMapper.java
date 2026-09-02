package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.FieldDimensionConfigEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FieldDimensionConfigMapper extends BaseMapper<FieldDimensionConfigEntity> {

    @Select("SELECT * FROM field_dimension_config WHERE enabled = 1")
    List<FieldDimensionConfigEntity> listEnabled();
}
