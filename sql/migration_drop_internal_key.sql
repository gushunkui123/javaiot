-- 删除 shoot_mold_rule.internal_key 列
-- field_code 已完全替代 internal_key（两者值一致，field_code 是规范化后的 internal_key）
-- 代码层面 ShootMoldRuleEntity 已删除 internalKey 字段，renderMappings 用 rule.getFieldCode() 查 field_mapping

-- 1. 先删除旧唯一键（包含 internal_key，不删无法 DROP COLUMN）
ALTER TABLE shoot_mold_rule DROP INDEX uk_mold_field;

-- 2. 删除 internal_key 列
ALTER TABLE shoot_mold_rule DROP COLUMN internal_key;

-- 3. 用 field_code 重建唯一键
ALTER TABLE shoot_mold_rule ADD UNIQUE KEY uk_mold_field (mold_id, field_code, dimension_type, stage);
