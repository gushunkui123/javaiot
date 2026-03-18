package com.agileboot.domain.business.formula.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 配方信息表 服务实现类
 *
 * @author Codex
 */
@Service
public class BizFormulaServiceImpl extends ServiceImpl<BizFormulaMapper, BizFormulaEntity> implements BizFormulaService {

    @Override
    public boolean isFormulaCodeDuplicated(Long formulaId, String formulaCode) {
        QueryWrapper<BizFormulaEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(formulaId != null, "formula_id", formulaId)
            .eq("formula_code", formulaCode);
        return baseMapper.exists(queryWrapper);
    }

}
