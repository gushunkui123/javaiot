-- 为 shoot_mold_rule 表添加 station_no 字段
-- station_no = 0 表示全局规则（如射枪温度），不绑定具体站位
ALTER TABLE shoot_mold_rule ADD COLUMN station_no INT DEFAULT NULL COMMENT '站位编号，0表示全局规则' AFTER enabled;