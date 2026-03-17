package com.agileboot.infrastructure.cache.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.infrastructure.cache.RedisUtil;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RedisCacheTemplateTest {

    private final RedisUtil redisUtil = mock(RedisUtil.class);

    @Test
    void getObjectByIdShouldReturnRedisValueWithoutDbLookup() {
        TestRedisCacheTemplate template = new TestRedisCacheTemplate(redisUtil, CacheKeyEnum.USER_ENTITY_KEY);
        when(redisUtil.getCacheObject("user_entity:1")).thenReturn("redis-value");

        String value = template.getObjectById(1L);

        assertEquals("redis-value", value);
        assertEquals(0, template.getDbLookups());
        verify(redisUtil, never()).setCacheObject(anyString(), any(), anyInt(), any());
    }

    @Test
    void getObjectByIdShouldLoadFromDbAndWriteRedisWhenMiss() {
        TestRedisCacheTemplate template = new TestRedisCacheTemplate(redisUtil, CacheKeyEnum.USER_ENTITY_KEY);
        template.setDbValue("db-value");
        when(redisUtil.getCacheObject("user_entity:2")).thenReturn(null);

        String value = template.getObjectById(2L);

        assertEquals("db-value", value);
        assertEquals(1, template.getDbLookups());
        verify(redisUtil).setCacheObject("user_entity:2", "db-value", 60, TimeUnit.MINUTES);
    }

    @Test
    void getObjectOnlyInCacheByIdShouldNotLoadFromDb() {
        TestRedisCacheTemplate template = new TestRedisCacheTemplate(redisUtil, CacheKeyEnum.RATE_LIMIT_KEY);
        template.setDbValue("db-value");
        when(redisUtil.getCacheObject("rate_limit:code-1")).thenReturn(null);

        String value = template.getObjectOnlyInCacheById("code-1");

        assertNull(value);
        assertEquals(0, template.getDbLookups());
    }

    @Test
    void setByKeyShouldWriteDirectlyToRedis() {
        TestRedisCacheTemplate template = new TestRedisCacheTemplate(redisUtil, CacheKeyEnum.LOGIN_USER_KEY);

        template.setByKey("login_tokens:token-1", "login-user");

        verify(redisUtil).setCacheObject("login_tokens:token-1", "login-user", 30, TimeUnit.MINUTES);
    }

    @Test
    void deleteAndRefreshShouldOnlyTouchRedis() {
        TestRedisCacheTemplate template = new TestRedisCacheTemplate(redisUtil, CacheKeyEnum.POST_ENTITY_KEY);

        template.delete(9L);
        template.refresh(9L);

        verify(redisUtil).deleteObject("post_entity:9");
        verify(redisUtil).expire("post_entity:9", 60, TimeUnit.MINUTES);
    }

    private static final class TestRedisCacheTemplate extends RedisCacheTemplate<String> {

        private String dbValue;
        private int dbLookups;

        private TestRedisCacheTemplate(RedisUtil redisUtil, CacheKeyEnum cacheKeyEnum) {
            super(redisUtil, cacheKeyEnum);
        }

        private void setDbValue(String dbValue) {
            this.dbValue = dbValue;
        }

        private int getDbLookups() {
            return dbLookups;
        }

        @Override
        public String getObjectFromDb(Object id) {
            dbLookups++;
            return dbValue;
        }
    }
}
