package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldRuleEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldRuleMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootRuleAlarmMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootStationScheduleMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShootMoldServiceImpl extends ServiceImpl<ShootMoldMapper, ShootMoldEntity>
        implements ShootMoldService {

    private final ShootMoldRuleMapper shootMoldRuleMapper;
    private final ShootStationScheduleMapper shootStationScheduleMapper;
    private final ShootRuleAlarmMapper shootRuleAlarmMapper;

    @Override
    public PageDTO<ShootMoldEntity> list(int pageNum, int pageSize) {
        Page<ShootMoldEntity> page = new Page<>(pageNum, pageSize);
        Page<ShootMoldEntity> result =
                lambdaQuery().orderByDesc(ShootMoldEntity::getUpdatedAt).page(page);
        return new PageDTO<>(result.getRecords(), result.getTotal());
    }

    @Override
    public ShootMoldEntity getByIdOrThrow(Long id) {
        ShootMoldEntity entity = getById(id);
        if (entity == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "模具");
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMoldEntity create(ShootMoldEntity entity) {
        validateMold(entity);
        if (entity.getEnabled() == null) {
            entity.setEnabled(true);
        }
        entity.setDeleted(false);
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMoldEntity update(Long id, ShootMoldEntity entity) {
        ShootMoldEntity existing = getByIdOrThrow(id);
        validateMold(entity);
        entity.setId(id);
        entity.setCreatedAt(existing.getCreatedAt());
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getByIdOrThrow(id);
        assertNoRules(id);
        assertNoActiveSchedule(id);
        assertNoAlarm(id);
        removeById(id);
    }

    /** 存在规则时不允许删除模具。 */
    private void assertNoRules(Long moldId) {
        Long ruleCount =
                shootMoldRuleMapper.selectCount(
                        Wrappers.<ShootMoldRuleEntity>lambdaQuery()
                                .eq(ShootMoldRuleEntity::getMoldId, moldId));
        if (ruleCount != null && ruleCount > 0) {
            throw new ApiException(
                    Client.COMMON_REQUEST_PARAMETERS_INVALID, "该模具下存在规则，请先删除规则后再删除模具");
        }
    }

    /** 存在未取消的排期时不允许删除模具。 */
    private void assertNoActiveSchedule(Long moldId) {
        Long scheduleCount =
                shootStationScheduleMapper.selectCount(
                        Wrappers.<ShootStationScheduleEntity>lambdaQuery()
                                .eq(ShootStationScheduleEntity::getMoldId, moldId)
                                .ne(
                                        ShootStationScheduleEntity::getStatus,
                                        ShootStationScheduleEntity.STATUS_CANCELLED));
        if (scheduleCount != null && scheduleCount > 0) {
            throw new ApiException(
                    Client.COMMON_REQUEST_PARAMETERS_INVALID, "该模具下存在排期，请先删除或取消排期后再删除模具");
        }
    }

    /** 存在报警时不允许删除模具。 */
    private void assertNoAlarm(Long moldId) {
        long alarmCount = shootRuleAlarmMapper.countAlarms(null, moldId, null, null);
        if (alarmCount > 0) {
            throw new ApiException(
                    Client.COMMON_REQUEST_PARAMETERS_INVALID, "该模具下存在报警记录，请先删除报警后再删除模具");
        }
    }

    // 验证
    private void validateMold(ShootMoldEntity entity) {
        if (StrUtil.isBlank(entity.getMoldModel())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "模具型号不能为空");
        }
        if (StrUtil.isBlank(entity.getColor())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "颜色不能为空");
        }
    }
}
