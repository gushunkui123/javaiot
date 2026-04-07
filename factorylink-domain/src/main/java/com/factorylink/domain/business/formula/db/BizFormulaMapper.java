package com.factorylink.domain.business.formula.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 配方信息表 Mapper 接口
 *
 * @author Codex
 */
public interface BizFormulaMapper extends BaseMapper<BizFormulaEntity> {

    @Delete("DELETE FROM biz_formula WHERE formula_code = #{formulaCode} AND deleted = 1")
    int deleteHistoryByFormulaCode(@Param("formulaCode") String formulaCode);
}
