package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldRuleMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootRuleAlarmMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootStationScheduleMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootDeleteValidator;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ShootMoldServiceImpl extends ServiceImpl<ShootMoldMapper, ShootMoldEntity>
        implements ShootMoldService {

    /** 模具停用时自动取消排期/红色报警的处理备注 */
    private static final String MOLD_DISABLE_CANCEL_REMARK = "模具停用自动取消";

    private final ShootDeleteValidator deleteValidator;
    private final ShootMoldRuleMapper moldRuleMapper;
    private final ShootStationScheduleMapper scheduleMapper;
    private final ShootRuleAlarmMapper alarmMapper;

    @Override
    public PageDTO<ShootMoldEntity> list(int pageNum, int pageSize, Boolean enabled, String moldModel, String color) {
        Page<ShootMoldEntity> page = new Page<>(pageNum, pageSize);
        var query = lambdaQuery();
        // enabled 非 null 时按值过滤（1 启用 / 0 禁用）；不传则返回全部（模具管理页需看到停用模具）
        if (enabled != null) {
            query.eq(ShootMoldEntity::getEnabled, enabled);
        }
        // 模具型号/颜色模糊匹配（留空不参与过滤）
        if (StrUtil.isNotBlank(moldModel)) {
            query.like(ShootMoldEntity::getMoldModel, moldModel);
        }
        if (StrUtil.isNotBlank(color)) {
            query.like(ShootMoldEntity::getColor, color);
        }
        query.orderByDesc(ShootMoldEntity::getUpdatedAt);
        Page<ShootMoldEntity> result = query.page(page);
        return new PageDTO<>(result.getRecords(), result.getTotal());
    }

    @Override
    public ShootMoldEntity getByIdOrThrow(Long id) {
        ShootMoldEntity entity = getById(id);
        if (entity == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID ,"请先添加模具信息");
        }
        return entity;
    }

    @Override
    public ShootMoldEntity getEnabledOrThrow(Long id) {
        ShootMoldEntity entity = getByIdOrThrow(id);
        if (!Boolean.TRUE.equals(entity.getEnabled())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "已停用的模具不能排产");
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMoldEntity create(ShootMoldEntity entity) {
        validateMold(entity);
        assertMoldModelUnique(entity.getMoldModel(), null);
        if (entity.getEnabled() == null) {
            entity.setEnabled(true);
        }
        entity.setDeleted(false);
        save(entity);
        return entity;
    }

    @Override
    public ShootMoldEntity update(Long id, ShootMoldEntity entity) {
        ShootMoldEntity existing = getByIdOrThrow(id);
        validateMold(entity);
        assertMoldModelUnique(entity.getMoldModel(), id);
        // 模具由启用切为停用：取消其未结束排期，并自动取消该模具未处理红色报警（看板不再报警）
        boolean disabling = Boolean.TRUE.equals(existing.getEnabled())
                && Boolean.FALSE.equals(entity.getEnabled());
        entity.setId(id);
        // 注意：updateById 与取消动作各自独立提交，不包在同一个事务里。
        // 否则取消时与 10 秒调度任务并发产生死锁，会把"已禁用"的修改一起回滚掉。
        updateById(entity);
        if (disabling) {
            executeWithDeadlockRetry(() -> scheduleMapper.cancelUnfinishedByMoldId(id, MOLD_DISABLE_CANCEL_REMARK));
            executeWithDeadlockRetry(() -> alarmMapper.cancelUnhandledRedByMoldId(id, MOLD_DISABLE_CANCEL_REMARK));
        }
        return entity;
    }

    /** 死锁自动重试（最多3次），消除与调度任务的并发锁冲突 */
    private void executeWithDeadlockRetry(Supplier<Integer> statement) {
        int maxAttempts = 3;
        for (int attempt = 1; ; attempt++) {
            try {
                statement.get();
                return;
            } catch (DeadlockLoserDataAccessException e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getByIdOrThrow(id);
        // 删除模具时直接连带删除其阈值规则，不再因存在规则而拦截
        moldRuleMapper.hardDeleteByMoldId(id);
        deleteValidator.assertNoMoldActiveSchedule(id);
        deleteValidator.assertNoAlarm(null, id, null, null);
        removeById(id);
    }

    // 验证
    private void validateMold(ShootMoldEntity entity) {
        if (StrUtil.isBlank(entity.getMoldModel())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "模具型号不能为空");
        }
    }

    /** 校验模具型号在"未删除"数据中唯一；excludeId 用于更新时排除自身 */
    private void assertMoldModelUnique(String moldModel, Long excludeId) {
        var query = lambdaQuery()
                .eq(ShootMoldEntity::getMoldModel, moldModel)
                .eq(ShootMoldEntity::getDeleted, false);
        if (excludeId != null) {
            query.ne(ShootMoldEntity::getId, excludeId);
        }
        if (query.count() > 0) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "模具型号已存在：" + moldModel);
        }
    }
}
