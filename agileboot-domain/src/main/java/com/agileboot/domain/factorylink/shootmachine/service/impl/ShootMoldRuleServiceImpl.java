package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldRuleEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldRuleMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldRuleService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShootMoldRuleServiceImpl extends ServiceImpl<ShootMoldRuleMapper, ShootMoldRuleEntity>
        implements ShootMoldRuleService {

    private final ShootMoldService shootMoldService;

    @Override
    public List<ShootMoldRuleEntity> listAll() {
        return enrichFieldNames(
                lambdaQuery()
                        .orderByAsc(ShootMoldRuleEntity::getMoldId)
                        .orderByAsc(ShootMoldRuleEntity::getFieldCode)
                        .list());
    }

    @Override
    public List<ShootMoldRuleEntity> listByMoldId(Long moldId) {
        shootMoldService.getByIdOrThrow(moldId);
        return enrichFieldNames(baseMapper.selectListByMoldIdWithMold(moldId));
    }

    @Override
    public ShootMoldRuleEntity getByIdOrThrow(Long id) {
        ShootMoldRuleEntity entity = getById(id);
        if (entity == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "模具规则");
        }
        return enrichFieldName(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMoldRuleEntity create(ShootMoldRuleEntity entity) {
        shootMoldService.getByIdOrThrow(entity.getMoldId());
        fillRule(entity);
        entity.setDeleted(false);
        save(entity);
        return enrichFieldName(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMoldRuleEntity update(Long id, ShootMoldRuleEntity entity) {
        ShootMoldRuleEntity existing = getByIdOrThrow(id);
        entity.setId(id);
        entity.setMoldId(existing.getMoldId());
        entity.setCreatedAt(existing.getCreatedAt());
        fillRule(entity);
        updateById(entity);
        return enrichFieldName(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getByIdOrThrow(id);
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveByMoldId(Long moldId, List<ShootMoldRuleEntity> rules) {
        shootMoldService.getByIdOrThrow(moldId);
        remove(Wrappers.<ShootMoldRuleEntity>lambdaQuery().eq(ShootMoldRuleEntity::getMoldId, moldId));
        if (CollUtil.isEmpty(rules)) {
            return;
        }
        for (ShootMoldRuleEntity rule : rules) {
            rule.setMoldId(moldId);
            rule.setId(null);
            rule.setDeleted(false);
            fillRule(rule);
        }
        saveBatch(rules);
    }

    private void fillRule(ShootMoldRuleEntity entity) {
        if (entity.getMoldId() == null || StrUtil.isBlank(entity.getFieldCode())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "模具ID和字段不能为空");
        }
        if (entity.getMinValue() == null || entity.getMaxValue() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "最小值和最大值不能为空");
        }
        if (entity.getMinValue().compareTo(entity.getMaxValue()) > 0) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "最小值不能大于最大值");
        }
        entity.setFieldCode(entity.getFieldCode().trim().toLowerCase());
        if (entity.getEnabled() == null) {
            entity.setEnabled(true);
        }
        enrichFieldName(entity);
    }

    /** 展示名统一由 field_code */
    private ShootMoldRuleEntity enrichFieldName(ShootMoldRuleEntity rule) {
        rule.setFieldName(PlcFieldKeyDisplayNames.resolveOrCode(rule.getFieldCode()));
        return rule;
    }

    private List<ShootMoldRuleEntity> enrichFieldNames(List<ShootMoldRuleEntity> rules) {
        rules.forEach(this::enrichFieldName);
        return rules;
    }
}
