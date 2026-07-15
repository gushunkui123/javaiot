-- 为 shoot_machine 表添加 station_count 字段
-- 新增机台时前端输入站位数量，后端自动生成对应站位记录
ALTER TABLE shoot_machine ADD COLUMN station_count INT NOT NULL DEFAULT 10 COMMENT '站位数量' AFTER remark;
