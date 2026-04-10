-- 设备所属产线字段
ALTER TABLE `biz_machine`
    ADD COLUMN `production_line` varchar(50) DEFAULT NULL COMMENT '所属产线' AFTER `device_type`;
