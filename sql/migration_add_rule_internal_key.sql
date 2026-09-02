-- 模具阈值规则改为「抽象内部键 + 维度」存储，与设备 L/R/枪号解耦。
-- 比较报警时由排期（mold_side / gun_no / station_no）实例化具体 PLC field_key。

-- 0. 清空旧规则（不兼容旧数据：旧 field_code 为具体 key，新结构为抽象 internalKey）
TRUNCATE TABLE shoot_mold_rule;

-- 1. 增加抽象字段
ALTER TABLE shoot_mold_rule
  ADD COLUMN internal_key VARCHAR(64) DEFAULT NULL COMMENT '抽象内部键，如 MOLD_SET_TEMP / GUN_TEMP / INJECT_SPEED，与设备维度解耦' AFTER field_name,
  ADD COLUMN dimension_type VARCHAR(20) DEFAULT NULL COMMENT '维度类型：GLOBAL 整体单行 / STAGE 按阶段' AFTER internal_key,
  ADD COLUMN stage INT DEFAULT NULL COMMENT '阶段号（STAGE 维度时有效，1~4；GLOBAL 为 NULL）' AFTER dimension_type;

-- 2. 唯一约束升级：原 (mold_id, field_code) 要求每个具体 key 唯一；
--    现在 field_code 存的是 internalKey（如 GUN_TEMP），同模具下按「阶段」会有多行，故需把维度纳入唯一键。
--    旧数据 internal_key 为 NULL，MySQL 唯一约束中 NULL 互不相同，不会与新约束冲突。
ALTER TABLE shoot_mold_rule DROP INDEX uk_mold_field;
ALTER TABLE shoot_mold_rule
  ADD UNIQUE KEY uk_mold_field (mold_id, internal_key, dimension_type, stage);
