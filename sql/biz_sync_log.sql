-- 设备下发同步日志表
CREATE TABLE IF NOT EXISTS `biz_sync_log` (
    `log_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `device_type`  VARCHAR(20)  NOT NULL COMMENT '设备类型: MAIN_SCALE / MICRO_SCALE',
    `operation`    VARCHAR(30)  NOT NULL COMMENT '操作: ADD_FORMULA / UPDATE_WORK_ORDER 等',
    `target_id`    BIGINT       NULL     COMMENT '业务数据ID',
    `request_body` TEXT         NULL     COMMENT '发送的JSON请求体',
    `error_msg`    VARCHAR(500) NULL     COMMENT '错误信息',
    `retry_count`  INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0失败 1成功',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_operation` (`operation`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备下发同步日志表';
