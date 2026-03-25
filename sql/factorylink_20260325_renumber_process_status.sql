-- 重编 process_status 流程状态值，使其连续
-- 旧值映射：3→2, 4→3, 5→4, 6→5
-- 必须按从小到大的顺序执行，避免覆盖冲突

UPDATE biz_work_order SET process_status = 2 WHERE process_status = 3;
UPDATE biz_work_order SET process_status = 3 WHERE process_status = 4;
UPDATE biz_work_order SET process_status = 4 WHERE process_status = 5;
UPDATE biz_work_order SET process_status = 5 WHERE process_status = 6;

-- 更新字段注释
ALTER TABLE biz_work_order MODIFY COLUMN `process_status` tinyint DEFAULT NULL COMMENT '流程状态(1:已创建 2:已添加配方 3:生产中 4:已完成 5:已取消)';
