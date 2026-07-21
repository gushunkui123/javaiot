package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldRuleEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldRuleMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootDeleteValidator;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldRuleService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShootMoldRuleServiceImpl extends ServiceImpl<ShootMoldRuleMapper, ShootMoldRuleEntity>
        implements ShootMoldRuleService {


    private final ShootMoldService shootMoldService;
    private final ShootDeleteValidator deleteValidator;

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
        // 全局规则（moldId = 0）不需要验证模具是否存在
        if (entity.getMoldId() != null && entity.getMoldId() != 0) {
            shootMoldService.getByIdOrThrow(entity.getMoldId());
        }
        fillRule(entity);
        entity.setDeleted(false);
        try {
            save(entity);
        } catch (DuplicateKeyException e) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "该模具已存在相同字段的规则");
        }
        return enrichFieldName(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMoldRuleEntity update(Long id, ShootMoldRuleEntity entity) {
        ShootMoldRuleEntity existing = getByIdOrThrow(id);
        entity.setId(id);
        entity.setMoldId(existing.getMoldId());
        fillRule(entity);
        updateById(entity);
        return enrichFieldName(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getByIdOrThrow(id);
        deleteValidator.assertNoAlarm(null, null, null, id);
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveByMoldId(Long moldId, List<ShootMoldRuleEntity> rules) {
        shootMoldService.getByIdOrThrow(moldId);
        // 物理删除：绕过 @TableLogic 软删除，避免唯一键冲突
        baseMapper.hardDeleteByMoldId(moldId);
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
        // 全局规则（moldId = 0）不需要验证模具ID
        boolean isGlobalRule = entity.getMoldId() != null && entity.getMoldId() == 0;
        if (!isGlobalRule && entity.getMoldId() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "模具ID不能为空");
        }
        if (StrUtil.isBlank(entity.getFieldCode())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "字段不能为空");
        }
        if (entity.getMinValue() == null || entity.getMaxValue() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "最小值和最大值不能为空");
        }
        if (entity.getMinValue().compareTo(entity.getMaxValue()) >= 0) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "最小值必须小于最大值");
        }
        entity.setFieldCode(PlcFieldKeyDisplayNames.normalizeGunTemperatureFieldCode(
                entity.getFieldCode().trim().toLowerCase()));
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
