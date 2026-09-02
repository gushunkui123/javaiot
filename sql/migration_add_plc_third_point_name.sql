-- 给 plc_data_latest 增加「第三方接口原始点位名称」字段，
-- 用于追溯每条最新数据来自第三方接口的哪个点位（displayName / remark），便于排查映射与展示。
ALTER TABLE plc_data_latest
  ADD COLUMN third_point_name VARCHAR(200) DEFAULT NULL COMMENT '第三方接口返回的原始点位名称(displayName/remark)' AFTER category_name;
