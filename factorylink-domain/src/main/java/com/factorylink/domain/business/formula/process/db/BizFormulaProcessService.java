package com.factorylink.domain.business.formula.process.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 工艺信息表 服务类
 *
 * @author Codex
 */
public interface BizFormulaProcessService extends IService<BizFormulaProcessEntity> {

    /**
     * 校验工艺编号是否重复
     *
     * @param processId 工艺ID（更新时排除自身）
     * @param processCode 工艺编号
     * @return 是否重复
     */
    boolean isProcessCodeDuplicated(Long processId, String processCode);

    /**
     * 删除同工艺编号的历史逻辑删除记录
     *
     * @param processCode 工艺编号
     * @return 删除条数
     */
    int deleteHistoryByProcessCode(String processCode);
}
