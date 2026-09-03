-- 为 shoot_machine 表添加 sort 排序字段
-- 机台列表按 sort 升序排列（数值越小越靠前）
ALTER TABLE shoot_machine ADD COLUMN sort INT NOT NULL DEFAULT 0 COMMENT '排序（升序，越小越靠前）' AFTER station_count;
