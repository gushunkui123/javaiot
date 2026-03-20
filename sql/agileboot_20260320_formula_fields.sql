-- 配方表新增字段：日期、模具代号、批次、手数、生产订单号
ALTER TABLE `biz_formula`
    ADD COLUMN `formula_date` varchar(50) DEFAULT NULL COMMENT '日期' AFTER `formula_name`,
    ADD COLUMN `mold_code`    varchar(50) DEFAULT NULL COMMENT '模具代号' AFTER `formula_date`,
    ADD COLUMN `batch`        varchar(50) DEFAULT NULL COMMENT '批次' AFTER `mold_code`,
    ADD COLUMN `batch_count`  varchar(50) DEFAULT NULL COMMENT '手数' AFTER `batch`,
    ADD COLUMN `order_no`     varchar(50) DEFAULT NULL COMMENT '生产订单号' AFTER `batch_count`;

-- 配方明细表新增字段：比率、重量单位
ALTER TABLE `biz_formula_item`
    ADD COLUMN `ratio`       decimal(18, 6) DEFAULT NULL COMMENT '比率' AFTER `material_weight`,
    ADD COLUMN `weight_unit` varchar(10)    DEFAULT 'g'  COMMENT '重量单位' AFTER `ratio`;
