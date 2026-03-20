package com.agileboot.domain.business.formula.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 配方 Excel 解析结果
 */
@Data
public class FormulaExcelDTO {

    /** 文档标题 (D1) */
    private String title;

    /** 日期 (A3) */
    private String date;

    /** 配方编号 (G3) */
    private String formulaCode;

    /** 客户 (B4) */
    private String customer;

    /** 规格/颜色 (D4) */
    private String specification;

    /** 型体/模具号 (D6) */
    private String model;

    /** 批次 (D8) */
    private String batch;

    /** 手数 (F8) */
    private String batchCount;

    /** 生产订单号 (C10) — 用作配方编号 */
    private String orderNo;

    /** Sheet 名称 — 用作配方名称 */
    private String sheetName;

    /** 配方明细列表 */
    private List<Item> items;

    @Data
    public static class Item {

        /** 排序号 */
        private int sortOrder;

        /** 原料分类: 主料/Category, 顆粒藥品/Particle Chemical, 粉未藥品/Powder Chemical, 色粒/Pigment */
        private String category;

        /** 原料代号 (C列) */
        private String code;

        /** 比率 (D列) */
        private BigDecimal ratio;

        /** 单位 (E列) */
        private String unit;

        /** 重量 (F列) */
        private BigDecimal weight;
    }
}
