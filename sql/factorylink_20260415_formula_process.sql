CREATE TABLE IF NOT EXISTS `biz_formula_process`
(
    `process_id`   bigint      NOT NULL AUTO_INCREMENT COMMENT '工艺ID',
    `process_code` varchar(50) NOT NULL COMMENT '工艺编号',
    `process_name` varchar(50) NOT NULL COMMENT '工艺名称',
    `creator_id`   bigint               DEFAULT NULL COMMENT '创建者ID',
    `create_time`  datetime             DEFAULT NULL COMMENT '创建时间',
    `updater_id`   bigint               DEFAULT NULL COMMENT '更新者ID',
    `update_time`  datetime             DEFAULT NULL COMMENT '更新时间',
    `deleted`      tinyint(1)  NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`process_id`),
    UNIQUE KEY `uk_process_code` (`process_code`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工艺信息表';

CREATE TABLE IF NOT EXISTS `biz_formula_process_step`
(
    `step_id`               bigint         NOT NULL AUTO_INCREMENT COMMENT '步骤ID',
    `process_id`            bigint         NOT NULL COMMENT '工艺ID',
    `sort_order`            int            NOT NULL DEFAULT 0 COMMENT '排序号',
    `step_no`               int            NOT NULL DEFAULT 0 COMMENT '步骤号',
    `action_id`             int            NOT NULL COMMENT '动作ID',
    `mixing_time`           decimal(18, 4) NOT NULL DEFAULT 0 COMMENT '混炼时间',
    `mixing_current`        decimal(18, 4) NOT NULL DEFAULT 0 COMMENT '混炼电流',
    `mixing_temp`           decimal(18, 4) NOT NULL DEFAULT 0 COMMENT '混炼温度',
    `rotate_speed`          decimal(18, 4) NOT NULL DEFAULT 0 COMMENT '转速',
    `pressure`              decimal(18, 4) NOT NULL DEFAULT 0 COMMENT '压力',
    `closing_condition_id`  int            NOT NULL DEFAULT 0 COMMENT '结束条件ID',
    `turning_times`         int            NOT NULL DEFAULT 0 COMMENT '翻转次数',
    `rising_time`           decimal(18, 4) NOT NULL DEFAULT 0 COMMENT '升降时间',
    `falling_time`          decimal(18, 4) NOT NULL DEFAULT 0 COMMENT '落料时间',
    PRIMARY KEY (`step_id`),
    KEY `idx_process_id` (`process_id`),
    KEY `idx_process_sort_order` (`process_id`, `sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工艺步骤表';

SET @process_id_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'biz_formula'
      AND COLUMN_NAME = 'process_id'
);
SET @process_id_column_sql := IF(
    @process_id_column_exists = 0,
    'ALTER TABLE `biz_formula` ADD COLUMN `process_id` bigint DEFAULT NULL COMMENT ''工艺ID'' AFTER `order_no`',
    'SELECT 1'
);
PREPARE stmt FROM @process_id_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @biz_formula_process_idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'biz_formula'
      AND INDEX_NAME = 'idx_process_id'
);
SET @biz_formula_process_idx_sql := IF(
    @biz_formula_process_idx_exists = 0,
    'ALTER TABLE `biz_formula` ADD INDEX `idx_process_id` (`process_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @biz_formula_process_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工艺管理', 1, 'FormulaProcess', 1, '/business/formula-process/index', 0, 'business:formula-process:list',
       '{"title":"工艺管理","icon":"ep:operation","showParent":true}', 1, '工艺管理菜单', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:formula-process:list');

SET @formula_process_menu_id := (
    SELECT `menu_id` FROM `sys_menu` WHERE `permission` = 'business:formula-process:list' LIMIT 1
);

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工艺新增', 0, ' ', @formula_process_menu_id, '', 1, 'business:formula-process:add',
       '{"title":"工艺新增"}', 1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @formula_process_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:formula-process:add');

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工艺修改', 0, ' ', @formula_process_menu_id, '', 1, 'business:formula-process:edit',
       '{"title":"工艺修改"}', 1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @formula_process_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:formula-process:edit');

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工艺删除', 0, ' ', @formula_process_menu_id, '', 1, 'business:formula-process:remove',
       '{"title":"工艺删除"}', 1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @formula_process_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:formula-process:remove');
