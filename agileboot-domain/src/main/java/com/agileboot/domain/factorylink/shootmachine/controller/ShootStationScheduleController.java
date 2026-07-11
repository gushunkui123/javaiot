package com.agileboot.domain.factorylink.shootmachine.controller;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.agileboot.domain.factorylink.shootmachine.service.BatchCreateStationScheduleRequest;
import com.agileboot.domain.factorylink.shootmachine.service.BatchCreateStationScheduleResult;
import com.agileboot.domain.factorylink.shootmachine.service.ShootStationScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@Tag(name = "射出机模具生产记录")
@RestController
@RequestMapping("/api/shoot")
@RequiredArgsConstructor
public class ShootStationScheduleController {

    private final ShootStationScheduleService shootStationScheduleService;

    @Operation(summary = "查询站位下的生产记录列表（时间轴）")
    @GetMapping("/station-schedules")
    public ResponseDTO<List<ShootStationScheduleEntity>> listByStation(
            @Parameter(description = "站位ID", required = true) @RequestParam Long stationId,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @Parameter(description = "模向：LEFT 左模 / RIGHT 右模") @RequestParam(required = false) String moldSide) {
        return ResponseDTO.ok(shootStationScheduleService.listByStationId(stationId, startDate, endDate, moldSide));
    }

    @Operation(summary = "查询机台当前时刻各站位生效中的生产记录")
    @GetMapping("/current")
    public ResponseDTO<List<ShootStationScheduleEntity>> listCurrentByMachine(
            @Parameter(description = "机台ID", required = true) @RequestParam Long machineId) {
        return ResponseDTO.ok(shootStationScheduleService.listCurrentByMachineId(machineId));
    }

    @Operation(summary = "查询生产记录详情")
    @GetMapping("/{id}")
    public ResponseDTO<ShootStationScheduleEntity> getById(
            @Parameter(description = "生产记录ID", required = true) @PathVariable Long id) {
        return ResponseDTO.ok(shootStationScheduleService.getByIdOrThrow(id));
    }

    @Operation(summary = "新增生产记录")
    @PostMapping("/create")
    public ResponseDTO<ShootStationScheduleEntity> create(@RequestBody ShootStationScheduleEntity entity) {
        return ResponseDTO.ok(shootStationScheduleService.create(entity));
    }

    @Operation(summary = "批量新增生产记录")
    @PostMapping("/batch")
    public ResponseDTO<BatchCreateStationScheduleResult> batchCreate(
            @RequestBody BatchCreateStationScheduleRequest request) {
        return ResponseDTO.ok(shootStationScheduleService.batchCreate(request));
    }

    @Operation(summary = "编辑生产记录")
    @PutMapping("/{id}")
    public ResponseDTO<ShootStationScheduleEntity> update(
            @Parameter(description = "生产记录ID", required = true) @PathVariable Long id,
            @RequestBody ShootStationScheduleEntity entity) {
        return ResponseDTO.ok(shootStationScheduleService.update(id, entity));
    }

    @Operation(summary = "删除生产记录")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(
            @Parameter(description = "生产记录ID", required = true) @PathVariable Long id) {
        shootStationScheduleService.delete(id);
        return ResponseDTO.ok();
    }

    @Operation(summary = "取消生产记录")
    @PatchMapping("/{id}/cancel")
    public ResponseDTO<Void> cancel(
            @Parameter(description = "生产记录ID", required = true) @PathVariable Long id) {
        shootStationScheduleService.cancel(id);
        return ResponseDTO.ok();
    }
}
