-- 字段维度上限配置表：把代码中写死的维度上限（idx=2 / stage=4）落到数据库，运行时读取。
-- 用法：改 max_value 即可改变展开数量，无需改代码。gun 维度来自排期/机台，不在此表。
CREATE TABLE IF NOT EXISTS `field_dimension_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dimension_name` varchar(32) NOT NULL COMMENT '维度名：idx / stage',
  `max_value` int NOT NULL COMMENT '该维度展开上限（含），如 idx=2 表示 {idx} 取 1~2',
  `description` varchar(128) NOT NULL DEFAULT '' COMMENT '说明',
  `enabled` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dimension` (`dimension_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='PLC字段维度上限配置表';

INSERT INTO `field_dimension_config` (`dimension_name`, `max_value`, `description`) VALUES
  ('idx', 2, '设定温度点数量（左/右模各 N 个，对应 {idx} 占位符）'),
  ('stage', 4, '射出阶段数量（对应 {stage} 占位符）')
ON DUPLICATE KEY UPDATE `max_value` = VALUES(`max_value`);
