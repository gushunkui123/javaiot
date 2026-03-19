package com.agileboot.domain.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.agileboot.infrastructure.cache.guava.AbstractGuavaCacheTemplate;
import com.agileboot.infrastructure.cache.redis.RedisCacheTemplate;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import com.agileboot.domain.system.dept.db.SysDeptEntity;
import com.agileboot.domain.system.post.db.SysPostEntity;
import com.agileboot.domain.system.role.db.SysRoleEntity;
import com.agileboot.domain.system.user.db.SysUserEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * 缓存中心  提供全局访问点
 * 如果是领域类的缓存  可以自己新建一个直接放在CacheCenter   不用放在infrastructure包里的GuavaCacheService
 * 或者RedisCacheService
 * @author valarchie
 */
@Component
public class CacheCenter {

    public static AbstractGuavaCacheTemplate<String> configCache;

    public static AbstractGuavaCacheTemplate<SysDeptEntity> deptCache;

    public static RedisCacheTemplate<SystemLoginUser> loginUserCache;

    public static RedisCacheTemplate<SysUserEntity> userCache;

    public static RedisCacheTemplate<SysRoleEntity> roleCache;

    public static RedisCacheTemplate<SysPostEntity> postCache;

    public static RedisCacheTemplate<String> usernameCache;

    @PostConstruct
    public void init() {
        GuavaCacheService guavaCache = SpringUtil.getBean(GuavaCacheService.class);
        RedisCacheService redisCache = SpringUtil.getBean(RedisCacheService.class);

        configCache = guavaCache.configCache;
        deptCache = guavaCache.deptCache;

        loginUserCache = redisCache.loginUserCache;
        userCache = redisCache.userCache;
        roleCache = redisCache.roleCache;
        postCache = redisCache.postCache;
        usernameCache = redisCache.usernameCache;
    }

    /**
     * 删除用户相关的所有缓存（实体缓存 + 用户名缓存）
     */
    public static void deleteUserCache(Long userId) {
        if (userCache != null) {
            userCache.delete(userId);
        }
        if (usernameCache != null) {
            usernameCache.delete(userId);
        }
    }

    public static String getUsernameById(Long userId) {
        if (userId == null || usernameCache == null) {
            return null;
        }
        return usernameCache.getObjectById(userId);
    }

}
