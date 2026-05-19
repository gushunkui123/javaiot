package com.agileboot.domain.factorylink.shootmachine.controller;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "射出机管理")
@RestController
@RequestMapping("/api/shoot/machines")
@RequiredArgsConstructor
public class ShootMachineController {

    private final ShootMachineService shootMachineService;

    @Operation(summary = "查询机台列表（含 PLC 最新采集时间与运行/停机状态）")
    @GetMapping
    public ResponseDTO<PageDTO<ShootMachineEntity>> list(
            @Parameter(description = "页码", example = "1")
            @RequestParam(value = "pageNum", defaultValue = "1")
            int pageNum,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(value = "pageSize", defaultValue = "20")
            int pageSize) {
        IPage<ShootMachineEntity> page = shootMachineService.list(pageNum, pageSize);
        return ResponseDTO.ok(new PageDTO<>(page.getRecords(), page.getTotal()));
    }

    @Operation(summary = "查询机台详情")
    @GetMapping("/{id}")
    public ResponseDTO<ShootMachineEntity> getById(
            @Parameter(description = "机台ID", required = true)
            @PathVariable("id")
            Long id) {
        return ResponseDTO.ok(shootMachineService.getByIdOrThrow(id));
    }

    @Operation(summary = "新增机台")
    @PostMapping("/create")
    public ResponseDTO<ShootMachineEntity> create(
            @Parameter(description = "机台信息", required = true)
            @RequestBody
            ShootMachineEntity entity) {
        return ResponseDTO.ok(shootMachineService.create(entity));
    }

    @Operation(summary = "编辑机台")
    @PutMapping("/{id}")
    public ResponseDTO<ShootMachineEntity> update(
            @Parameter(description = "机台ID", required = true)
            @PathVariable("id")
            Long id,
            @Parameter(description = "机台信息", required = true)
            @RequestBody
            ShootMachineEntity entity) {
        return ResponseDTO.ok(shootMachineService.update(id, entity));
    }

    @Operation(summary = "删除机台")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(
            @Parameter(description = "机台ID", required = true)
            @PathVariable("id")
            Long id) {
        shootMachineService.delete(id);
        return ResponseDTO.ok();
    }


}
