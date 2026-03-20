package com.agileboot.domain.business.formula;

import com.agileboot.domain.business.formula.dto.FormulaExcelDTO;
import com.agileboot.domain.business.formula.dto.FormulaExcelDTO.Item;

/**
 * 配方 Excel 解析验证入口
 */
public class FormulaExcelParserMain {

    private static final String DEFAULT_PATH = "D:/lable/FactoryLink/配方.xlsx";

    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : DEFAULT_PATH;
        System.out.println("解析文件: " + filePath);
        System.out.println();

        FormulaExcelDTO dto = FormulaExcelParser.parse(filePath);

        // --- 头部信息 ---
        System.out.println("========== 头部信息 ==========");
        System.out.println("Sheet名称(配方名称): " + dto.getSheetName());
        System.out.println("标题:              " + dto.getTitle());
        System.out.println("日期:              " + dto.getDate());
        System.out.println("配方编号(G3):      " + dto.getFormulaCode());
        System.out.println("客户:              " + dto.getCustomer());
        System.out.println("规格/颜色:         " + dto.getSpecification());
        System.out.println("型体/模具号:       " + dto.getModel());
        System.out.println("批次:              " + dto.getBatch());
        System.out.println("手数:              " + dto.getBatchCount());
        System.out.println("生产订单号:   " + dto.getOrderNo());

        // --- 明细信息 ---
        System.out.println();
        System.out.println("========== 配方明细 ==========");

        String lastCategory = null;
        for (Item item : dto.getItems()) {
            if (!item.getCategory().equals(lastCategory)) {
                lastCategory = item.getCategory();
                System.out.println();
                System.out.printf("[%s]%n", lastCategory);
                System.out.printf("  %3s  %-25s %10s %4s %10s%n", "#", "代号", "比率", "单位", "重量");
                System.out.printf("  %3s  %-25s %10s %4s %10s%n", "---", "-------------------------", "----------", "----", "----------");
            }
            System.out.printf("  %3d  %-25s %10s %4s %10s%n",
                item.getSortOrder(),
                item.getCode(),
                item.getRatio(),
                item.getUnit(),
                item.getWeight());
        }

        // --- 汇总 ---
        System.out.println();
        System.out.println("========== 汇总 ==========");
        System.out.println("原料总条数: " + dto.getItems().size());

        long categoryCount = dto.getItems().stream()
            .map(Item::getCategory).distinct().count();
        System.out.println("分类数量:   " + categoryCount);
    }
}
