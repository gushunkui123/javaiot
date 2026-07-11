-- 1. 删除旧联合唯一索引 uk_device_field_category
ALTER TABLE plc_data_latest
DROP INDEX uk_device_field_category,
-- 2. 新增业务点位编码字段
ADD COLUMN data_code VARCHAR(100) NULL COMMENT '点位编码 dataCode，作为 upsert 唯一标识',
-- 3. 创建设备+业务点位编码新唯一索引
ADD UNIQUE KEY uk_device_data_code (device_name, data_code);