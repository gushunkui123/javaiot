-- per-field stage_count: 将 stage 阶段数从全局配置改为每个字段独立配置。
-- 用途：INJECT_SPEED 需要 5 阶段，GUN_TEMP/INJECT_PRESS 只需 4 阶段。

-- 1. field_mapping 加 stage_count 列
ALTER TABLE field_mapping
  ADD COLUMN stage_count INT DEFAULT NULL COMMENT '该字段最大阶段数（STAGE 维度字段填写，NULL=无阶段维度）' AFTER category_template;

-- 2. 为现有 STAGE 字段设置 stage_count
--    dimensions 包含 'stage' 的字段才需要填值
UPDATE field_mapping SET stage_count = 4 WHERE internal_key IN ('GUN_TEMP', 'INJECT_PRESS');
UPDATE field_mapping SET stage_count = 5 WHERE internal_key = 'INJECT_SPEED';

-- 3. field_dimension_config 删除 stage 行（已被 per-field stage_count 替代）
DELETE FROM field_dimension_config WHERE dimension_name = 'stage';
