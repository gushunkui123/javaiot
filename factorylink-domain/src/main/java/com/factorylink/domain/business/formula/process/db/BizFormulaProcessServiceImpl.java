package com.factorylink.domain.business.formula.process.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 工艺信息表 服务实现类
 *
 * @author Codex
 */
@Service
public class BizFormulaProcessServiceImpl
        extends ServiceImpl<BizFormulaProcessMapper, BizFormulaProcessEntity> implements BizFormulaProcessService {

    @Override
    public boolean isProcessCodeDuplicated(Long processId, String processCode) {
        QueryWrapper<BizFormulaProcessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(processId != null, "process_id", processId)
            .eq("process_code", processCode);
        return baseMapper.exists(queryWrapper);
    }

    @Override
    public int deleteHistoryByProcessCode(String processCode) {
        return baseMapper.deleteHistoryByProcessCode(processCode);
    }
}
