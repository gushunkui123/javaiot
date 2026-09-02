package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.FieldMappingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FieldMappingMapper extends BaseMapper<FieldMappingEntity> {

    @Select("SELECT * FROM field_mapping WHERE enabled = 1 ORDER BY id")
    List<FieldMappingEntity> listEnabled();

    @Select("SELECT * FROM field_mapping WHERE internal_key = #{internalKey} AND enabled = 1 LIMIT 1")
    FieldMappingEntity selectByInternalKey(@Param("internalKey") String internalKey);

    @Select({
        "<script>",
        "SELECT * FROM field_mapping WHERE enabled = 1 AND internal_key IN",
        "<foreach collection='internalKeys' item='k' open='(' separator=',' close=')'>#{k}</foreach>",
        "</script>"
    })
    List<FieldMappingEntity> selectByInternalKeys(@Param("internalKeys") Collection<String> internalKeys);
}
