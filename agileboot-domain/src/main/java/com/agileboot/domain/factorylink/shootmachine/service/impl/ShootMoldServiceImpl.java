package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootDeleteValidator;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShootMoldServiceImpl extends ServiceImpl<ShootMoldMapper, ShootMoldEntity>
        implements ShootMoldService {

    private final ShootDeleteValidator deleteValidator;

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
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID ,"请先添加模具信息");
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
        getByIdOrThrow(id);
        validateMold(entity);
        entity.setId(id);
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getByIdOrThrow(id);
        deleteValidator.assertNoMoldRules(id);
        deleteValidator.assertNoMoldActiveSchedule(id);
        deleteValidator.assertNoAlarm(null, id, null, null);
        removeById(id);
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
