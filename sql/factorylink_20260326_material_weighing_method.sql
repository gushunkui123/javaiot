-- 原料表新增称重方式字段
ALTER TABLE `biz_material`
    ADD COLUMN `weighing_method` tinyint NOT NULL DEFAULT 1 COMMENT '称重方式（1-手动 2-自动）' AFTER `material_name`;
