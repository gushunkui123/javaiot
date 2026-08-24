package com.agileboot.domain.factorylink.shootmachine.entity;

import com.agileboot.common.annotation.ExcelColumn;
import com.agileboot.common.annotation.ExcelSheet;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@ExcelSheet(name = "报警数据")
public class ShootRuleAlarmExportDTO {

    @ExcelColumn(name = "机器名称")
    private String machineName;

    @ExcelColumn(name = "站位名称")
    private String stationName;

    @ExcelColumn(name = "报警字段")
    private String fieldName;

    @ExcelColumn(name = "报警级别")
    private String alarmLevel;

    @ExcelColumn(name = "最小阈值")
    private String minValue;

    @ExcelColumn(name = "当前值")
    private String currentValue;

    @ExcelColumn(name = "最大阈值")
    private String maxValue;

    @ExcelColumn(name = "超时时间(秒)")
    private String timeoutSeconds;

    @ExcelColumn(name = "出现次数")
    private String occurrenceCount;

    @ExcelColumn(name = "首次报警时间")
    private String firstAlarmTime;

    @ExcelColumn(name = "末次报警时间")
    private String lastAlarmTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExcelColumn(name = "报警时间")
    private String alarmTime;

    @ExcelColumn(name = "处理状态")
    private String handleStatus;

    @ExcelColumn(name = "模具型号")
    private String moldModel;

    @ExcelColumn(name = "模具颜色")
    private String moldColor;
}
