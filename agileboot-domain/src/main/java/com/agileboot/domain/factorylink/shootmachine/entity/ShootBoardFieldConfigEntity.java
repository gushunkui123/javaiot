package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 看板固定展示字段配置。
 * 每个机器可配置一组固定要展示的工艺参数（如左模1设定温度、右模第一阶段射出压力等），
 * 看板顶部固定区块按此配置渲染，与站位的动态网格解耦。
 */
@Data
@TableName("shoot_board_field_config")
public class ShootBoardFieldConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联 shoot_machine.id，空表示通用模板 */
    @TableField("machine_id")
    private Long machineId;

    /** 对应的英文 field_key，如 MOLD_SET_TEMP_L_1 */
    @TableField("field_key")
    private String fieldKey;

    /** 看板显示名，如"左模1设定温度" */
    @TableField("display_name")
    private String displayName;

    /** 分组名，如"设定温度"/"射出速度"/"射出压力"/"加硫时间" */
    @TableField("group_name")
    private String groupName;

    /** 展示顺序 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否启用 */
    @TableField("is_enabled")
    private Boolean enabled;

    /** 抽象内部键（不含 side/idx/stage 后缀），如 MOLD_SET_TEMP、GUN_TEMP、SET_CURE_TIME */
    @TableField("internal_key")
    private String internalKey;

    /** 分类模板：STATION=按站台维度（category_name=站台N），GLOBAL=机器级全局 */
    @TableField("category_template")
    private String categoryTemplate;
}
