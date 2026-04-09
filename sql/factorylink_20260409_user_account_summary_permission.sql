INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `menu_type`, `router_name`, `parent_id`, `path`, `is_button`, `permission`, `meta_info`,
 `status`, `remark`, `creator_id`, `create_time`, `updater_id`, `update_time`, `deleted`)
SELECT `next_menu_id`, '账号摘要列表', 0, ' ', 5, '', 1, 'system:user:summaryList', '{"title":"账号摘要列表"}',
       1, '', 1, NOW(), NULL, NULL, 0
FROM (SELECT IFNULL(MAX(`menu_id`), 0) + 1 AS `next_menu_id` FROM `sys_menu`) menu_seq
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'system:user:summaryList');

SET @user_summary_menu_id := (SELECT `menu_id` FROM `sys_menu` WHERE `permission` = 'system:user:summaryList' LIMIT 1);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, @user_summary_menu_id
WHERE @user_summary_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu`
    WHERE `role_id` = 2
      AND `menu_id` = @user_summary_menu_id
);
