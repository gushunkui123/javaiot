-- 模具停用自动取消排期/红色报警：为按 mold_id 的批量 UPDATE 提供索引，
-- 缩小行锁范围，降低与 10 秒调度任务并发写 shoot_rule_alarm / shoot_station_schedule 时的死锁概率
ALTER TABLE shoot_rule_alarm ADD INDEX idx_alarm_mold (mold_id);
ALTER TABLE shoot_station_schedule ADD INDEX idx_schedule_mold (mold_id);
