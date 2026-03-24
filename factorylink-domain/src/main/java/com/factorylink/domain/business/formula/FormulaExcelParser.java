package com.factorylink.domain.business.formula;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.factorylink.domain.business.formula.dto.FormulaExcelDTO;
import com.factorylink.domain.business.formula.dto.FormulaExcelDTO.Item;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 配方 Excel 解析器
 * <p>
 * 解析固定格式的配方制造令 Excel 文件。
 * 头部区域 A1:F12 为固定信息，A13 起为四种分类的原料明细。
 * 自动跳过空 sheet，选取第一个有数据的 sheet 进行解析。
 */
public class FormulaExcelParser {

    /** 四种原料分类关键词 */
    private static final List<String> CATEGORY_KEYWORDS = List.of(
        "主料", "顆粒藥品", "粉未藥品", "色粒"
    );

    private FormulaExcelParser() {
    }

    /**
     * 从 InputStream 解析配方 Excel
     */
    public static FormulaExcelDTO parse(InputStream inputStream) {
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        try {
            reader = pickDataSheet(reader);
            return doParse(reader);
        } finally {
            reader.close();
        }
    }

    /**
     * 从文件路径解析配方 Excel
     */
    public static FormulaExcelDTO parse(String filePath) {
        ExcelReader reader = ExcelUtil.getReader(filePath);
        try {
            reader = pickDataSheet(reader);
            return doParse(reader);
        } finally {
            reader.close();
        }
    }

    /**
     * 自动选取第一个有数据（行数 > 1）的 sheet
     */
    private static ExcelReader pickDataSheet(ExcelReader reader) {
        Workbook workbook = reader.getWorkbook();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 1) {
                reader.setSheet(sheet);
                return reader;
            }
        }
        return reader;
    }

    private static FormulaExcelDTO doParse(ExcelReader reader) {
        FormulaExcelDTO dto = new FormulaExcelDTO();

        // --- 解析头部 ---
        dto.setSheetName(reader.getSheet().getSheetName());
        dto.setTitle(readCellStr(reader, 0, 3));          // D1
        dto.setDate(readCellStr(reader, 2, 0));            // A3
        dto.setFormulaCode(readCellStr(reader, 2, 6));     // G3  配方编号
        dto.setCustomer(readCellStr(reader, 3, 1));        // B4
        dto.setSpecification(readCellStr(reader, 3, 3));   // D4
        dto.setModel(readCellStr(reader, 5, 3));           // D6
        dto.setBatch(readCellStr(reader, 7, 3));           // D8
        dto.setBatchCount(readCellStr(reader, 7, 5));      // F8
        dto.setOrderNo(readCellStr(reader, 9, 2));         // C10

        // --- 解析明细 (A13 起, 行索引 12 起) ---
        int rowCount = reader.getRowCount();
        String currentCategory = null;
        List<Item> items = new ArrayList<>();
        int sortOrder = 1;

        for (int rowIdx = 12; rowIdx < rowCount; rowIdx++) {
            String aVal = readCellStr(reader, rowIdx, 0);  // A列 — 分类
            String cVal = readCellStr(reader, rowIdx, 2);  // C列 — 代号
            Object dVal = readCell(reader, rowIdx, 3);     // D列 — 比率
            String eVal = readCellStr(reader, rowIdx, 4);  // E列 — 单位
            Object fVal = readCell(reader, rowIdx, 5);     // F列 — 重量

            // 遇到合计行则停止
            if (StrUtil.isNotBlank(aVal) && aVal.contains("合計")) {
                break;
            }

            // 检测分类行
            if (StrUtil.isNotBlank(aVal) && isCategoryLabel(aVal)) {
                currentCategory = aVal.trim();
            }

            // C 列和 D 列都有值才是有效的原料行
            if (StrUtil.isNotBlank(cVal) && dVal != null) {
                Item item = new Item();
                item.setSortOrder(sortOrder++);
                item.setCategory(currentCategory);
                item.setCode(cVal.trim());
                item.setRatio(toBigDecimal(dVal));
                item.setUnit(StrUtil.trimToEmpty(eVal));
                item.setWeight(toBigDecimal(fVal));
                items.add(item);
            }
        }

        dto.setItems(items);
        return dto;
    }

    /**
     * 判断 A 列值是否为分类标签
     */
    private static boolean isCategoryLabel(String value) {
        for (String keyword : CATEGORY_KEYWORDS) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 安全读取单元格字符串值（处理合并单元格）
     */
    private static String readCellStr(ExcelReader reader, int rowIdx, int colIdx) {
        Object val = readCell(reader, rowIdx, colIdx);
        return val != null ? val.toString().trim() : null;
    }

    /**
     * 安全读取单元格原始值（处理合并单元格）
     */
    private static Object readCell(ExcelReader reader, int rowIdx, int colIdx) {
        // Hutool ExcelReader 的 readCellValue 会自动处理合并单元格
        return reader.readCellValue(colIdx, rowIdx);
    }

    private static BigDecimal toBigDecimal(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        String str = val.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        return new BigDecimal(str);
    }
}
