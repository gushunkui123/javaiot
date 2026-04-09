package com.factorylink.domain.system.user.dto;

import com.factorylink.domain.common.cache.CacheCenter;
import com.factorylink.domain.system.role.db.SysRoleEntity;
import com.factorylink.domain.system.user.db.SysUserEntity;
import java.util.Objects;
import lombok.Data;

/**
 * 用户账号摘要
 *
 * @author valarchie
 */
@Data
public class UserAccountSummaryDTO {

    public UserAccountSummaryDTO(SysUserEntity entity) {
        if (entity == null) {
            return;
        }
        this.roleName = "";
        this.username = entity.getUsername();
        this.email = entity.getEmail();
        this.enabled = Objects.equals(entity.getStatus(), 1);

        if (entity.getRoleId() != null && CacheCenter.roleCache != null) {
            SysRoleEntity roleEntity = CacheCenter.roleCache.getObjectById(entity.getRoleId());
            this.roleName = roleEntity != null ? roleEntity.getRoleName() : "";
        }
    }

    private String username;

    private String roleName;

    private String email;

    private Boolean enabled;

}
