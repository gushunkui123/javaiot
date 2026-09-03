package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ShootMoldService extends IService<ShootMoldEntity> {

    PageDTO<ShootMoldEntity> list(int pageNum, int pageSize, Boolean enabled, String moldModel, String color);

    ShootMoldEntity getByIdOrThrow(Long id);

    /** 查询模具并校验其已启用（停用的模具不能排产） */
    ShootMoldEntity getEnabledOrThrow(Long id);

    ShootMoldEntity create(ShootMoldEntity entity);

    ShootMoldEntity update(Long id, ShootMoldEntity entity);

    void delete(Long id);
}
