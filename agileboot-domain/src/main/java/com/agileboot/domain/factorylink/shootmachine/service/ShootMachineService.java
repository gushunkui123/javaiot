package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ShootMachineService extends IService<ShootMachineEntity> {

    IPage<ShootMachineEntity> list(int pageNum, int pageSize);

    ShootMachineEntity getByIdOrThrow(Long id);

    ShootMachineEntity create(ShootMachineEntity entity);

    ShootMachineEntity update(Long id, ShootMachineEntity entity);

    void delete(Long id);


}
