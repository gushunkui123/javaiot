-- 设备在线状态字段
ALTER TABLE `biz_machine`
    ADD COLUMN `online_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '在线状态（0-离线 1-在线）' AFTER `remark`,
    ADD COLUMN `last_check_time` datetime DEFAULT NULL COMMENT '最后检测时间' AFTER `online_status`;
