package com.agileboot.domain.factorylink.shootmachine.util;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.agileboot.domain.factorylink.shootmachine.entity.AlarmExportResult;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmExportDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/** 报警数据 Excel 导出工具：按报警级别拆红/黄两个 sheet，各自表头与列不同 */
public final class AlarmExcelExportUtil {

    /** 红色报警列：含阈值与模具信息，无超时时间 */
    private static final List<ColumnMeta> RED_COLUMNS = List.of(
            new ColumnMeta("machineName", "机器名称", 20),
            new ColumnMeta("stationName", "站位名称", 25),
            new ColumnMeta("fieldName", "报警字段", 15),
            new ColumnMeta("alarmLevel", "报警级别", 10),
            new ColumnMeta("minValue", "最小阈值", 12),
            new ColumnMeta("currentValue", "当前值", 12),
            new ColumnMeta("maxValue", "最大阈值", 12),
            new ColumnMeta("alarmTime", "报警时间", 20),
            new ColumnMeta("handleStatus", "处理状态", 10),
            new ColumnMeta("moldModel", "模具型号", 15),
            new ColumnMeta("moldColor", "模具颜色", 10)
    );

    /** 黄色报警列：仅有超时时间，无阈值/模具信息 */
    private static final List<ColumnMeta> YELLOW_COLUMNS = List.of(
            new ColumnMeta("machineName", "机器名称", 20),
            new ColumnMeta("stationName", "站位名称", 25),
            new ColumnMeta("fieldName", "报警字段", 15),
            new ColumnMeta("alarmLevel", "报警级别", 10),
            new ColumnMeta("timeoutSeconds", "超时时间(秒)", 12),
            new ColumnMeta("alarmTime", "报警时间", 20),
            new ColumnMeta("handleStatus", "处理状态", 10)
    );

    private AlarmExcelExportUtil() {
    }

    public static void export(HttpServletResponse response, AlarmExportResult result) throws Exception {
        try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
            writeSheet(writer, "红色报警", result.getRedList(), RED_COLUMNS, true);
            writeSheet(writer, "黄色报警", result.getYellowList(), YELLOW_COLUMNS, false);
            writer.flush(response.getOutputStream(), true);
        }
    }

    /**
     * 写一个 sheet：首个 sheet 用 renameSheet 避免遗留空 Sheet1；其余用 setSheet 新建。
     * 每个 sheet 按各自列元数据独立设置列宽/表头别名与着色。
     */
    private static void writeSheet(ExcelWriter writer, String sheetName,
                                   List<ShootRuleAlarmExportDTO> exportList,
                                   List<ColumnMeta> columns, boolean renameDefault) {
        if (renameDefault) {
            writer.renameSheet(sheetName);
        } else {
            writer.setSheet(sheetName);
        }
        configureWriter(writer, columns);
        writer.write(exportList, true);
        applyColors(writer, exportList.size(), columns);
    }

    private static void configureWriter(ExcelWriter writer, List<ColumnMeta> columns) {
        writer.clearHeaderAlias();
        for (int i = 0; i < columns.size(); i++) {
            ColumnMeta c = columns.get(i);
            writer.setColumnWidth(i, c.width);
            writer.addHeaderAlias(c.field, c.header);
        }
        writer.setOnlyAlias(true);
    }

    private static void applyColors(ExcelWriter writer, int rowCount, List<ColumnMeta> columns) {
        if (rowCount == 0) {
            return;
        }
        int levelCol = indexOf(columns, "alarmLevel");
        int statusCol = indexOf(columns, "handleStatus");
        applyAlarmLevelColors(writer, rowCount, levelCol);
        applyHandleStatusColors(writer, rowCount, statusCol);
    }

    private static int indexOf(List<ColumnMeta> columns, String field) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).field.equals(field)) {
                return i;
            }
        }
        return -1;
    }

    private static void applyAlarmLevelColors(ExcelWriter writer, int rowCount, int levelCol) {
        if (levelCol < 0) {
            return;
        }
        Sheet sheet = writer.getSheet();
        CellStyle yellowStyle = createStyle(writer, IndexedColors.YELLOW);
        CellStyle redStyle = createStyle(writer, IndexedColors.RED);
        for (int i = 1; i <= rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Cell cell = row.getCell(levelCol);
            if (cell == null) {
                continue;
            }
            String alarmLevel = cell.getStringCellValue();
            if ("黄色".equals(alarmLevel)) {
                cell.setCellStyle(yellowStyle);
            } else if ("红色".equals(alarmLevel)) {
                cell.setCellStyle(redStyle);
            }
        }
    }

    private static void applyHandleStatusColors(ExcelWriter writer, int rowCount, int statusCol) {
        if (statusCol < 0) {
            return;
        }
        Sheet sheet = writer.getSheet();
        CellStyle handledStyle = createStyle(writer, IndexedColors.GREEN);
        CellStyle unhandledStyle = createStyle(writer, IndexedColors.GREY_25_PERCENT);
        for (int i = 1; i <= rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            Cell cell = row.getCell(statusCol);
            if (cell == null) {
                continue;
            }
            String handleStatus = cell.getStringCellValue();
            if ("已处理".equals(handleStatus)) {
                cell.setCellStyle(handledStyle);
            } else if ("未处理".equals(handleStatus)) {
                cell.setCellStyle(unhandledStyle);
            }
        }
    }

    private static CellStyle createStyle(ExcelWriter writer, IndexedColors color) {
        CellStyle style = writer.getWorkbook().createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /** 列元数据：字段名（对应 DTO 属性）、表头文案、列宽 */
    private static final class ColumnMeta {
        private final String field;
        private final String header;
        private final int width;

        private ColumnMeta(String field, String header, int width) {
            this.field = field;
            this.header = header;
            this.width = width;
        }
    }
}
