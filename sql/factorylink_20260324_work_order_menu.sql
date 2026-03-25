-- =============================================
-- 工单管理菜单迁移SQL
-- =============================================

-- 1. 父菜单：工单管理
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工单管理', 1, 'WorkOrder', 1, '/business/workOrder/index', 0, 'business:workOrder:list',
       '{"title":"工单管理","icon":"ep:list","showParent":true}', 1, '工单管理菜单', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:list');

SET @work_order_menu_id := (SELECT `menu_id` FROM `sys_menu` WHERE `permission` = 'business:workOrder:list' LIMIT 1);

-- 2. 按钮：工单新增
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工单新增', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:add', '{"title":"工单新增"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:add');

-- 3. 按钮：工单修改
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工单修改', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:edit', '{"title":"工单修改"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:edit');

-- 4. 按钮：工单删除
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工单删除', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:remove', '{"title":"工单删除"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:remove');

-- 5. 按钮：工单导出
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工单导出', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:export', '{"title":"工单导出"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:export');

-- 6. 按钮：工单详情
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工单详情', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:query', '{"title":"工单详情"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:query');

-- 7. 按钮：工单确认
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '工单确认', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:confirm', '{"title":"工单确认"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:confirm');

-- 8. 按钮：待分配配方工单查询
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '待分配工单', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:pending', '{"title":"待分配工单"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:pending');

-- 9. 按钮：分配配方
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '分配配方', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:assignFormula', '{"title":"分配配方"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:assignFormula');

-- 10. 按钮：已派工工单查询
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '已派工工单', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:dispatched', '{"title":"已派工工单"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:dispatched');

-- 11. 按钮：开始生产
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '开始生产', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:startProduction', '{"title":"开始生产"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:startProduction');

-- 12. 按钮：修改工单配方
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '修改配方', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:modifyFormula', '{"title":"修改配方"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:modifyFormula');

-- 13. 按钮：设备下发
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '设备下发', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:sync', '{"title":"设备下发"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:sync');

-- 14. 按钮：取消工单
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '取消工单', 0, ' ', @work_order_menu_id, '', 1, 'business:workOrder:cancel', '{"title":"取消工单"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE @work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'business:workOrder:cancel');
