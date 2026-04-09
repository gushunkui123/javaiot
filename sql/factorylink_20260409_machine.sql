-- 设备信息表
CREATE TABLE IF NOT EXISTS `biz_machine`
(
    `machine_id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `machine_code`     varchar(50)  NOT NULL COMMENT '设备编码（唯一标识，如 MAIN_SCALE）',
    `machine_name`     varchar(100) NOT NULL COMMENT '设备名称',
    `device_type`      varchar(30)  NOT NULL COMMENT '设备类型: MAIN_SCALE / MICRO_SCALE / LABELING_MACHINE',
    `enabled`          tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（0-禁用 1-启用）',
    `ip`               varchar(50)           DEFAULT NULL COMMENT 'IP地址',
    `port`             int                   DEFAULT NULL COMMENT '端口号（贴标机使用）',
    `api_url`          varchar(255)          DEFAULT NULL COMMENT '查询接口地址（磅秤使用）',
    `api_write_url`    varchar(255)          DEFAULT NULL COMMENT '写入接口地址（磅秤使用）',
    `plant`            varchar(50)           DEFAULT NULL COMMENT '工厂编号（磅秤使用）',
    `scale_machine_id` int                   DEFAULT NULL COMMENT '磅秤系统设备编号（磅秤使用）',
    `remark`           varchar(500)          DEFAULT NULL COMMENT '备注',
    `creator_id`       bigint                DEFAULT NULL COMMENT '创建者ID',
    `create_time`      datetime              DEFAULT NULL COMMENT '创建时间',
    `updater_id`       bigint                DEFAULT NULL COMMENT '更新者ID',
    `update_time`      datetime              DEFAULT NULL COMMENT '更新时间',
    `deleted`          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`machine_id`),
    UNIQUE KEY `uk_machine_code` (`machine_code`, `deleted`),
    KEY `idx_device_type` (`device_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '设备信息表';

-- 种子数据（与当前 application-dev.yml 配置一致）
INSERT INTO `biz_machine` (`machine_code`, `machine_name`, `device_type`, `enabled`, `ip`, `api_url`, `api_write_url`, `plant`, `scale_machine_id`, `create_time`)
VALUES ('MAIN_SCALE', '主磅', 'MAIN_SCALE', 1, '10.0.97.41', 'http://10.0.97.41:8000/mws/api', 'http://10.0.97.41:8000/mws/api/wr', 'A1', 1, NOW()),
       ('MICRO_SCALE', '微量', 'MICRO_SCALE', 1, '10.0.97.40', 'http://10.0.97.40:8000/aws/api', 'http://10.0.97.40:8000/aws/api/wr', 'A1', 1, NOW()),
       ('LABELING_MACHINE', '贴标机', 'LABELING_MACHINE', 1, '192.168.99.202', NULL, NULL, NULL, NULL, NOW());
UPDATE `biz_machine` SET `port` = 9100 WHERE `machine_code` = 'LABELING_MACHINE';

-- 设备管理菜单
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '设备管理', 1, 'Machine', 1, '/business/machine/index', 0, 'business:machine:list',
       '{"title":"设备管理","icon":"ep:monitor","showParent":true}', 1, '设备管理菜单', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:machine:list');

SET @machine_menu_id := (SELECT `menu_id` FROM `sys_menu` WHERE `permission` = 'business:machine:list' LIMIT 1);

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '设备新增', 0, ' ', @machine_menu_id, '', 1, 'business:machine:add', '{"title":"设备新增"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @machine_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:machine:add');

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '设备修改', 0, ' ', @machine_menu_id, '', 1, 'business:machine:edit', '{"title":"设备修改"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @machine_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:machine:edit');

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '设备删除', 0, ' ', @machine_menu_id, '', 1, 'business:machine:remove', '{"title":"设备删除"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @machine_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:machine:remove');
