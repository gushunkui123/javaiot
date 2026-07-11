-- 为 shoot_station_schedule 表添加 mold_side 字段
-- 区分投产计划的模向：LEFT 左模 / RIGHT 右模，同一站位可同时有左右两个计划
ALTER TABLE shoot_station_schedule ADD COLUMN mold_side VARCHAR(10) DEFAULT NULL COMMENT '模向：LEFT 左模 / RIGHT 右模';