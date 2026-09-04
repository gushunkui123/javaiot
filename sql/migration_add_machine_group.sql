-- 机台分组：shoot_machine 增加分组编码列，并新增分组表（车间1/车间2）
ALTER TABLE shoot_machine ADD COLUMN machine_group varchar(64) DEFAULT NULL COMMENT '机台分组编码' AFTER remark;

CREATE TABLE `machine_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_code` varchar(64) NOT NULL COMMENT '分组编码（shoot_machine.machine_group 存此值）',
  `group_name` varchar(128) NOT NULL COMMENT '分组显示名（如 车间1）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（升序，越小越靠前）',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='射出机分组';

INSERT INTO `machine_group` (`group_code`, `group_name`, `sort`, `enabled`, `deleted`) VALUES
('workshop_1', '车间1', 1, 1, 0),
('workshop_2', '车间2', 2, 1, 0);
