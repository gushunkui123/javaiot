package com.agileboot.infrastructure.cache.redis;

import com.agileboot.infrastructure.cache.RedisUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存接口实现类，直接使用 Redis 作为缓存介质。
 * @author valarchie
 */
@Slf4j
public class RedisCacheTemplate<T> {

    private final RedisUtil redisUtil;
    private final CacheKeyEnum redisRedisEnum;

    public RedisCacheTemplate(RedisUtil redisUtil, CacheKeyEnum redisRedisEnum) {
        this.redisUtil = redisUtil;
        this.redisRedisEnum = redisRedisEnum;
    }

    /**
     * 从缓存中获取对象   如果获取不到的话  从DB层面获取
     *
     * @param id id
     */
    public T getObjectById(Object id) {
        String cachedKey = generateKey(id);
        T cacheObject = redisUtil.getCacheObject(cachedKey);
        log.debug("find the redis cache of key: {} is {}", cachedKey, cacheObject);
        if (cacheObject != null) {
            return cacheObject;
        }

        T objectFromDb = getObjectFromDb(id);
        if (objectFromDb != null) {
            set(id, objectFromDb);
        }
        return objectFromDb;
    }

    /**
     * 从缓存中获取 对象， 即使找不到的话 也不从DB中找
     * @param id id
     */
    public T getObjectOnlyInCacheById(Object id) {
        String cachedKey = generateKey(id);
        T cacheObject = redisUtil.getCacheObject(cachedKey);
        log.debug("find the redis cache of key: {} is {}", cachedKey, cacheObject);
        return cacheObject;
    }

    /**
     * 从缓存中获取 对象， 即使找不到的话 也不从DB中找
     * @param cachedKey 直接通过redis的key来搜索
     */
    public T getObjectOnlyInCacheByKey(String cachedKey) {
        T cacheObject = redisUtil.getCacheObject(cachedKey);
        log.debug("find the redis cache of key: {} is {}", cachedKey, cacheObject);
        return cacheObject;
    }


    public void set(Object id, T obj) {
        String fullKey = generateKey(id);
        redisUtil.setCacheObject(fullKey, obj, redisRedisEnum.expiration(), redisRedisEnum.timeUnit());
    }

    /**
     * 通过完整的Redis key直接设置缓存
     */
    public void setByKey(String fullKey, T obj) {
        redisUtil.setCacheObject(fullKey, obj, redisRedisEnum.expiration(), redisRedisEnum.timeUnit());
    }

    public void delete(Object id) {
        String fullKey = generateKey(id);
        redisUtil.deleteObject(fullKey);
    }

    public void refresh(Object id) {
        String fullKey = generateKey(id);
        redisUtil.expire(fullKey, redisRedisEnum.expiration(), redisRedisEnum.timeUnit());
    }

    public String generateKey(Object id) {
        return redisRedisEnum.key() + id;
    }

    public T getObjectFromDb(Object id) {
        return null;
    }

}
