package com.agileboot.domain.factorylink.shootmachine.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Internal;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmExportDTO;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;

@Tag(name = "模具阈值报警")
@RestController
@RequestMapping("/api/shoot/alarm")
@RequiredArgsConstructor
public class ShootRuleAlarmController {

    private final ShootRuleAlarmService shootRuleAlarmService;
    
    // 报警级别列索引
    private static final int COLUMN_ALARM_LEVEL = 3;
    // 处理状态列索引
    private static final int COLUMN_HANDLE_STATUS = 8;

    @Operation(summary = "查询所有未处理的报警")
    @GetMapping("/unhandled")
    public ResponseDTO<List<ShootRuleAlarmEntity>> listUnhandled(@RequestParam(required = false) Long machineId) {
        return ResponseDTO.ok(shootRuleAlarmService.listUnhandledWithRelation(machineId));
    }

    @Operation(summary = "根据站位号查询报警详情")
    @GetMapping("/detail")
    public ResponseDTO<Map<String, Object>> getDetailByStationNo(@RequestParam Integer stationNo) {
        return ResponseDTO.ok(shootRuleAlarmService.getDetailByStationNo(stationNo));
    }

    @Operation(summary = "根据站位号和字段名称批量处理报警")
    @PostMapping("/handleByStation")
    public ResponseDTO<Void> handleByStation(
            @RequestParam Integer stationNo,
            @RequestParam String fieldName,
            @RequestParam(required = false) String handleRemark) {
        shootRuleAlarmService.handleByStationNoAndField(stationNo, fieldName, handleRemark);
        return ResponseDTO.ok();
    }

    @Operation(summary = "统计概览")
    @GetMapping("/statistics/count")
    public ResponseDTO<Map<String, Long>> getStatisticsOverview(@RequestParam(required = false) Long machineId) {
        return ResponseDTO.ok(shootRuleAlarmService.getStatisticsOverview(machineId));
    }

    @Operation(summary = "导出报警数据到Excel")
    @GetMapping("/export")
    public void exportAlarms(@RequestParam(required = false) Long machineId,
                             @RequestParam(defaultValue = "15") Integer days,
                             HttpServletResponse response) {
        try {
            List<ShootRuleAlarmExportDTO> exportList = shootRuleAlarmService.listAllForExport(machineId, days);
            
            // 创建ExcelWriter，使用try-with-resources自动关闭
            try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
                writer.renameSheet("报警数据");
                configureExcelWriter(writer);
                writer.write(exportList, true);
                applyAlarmLevelColors(writer, exportList.size());
                applyHandleStatusColors(writer, exportList.size());
                writer.flush(response.getOutputStream(), true);
            }
        } catch (Exception e) {
            throw new ApiException(e, Internal.EXCEL_PROCESS_ERROR, e.getMessage());
        }
    }
    
    /**
     * 配置ExcelWriter的列宽和标题别名
     */
    private void configureExcelWriter(ExcelWriter writer) {
        // 设置列宽
        int[] columnWidths = {20, 25, 15, 10, 12, 12, 12, 20, 10, 15, 10};
        for (int i = 0; i < columnWidths.length; i++) {
            writer.setColumnWidth(i, columnWidths[i]);
        }
        
        // 添加标题别名（确保字段映射正确）
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
    
    /**
     * 为报警级别列应用颜色样式
     */
    private void applyAlarmLevelColors(ExcelWriter writer, int rowCount) {
        if (rowCount == 0) {
            return;
        }
        
        org.apache.poi.ss.usermodel.Sheet sheet = writer.getSheet();
        
        // 创建一次样式，循环中复用
        CellStyle yellowStyle = writer.getWorkbook().createCellStyle();
        yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle redStyle = writer.getWorkbook().createCellStyle();
        redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 遍历数据行（从第2行开始，第1行是表头）
        for (int i = 1; i <= rowCount; i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
            if (row != null) {
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(COLUMN_ALARM_LEVEL);
                if (cell != null) {
                    String alarmLevel = cell.getStringCellValue();
                    if ("黄色".equals(alarmLevel)) {
                        cell.setCellStyle(yellowStyle);
                    } else if ("红色".equals(alarmLevel)) {
                        cell.setCellStyle(redStyle);
                    }
                }
            }
        }
    }
    
    /**
     * 为处理状态列应用颜色样式
     */
    private void applyHandleStatusColors(ExcelWriter writer, int rowCount) {
        if (rowCount == 0) {
            return;
        }
        
        org.apache.poi.ss.usermodel.Sheet sheet = writer.getSheet();
        
        // 创建一次样式，循环中复用
        CellStyle handledStyle = writer.getWorkbook().createCellStyle();
        handledStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        handledStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle unhandledStyle = writer.getWorkbook().createCellStyle();
        unhandledStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        unhandledStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 遍历数据行（从第2行开始，第1行是表头）
        for (int i = 1; i <= rowCount; i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
            if (row != null) {
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(COLUMN_HANDLE_STATUS);
                if (cell != null) {
                    String handleStatus = cell.getStringCellValue();
                    if ("已处理".equals(handleStatus)) {
                        cell.setCellStyle(handledStyle);
                    } else if ("未处理".equals(handleStatus)) {
                        cell.setCellStyle(unhandledStyle);
                    }
                }
            }
        }
    }
}
