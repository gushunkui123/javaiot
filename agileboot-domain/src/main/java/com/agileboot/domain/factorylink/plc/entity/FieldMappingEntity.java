package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("field_mapping")
public class FieldMappingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("internal_key")
    private String internalKey;

    @TableField("match_pattern")
    private String matchPattern;

    @TableField("match_type")
    private String matchType = "EXACT";

    @TableField("dimensions")
    private String dimensions;

    @TableField("data_type")
    private String dataType = "NUMBER";

    @TableField("category_template")
    private String categoryTemplate = "STATION";

    /** 该字段最大阶段数（STAGE 维度字段填写，NULL=无阶段维度或取全局默认值） */
    @TableField("stage_count")
    private Integer stageCount;

    @TableField("enabled")
    private Boolean enabled;
}
