CREATE TABLE IF NOT EXISTS `biz_formula`
(
    `formula_id`   bigint       NOT NULL AUTO_INCREMENT COMMENT '配方ID',
    `formula_code` varchar(50)  NOT NULL COMMENT '配方编号',
    `formula_name` varchar(50)  NOT NULL COMMENT '配方名称',
    `creator_id`   bigint                DEFAULT NULL COMMENT '创建者ID',
    `create_time`  datetime              DEFAULT NULL COMMENT '创建时间',
    `updater_id`   bigint                DEFAULT NULL COMMENT '更新者ID',
    `update_time`  datetime              DEFAULT NULL COMMENT '更新时间',
    `deleted`      tinyint(1)   NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`formula_id`),
    UNIQUE KEY `uk_formula_code` (`formula_code`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='配方信息表';

CREATE TABLE IF NOT EXISTS `biz_formula_item`
(
    `item_id`         bigint         NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `formula_id`      bigint         NOT NULL COMMENT '配方ID',
    `material_id`     bigint         NOT NULL COMMENT '原料ID',
    `material_weight` decimal(18, 4) NOT NULL COMMENT '原料重量(kg)',
    `step_no`         int                     DEFAULT NULL COMMENT '下料段序（主料使用，微量可空）',
    `sort_order`      int            NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (`item_id`),
    KEY `idx_formula_id` (`formula_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='配方明细表';

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '配方管理', 1, 'Formula', 1, '/business/formula/index', 0, 'business:formula:list',
       '{"title":"配方管理","icon":"ep:document","showParent":true}', 1, '配方管理菜单', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:formula:list');

SET @formula_menu_id := (SELECT `menu_id` FROM `sys_menu` WHERE `permission` = 'business:formula:list' LIMIT 1);

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '配方新增', 0, ' ', @formula_menu_id, '', 1, 'business:formula:add', '{"title":"配方新增"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @formula_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:formula:add');

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '配方修改', 0, ' ', @formula_menu_id, '', 1, 'business:formula:edit', '{"title":"配方修改"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @formula_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:formula:edit');

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '配方删除', 0, ' ', @formula_menu_id, '', 1, 'business:formula:remove', '{"title":"配方删除"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @formula_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:formula:remove');
