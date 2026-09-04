package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.MachineGroupEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ShootMachineService extends IService<ShootMachineEntity> {

    // 分页查询（enabled 为 null 时返回全部；group 为空时返回全部分组）
    PageDTO<ShootMachineEntity> list(int pageNum, int pageSize, Boolean enabled, String group);
    // 查询启用分组（下拉选项）
    List<MachineGroupEntity> listGroups();
    // 根据 id 查询
    ShootMachineEntity getByIdOrThrow(Long id);
    // 创建
    ShootMachineEntity create(ShootMachineEntity entity);
    // 更新
    ShootMachineEntity update(Long id, ShootMachineEntity entity);
    // 删除
    void delete(Long id);
}
