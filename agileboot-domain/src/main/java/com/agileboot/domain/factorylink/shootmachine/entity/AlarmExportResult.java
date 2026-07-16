package com.agileboot.domain.factorylink.shootmachine.entity;

import java.util.List;
import lombok.Data;

/**
 * 报警导出结果：按报警级别分成红色、黄色两部分，便于导出为两个 sheet。
 */
@Data
public class AlarmExportResult {

    /** 红色报警（阈值超标） */
    private List<ShootRuleAlarmExportDTO> redList;

    /** 黄色报警（操作超时/停机） */
    private List<ShootRuleAlarmExportDTO> yellowList;

    public AlarmExportResult() {
    }

    public AlarmExportResult(List<ShootRuleAlarmExportDTO> redList,
                             List<ShootRuleAlarmExportDTO> yellowList) {
        this.redList = redList;
        this.yellowList = yellowList;
    }
}
