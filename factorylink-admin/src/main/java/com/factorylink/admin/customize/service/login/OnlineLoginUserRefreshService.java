package com.factorylink.admin.customize.service.login;

import cn.hutool.core.collection.CollUtil;
import com.factorylink.domain.common.cache.RedisCacheService;
import com.factorylink.domain.system.role.db.SysRoleMenuEntity;
import com.factorylink.domain.system.role.db.SysRoleMenuService;
import com.factorylink.domain.system.user.db.SysUserEntity;
import com.factorylink.domain.system.user.db.SysUserService;
import com.factorylink.infrastructure.cache.redis.CacheKeyEnum;
import com.factorylink.infrastructure.user.web.SystemLoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

/**
 * 在线登录态刷新服务。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OnlineLoginUserRefreshService {

    private final RedisTemplate<String, ?> redisTemplate;

    private final RedisCacheService redisCache;

    private final SysUserService userService;

    private final SysRoleMenuService roleMenuService;

    private final RoleInfoResolver roleInfoResolver;

    public int refreshByUserIds(Collection<Long> userIds) {
        Set<Long> normalizedUserIds = normalizeIds(userIds);
        if (CollUtil.isEmpty(normalizedUserIds)) {
            return 0;
        }
        return refreshMatchedSessions(loginUser -> normalizedUserIds.contains(loginUser.getUserId()));
    }

    public int refreshByRoleIds(Collection<Long> roleIds) {
        Set<Long> normalizedRoleIds = normalizeIds(roleIds);
        if (CollUtil.isEmpty(normalizedRoleIds)) {
            return 0;
        }
        return refreshMatchedSessions(loginUser ->
            loginUser.getRoleInfo() != null && normalizedRoleIds.contains(loginUser.getRoleInfo().getRoleId()));
    }

    public int refreshByMenuIds(Collection<Long> menuIds) {
        Set<Long> normalizedMenuIds = normalizeIds(menuIds);
        if (CollUtil.isEmpty(normalizedMenuIds)) {
            return 0;
        }

        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRoleMenuEntity::getMenuId, normalizedMenuIds);
        Set<Long> roleIds = roleMenuService.list(queryWrapper).stream()
            .map(SysRoleMenuEntity::getRoleId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return refreshByRoleIds(roleIds);
    }

    private int refreshMatchedSessions(Predicate<SystemLoginUser> matcher) {
        String pattern = CacheKeyEnum.LOGIN_USER_KEY.key() + "*";
        Set<String> keys = new LinkedHashSet<>();
        try (Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions().match(pattern).count(100).build())) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        if (keys.isEmpty()) {
            return 0;
        }

        int refreshedCount = 0;
        for (String key : keys) {
            SystemLoginUser loginUser = redisCache.loginUserCache.getObjectOnlyInCacheByKey(key);
            if (loginUser == null || !matcher.test(loginUser)) {
                continue;
            }
            if (refreshLoginUser(key, loginUser)) {
                refreshedCount++;
            }
        }

        log.debug("refreshed {} online login users", refreshedCount);
        return refreshedCount;
    }

    private boolean refreshLoginUser(String redisKey, SystemLoginUser loginUser) {
        SysUserEntity latestUser = userService.getById(loginUser.getUserId());
        if (latestUser == null) {
            return false;
        }

        boolean isAdmin = Boolean.TRUE.equals(latestUser.getIsAdmin());
        loginUser.setUsername(latestUser.getUsername());
        loginUser.setPassword(latestUser.getPassword());
        loginUser.setDeptId(latestUser.getDeptId());
        loginUser.setAdmin(isAdmin);
        loginUser.setRoleInfo(roleInfoResolver.resolve(latestUser.getRoleId(), isAdmin));

        redisCache.loginUserCache.setByKey(redisKey, loginUser);
        return true;
    }

    private Set<Long> normalizeIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Set.of();
        }
        return ids.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
