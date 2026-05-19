package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ShootMachineServiceImpl extends ServiceImpl<ShootMachineMapper, ShootMachineEntity>
        implements ShootMachineService {

    /** 超过该时长未收到 PLC 数据视为停机（2 分钟） */
    private static final long PLC_STOP_THRESHOLD_MS = 2 * 60 * 1000L;

    private final PlcDataService plcDataService;

    @Override
    public IPage<ShootMachineEntity> list(int pageNum, int pageSize) {
        IPage<ShootMachineEntity> result =
                activeQuery().orderByDesc(ShootMachineEntity::getUpdatedAt).page(new Page<>(pageNum, pageSize));
        fillPlcRunStatus(result.getRecords());
        return result;
    }

    @Override
    public ShootMachineEntity getByIdOrThrow(Long id) {
        ShootMachineEntity entity = getActiveById(id);
        if (entity == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "机台");
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMachineEntity create(ShootMachineEntity entity) {
        checkMachineCodeUnique(entity.getMachineCode(), null);
        checkIpUnique(entity.getIp(), null);
        entity.setDeleted(false);
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMachineEntity update(Long id, ShootMachineEntity entity) {
        ShootMachineEntity existing = getByIdOrThrow(id);
        checkMachineCodeUnique(entity.getMachineCode(), id);
        checkIpUnique(entity.getIp(), id);
        entity.setId(id);
        entity.setCreatedAt(existing.getCreatedAt());
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ShootMachineEntity entity = getByIdOrThrow(id);
        entity.setDeleted(true);
        updateById(entity);
    }

    /** 查询未逻辑删除的机台 */
    private ShootMachineEntity getActiveById(Long id) {
        return activeQuery().eq(ShootMachineEntity::getId, id).one();
    }

    /** 仅查询 deleted=false 的记录 */
    private LambdaQueryChainWrapper<ShootMachineEntity> activeQuery() {
        return lambdaQuery().eq(ShootMachineEntity::getDeleted, false);
    }

    /** 机台编号唯一；excludeId 为编辑时排除自身 */
    private void checkMachineCodeUnique(String machineCode, Long excludeId) {
        if (existsActive(
                excludeId, query -> query.eq(ShootMachineEntity::getMachineCode, machineCode))) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "机台编号已存在");
        }
    }

    /** IP 唯一；excludeId 为编辑时排除自身 */
    private void checkIpUnique(String ip, Long excludeId) {
        if (existsActive(excludeId, query -> query.eq(ShootMachineEntity::getIp, ip))) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "IP 已被其他机台使用");
        }
    }

    /**
     * 在未删除机台中判断是否已存在满足条件的记录。
     *
     * @param excludeId 编辑时传入当前机台 id，新增时传 null
     */
    private boolean existsActive(
            Long excludeId,
            Function<LambdaQueryChainWrapper<ShootMachineEntity>, LambdaQueryChainWrapper<ShootMachineEntity>>
                    condition) {
        LambdaQueryChainWrapper<ShootMachineEntity> query = condition.apply(activeQuery());
        if (excludeId != null) {
            query.ne(ShootMachineEntity::getId, excludeId);
        }
        return query.exists();
    }

    /**
     * 根据 plc_data 最新采集时间填充运行状态。
     */
    private void fillPlcRunStatus(List<ShootMachineEntity> machines) {
        if (CollUtil.isEmpty(machines)) {
            return;
        }
        List<String> deviceNames =
                machines.stream()
                        .map(this::resolvePlcDeviceName)
                        .filter(StrUtil::isNotBlank)
                        .distinct()
                        .toList();
        if (deviceNames.isEmpty()) {
            return;
        }
        // 批量查最新时间
        Map<String, Date> latestByDevice = plcDataService.mapLatestDataTimestampByDeviceNames(deviceNames);
        long now = System.currentTimeMillis();
        for (ShootMachineEntity machine : machines) {
            String deviceName = resolvePlcDeviceName(machine);
            if (StrUtil.isBlank(deviceName)) {
                machine.setRunning(false);
                continue;
            }
            Date latest = latestByDevice.get(deviceName);
            machine.setLatestPlcDataTime(latest);
            machine.setRunning(latest != null && now - latest.getTime() <= PLC_STOP_THRESHOLD_MS);
        }
    }

    // 解析 plc 设备名称
    private String resolvePlcDeviceName(ShootMachineEntity machine) {
        return StrUtil.isNotBlank(machine.getMachineName()) ? machine.getMachineName().trim() : "";
    }
}
