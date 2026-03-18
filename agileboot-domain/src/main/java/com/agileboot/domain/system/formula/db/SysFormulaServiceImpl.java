package com.agileboot.domain.system.formula.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 配方信息表 服务实现类
 *
 * @author Codex
 */
@Service
public class SysFormulaServiceImpl extends ServiceImpl<SysFormulaMapper, SysFormulaEntity> implements SysFormulaService {

    @Override
    public boolean isFormulaCodeDuplicated(Long formulaId, String formulaCode) {
        QueryWrapper<SysFormulaEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(formulaId != null, "formula_id", formulaId)
            .eq("formula_code", formulaCode);
        return baseMapper.exists(queryWrapper);
    }

}
