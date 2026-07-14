package com.agileboot.domain.factorylink.shootmachine.util;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmExportDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/** 报警数据 Excel 导出工具：列宽、标题别名、级别/状态着色 */
public final class AlarmExcelExportUtil {

    private static final int COLUMN_ALARM_LEVEL = 3;
    private static final int COLUMN_HANDLE_STATUS = 8;

    private AlarmExcelExportUtil() {
    }

    public static void export(HttpServletResponse response, List<ShootRuleAlarmExportDTO> exportList) throws Exception {
        try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
            writer.renameSheet("报警数据");
            configureWriter(writer);
            writer.write(exportList, true);
            applyAlarmLevelColors(writer, exportList.size());
            applyHandleStatusColors(writer, exportList.size());
            writer.flush(response.getOutputStream(), true);
        }
    }

    private static void configureWriter(ExcelWriter writer) {
        int[] columnWidths = {20, 25, 15, 10, 12, 12, 12, 20, 10, 15, 10};
        for (int i = 0; i < columnWidths.length; i++) {
            writer.setColumnWidth(i, columnWidths[i]);
        }
        writer.addHeaderAlias("machineName", "机器名称");
        writer.addHeaderAlias("stationName", "站位名称");
        writer.addHeaderAlias("fieldName", "报警字段");
        writer.addHeaderAlias("alarmLevel", "报警级别");
        writer.addHeaderAlias("minValue", "最小阈值");
        writer.addHeaderAlias("currentValue", "当前值");
        writer.addHeaderAlias("maxValue", "最大阈值");
        writer.addHeaderAlias("alarmTime", "报警时间");
        writer.addHeaderAlias("handleStatus", "处理状态");
        writer.addHeaderAlias("moldModel", "模具型号");
        writer.addHeaderAlias("moldColor", "模具颜色");
        writer.setOnlyAlias(true);
    }

    private static void applyAlarmLevelColors(ExcelWriter writer, int rowCount) {
        if (rowCount == 0) {
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
            Cell cell = row.getCell(COLUMN_ALARM_LEVEL);
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

    private static void applyHandleStatusColors(ExcelWriter writer, int rowCount) {
        if (rowCount == 0) {
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
            Cell cell = row.getCell(COLUMN_HANDLE_STATUS);
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
}
