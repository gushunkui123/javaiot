-- 原料表新增称重方式字段
ALTER TABLE `biz_material`
    ADD COLUMN `weighing_method` tinyint NOT NULL DEFAULT 0 COMMENT '称重方式（0-自动 1-手动）' AFTER `material_name`;
