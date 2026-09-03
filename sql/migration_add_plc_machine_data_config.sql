-- 机台 dataCode/configCode 映射配置表：把 ShootMachineCode 枚举里的 (dataCode, configCode) 落到数据库，运行时读取。
-- 用法：新增机台/换 dataCode/改 configCode 只需插行/改行 + 重启后端，无需改代码。
CREATE TABLE IF NOT EXISTS `plc_machine_data_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `machine_name` varchar(64) NOT NULL COMMENT '机台名称，与 shoot_machine.machine_name 一致',
  `data_code` varchar(64) NOT NULL COMMENT '第三方 dataCode',
  `config_code` varchar(64) DEFAULT NULL COMMENT 'MQTT 配置编码 configCode（调用外部PLC接口必传，可空）',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '启用开关：1启用 0停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_machine_data` (`machine_name`, `data_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='射出机机台 dataCode/configCode 配置表';

-- 种子数据：对齐原 ShootMachineCode 枚举
INSERT INTO `plc_machine_data_config` (`machine_name`, `data_code`, `config_code`, `remark`) VALUES
  ('射出机九号机', 'kkb756', '3DVDjR', '9号机'),
  ('射出机九号机', '7JTAVe', 'kwhu2E', '9号机'),
  ('射出机五号机', '2WSK6z', 'CdyBuv', '5号机'),
  ('射出机五号机', 'QLjnd7', 'TLVh1z', '5号机'),
  ('射出机五号机', 'Tq5xxP', 'TLVh1z', '5号机')
ON DUPLICATE KEY UPDATE `config_code` = VALUES(`config_code`), `remark` = VALUES(`remark`);
