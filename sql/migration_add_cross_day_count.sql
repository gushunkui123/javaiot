-- 为 shoot_station_schedule 表添加 cross_day_count 字段
-- 记录计划跨过的自然日数：仅按 start_time / end_time 的"年月日"之差计算，忽略时分秒
-- 0 = 不跨天，1 = 跨1天，N = 跨N天
-- 该值为派生字段，由后端在新增 / 编辑 / 批量新增时自动计算并落库，前端只读展示，不再前端计算
ALTER TABLE shoot_station_schedule
  ADD COLUMN cross_day_count INT NOT NULL DEFAULT 0
  COMMENT '跨自然日数（按年月日计算，忽略时分秒，0=不跨天，N=跨N天）';
