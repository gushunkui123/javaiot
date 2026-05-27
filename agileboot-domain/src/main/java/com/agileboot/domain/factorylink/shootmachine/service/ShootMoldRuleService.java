package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldRuleEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ShootMoldRuleService extends IService<ShootMoldRuleEntity> {

    /** 查询所有模具的规则列表 */
    List<ShootMoldRuleEntity> listAll();

    List<ShootMoldRuleEntity> listByMoldId(Long moldId);

    ShootMoldRuleEntity getByIdOrThrow(Long id);

    ShootMoldRuleEntity create(ShootMoldRuleEntity entity);

    ShootMoldRuleEntity update(Long id, ShootMoldRuleEntity entity);

    void delete(Long id);

    /** 按模具整批保存规则。 */
    void saveByMoldId(Long moldId, List<ShootMoldRuleEntity> rules);
}
