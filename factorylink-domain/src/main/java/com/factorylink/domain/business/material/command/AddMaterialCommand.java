package com.factorylink.domain.business.material.command;

import com.factorylink.common.annotation.ExcelColumn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Codex
 */
@Data
@Schema(name = "新增原料命令")
public class AddMaterialCommand {

    @ExcelColumn(name = "原料类型")
    @Schema(description = "原料类型")
    @NotBlank(message = "原料类型不能为空")
    @Size(max = 64, message = "原料类型长度不能超过64个字符")
    protected String materialType;

    @ExcelColumn(name = "原料名称")
    @Schema(description = "原料名称")
    @NotBlank(message = "原料名称不能为空")
    @Size(max = 128, message = "原料名称长度不能超过128个字符")
    protected String materialName;

}
