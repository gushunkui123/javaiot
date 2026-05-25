package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * PLC阈值配置表。
 */
@Data
@TableName("plc_threshold_config")
public class PlcThresholdEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_name")
    private String deviceName;

    @TableField("kai_mo_threshold")
    private Integer kaiMoThreshold;

    @TableField("zeng_ya_threshold")
    private Integer zengYaThreshold;

    @TableField("zuo_mo_liang_threshold")
    private Integer zuoMoLiangThreshold;

    @TableField("zuo_mo_shi_liang_threshold")
    private Integer zuoMoShiLiangThreshold;

    @TableField("zuo_mo_time_threshold")
    private Integer zuoMoTimeThreshold;

    @TableField("you_mo_liang_threshold")
    private Integer youMoLiangThreshold;

    @TableField("you_mo_shi_liang_threshold")
    private Integer youMoShiLiangThreshold;

    @TableField("you_mo_time_threshold")
    private Integer youMoTimeThreshold;

    @TableField("she_ding_jia_liu_threshold")
    private Integer sheDingJiaLiuThreshold;

    @TableField("dang_qian_jia_liu_threshold")
    private Integer dangQianJiaLiuThreshold;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
