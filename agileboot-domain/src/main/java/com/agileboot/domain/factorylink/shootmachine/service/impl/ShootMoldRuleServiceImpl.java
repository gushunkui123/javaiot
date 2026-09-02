package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.plc.entity.FieldMappingEntity;
import com.agileboot.domain.factorylink.plc.mapper.FieldMappingMapper;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldRuleEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldRuleMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootDeleteValidator;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldRuleService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final FieldMappingMapper fieldMappingMapper;

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
        // fieldCode 即抽象内部键（如 MOLD_SET_TEMP / GUN_TEMP），与设备 L/R/枪号解耦，比较时由排期实例化
        if (StrUtil.isBlank(entity.getFieldCode())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "fieldCode不能为空");
        }
        if (StrUtil.isBlank(entity.getDimensionType())
                || (!"GLOBAL".equals(entity.getDimensionType()) && !"STAGE".equals(entity.getDimensionType()))) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "dimensionType必须为 GLOBAL 或 STAGE");
        }
        if (entity.getMinValue() == null || entity.getMaxValue() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "最小值和最大值不能为空");
        }
        if (entity.getMinValue().compareTo(entity.getMaxValue()) >= 0) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "最小值必须小于最大值");
        }
        // fieldCode 规范化（去空格 + 大写）
        entity.setFieldCode(entity.getFieldCode().trim().toUpperCase().replaceAll("\\s+", ""));
        if (entity.getEnabled() == null) {
            entity.setEnabled(true);
        }
        if ("GLOBAL".equals(entity.getDimensionType())) {
            entity.setStage(null);
        }
        enrichFieldName(entity);
    }

    /** 展示名统一由 fieldCode + field_mapping.match_pattern */
    private ShootMoldRuleEntity enrichFieldName(ShootMoldRuleEntity rule) {
        String displayName = resolveDisplayName(rule.getFieldCode());
        rule.setFieldName(displayName);
        return rule;
    }

    private String resolveDisplayName(String internalKey) {
        if (StrUtil.isBlank(internalKey)) {
            return internalKey;
        }
        FieldMappingEntity mapping = fieldMappingMapper.selectByInternalKey(internalKey);
        if (mapping != null && StrUtil.isNotBlank(mapping.getMatchPattern())) {
            return stripMoldSidePrefix(mapping.getMatchPattern());
        }
        return internalKey;
    }

    /** 从预查的 patternMap 中取 match_pattern 并剥前缀，不查 DB（批量场景使用） */
    private String resolveDisplayNameFromMap(String internalKey, Map<String, String> patternMap) {
        if (StrUtil.isBlank(internalKey)) {
            return internalKey;
        }
        String pattern = patternMap.get(internalKey);
        if (StrUtil.isNotBlank(pattern)) {
            return stripMoldSidePrefix(pattern);
        }
        return internalKey;
    }

    /** 去掉"左模"/"右模"前缀，保留 {idx}/{stage}/{gun} 占位符 */
    private String stripMoldSidePrefix(String name) {
        if (name.startsWith("左模")) {
            return name.substring(2);
        } else if (name.startsWith("右模")) {
            return name.substring(2);
        }
        return name;
    }

    private List<ShootMoldRuleEntity> enrichFieldNames(List<ShootMoldRuleEntity> rules) {
        if (CollUtil.isEmpty(rules)) {
            return rules;
        }
        // 批量查询 field_mapping，消除 N+1：一次 IN 查询取回所有 internalKey 的 match_pattern
        Set<String> internalKeys = rules.stream()
                .map(ShootMoldRuleEntity::getFieldCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (internalKeys.isEmpty()) {
            return rules;
        }
        List<FieldMappingEntity> mappings = fieldMappingMapper.selectByInternalKeys(internalKeys);
        // 同一 internalKey 可能有多条（左模/右模），取第一条即可（resolveDisplayName 会剥前缀）
        Map<String, String> patternMap = mappings.stream()
                .collect(Collectors.toMap(
                        FieldMappingEntity::getInternalKey,
                        FieldMappingEntity::getMatchPattern,
                        (first, second) -> first));
        for (ShootMoldRuleEntity rule : rules) {
            String displayName = resolveDisplayNameFromMap(rule.getFieldCode(), patternMap);
            rule.setFieldName(displayName);
        }
        return rules;
    }
}
