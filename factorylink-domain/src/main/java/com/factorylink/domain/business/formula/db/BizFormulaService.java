package com.factorylink.domain.business.formula.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 配方信息表 服务类
 *
 * @author Codex
 */
public interface BizFormulaService extends IService<BizFormulaEntity> {

    /**
     * 校验配方编号是否重复
     *
     * @param formulaId   配方ID（更新时排除自身）
     * @param formulaCode 配方编号
     * @return 是否重复
     */
    boolean isFormulaCodeDuplicated(Long formulaId, String formulaCode);

    /**
     * 删除同配方编号的历史逻辑删除记录
     *
     * @param formulaCode 配方编号
     * @return 删除条数
     */
    int deleteHistoryByFormulaCode(String formulaCode);
}
