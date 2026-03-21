package com.agileboot.domain.business.machine.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 设备下发同步日志表
 */
@Getter
@Setter
@TableName("biz_sync_log")
@Schema(name = "BizSyncLogEntity", description = "设备下发同步日志表")
public class BizSyncLogEntity extends Model<BizSyncLogEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID")
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    @Schema(description = "设备类型: MAIN_SCALE / MICRO_SCALE")
    @TableField("device_type")
    private String deviceType;

    @Schema(description = "操作类型: ADD_FORMULA / UPDATE_FORMULA / DELETE_FORMULA 等")
    @TableField("operation")
    private String operation;

    @Schema(description = "业务数据ID")
    @TableField("target_id")
    private Long targetId;

    @Schema(description = "发送的JSON请求体")
    @TableField("request_body")
    private String requestBody;

    @Schema(description = "错误信息")
    @TableField("error_msg")
    private String errorMsg;

    @Schema(description = "已重试次数")
    @TableField("retry_count")
    private Integer retryCount;

    @Schema(description = "状态: 0失败 1成功")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @Override
    public Serializable pkVal() {
        return this.logId;
    }
}
