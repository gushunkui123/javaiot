-- 为 shoot_mold 表添加 mold_side 字段
-- 区分模具左模/右模，跟随模具主数据存储，阈值配置页面按此字段过滤展示
ALTER TABLE shoot_mold ADD COLUMN mold_side VARCHAR(20) NOT NULL DEFAULT 'LEFT' COMMENT '模具模向：LEFT 左模 / RIGHT 右模';
