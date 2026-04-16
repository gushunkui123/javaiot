package com.factorylink.domain.business.formula.process.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 工艺信息表 Mapper 接口
 *
 * @author Codex
 */
public interface BizFormulaProcessMapper extends BaseMapper<BizFormulaProcessEntity> {

    @Delete("DELETE FROM biz_formula_process WHERE process_code = #{processCode} AND deleted = 1")
    int deleteHistoryByProcessCode(@Param("processCode") String processCode);
}
