package com.factorylink.domain.system.monitor;

import cn.hutool.core.util.StrUtil;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Internal;
import com.factorylink.domain.common.cache.CacheCenter;
import com.factorylink.domain.system.monitor.dto.OnlineUserDTO;
import com.factorylink.domain.system.monitor.dto.RedisCacheInfoDTO;
import com.factorylink.domain.system.monitor.dto.RedisCacheInfoDTO.CommandStatusDTO;
import com.factorylink.domain.system.monitor.dto.ServerInfo;
import com.factorylink.infrastructure.cache.redis.CacheKeyEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class MonitorApplicationService {

    private final RedisTemplate<String, ?> redisTemplate;

    public RedisCacheInfoDTO getRedisCacheInfo() {
        Properties info = (Properties) redisTemplate.execute(
            (RedisCallback<Object>) connection -> connection.serverCommands().info());
        Properties commandStats = (Properties) redisTemplate.execute(
            (RedisCallback<Object>) connection -> connection.serverCommands().info("commandstats"));
        Long dbSize = redisTemplate.execute(
            (RedisCallback<Long>) connection -> connection.serverCommands().dbSize());

        if (commandStats == null || info == null) {
            throw new ApiException(Internal.INTERNAL_ERROR, "获取Redis监控信息失败。");
        }

        RedisCacheInfoDTO cacheInfo = new RedisCacheInfoDTO();

        cacheInfo.setInfo(info);
        cacheInfo.setDbSize(dbSize);
        cacheInfo.setCommandStats(new ArrayList<>());

        commandStats.stringPropertyNames().forEach(key -> {
            String property = commandStats.getProperty(key);

            CommandStatusDTO commonStatus = new CommandStatusDTO();
            commonStatus.setName(StrUtil.removePrefix(key, "cmdstat_"));
            commonStatus.setValue(StrUtil.subBetween(property, "calls=", ",usec"));

            cacheInfo.getCommandStats().add(commonStatus);
        });

        return cacheInfo;
    }

    public List<OnlineUserDTO> getOnlineUserList(String username, String ipAddress) {
        Collection<String> keys = redisTemplate.keys(CacheKeyEnum.LOGIN_USER_KEY.key() + "*");

        Stream<OnlineUserDTO> onlineUserStream = keys.stream().map(o ->
                    CacheCenter.loginUserCache.getObjectOnlyInCacheByKey(o))
            .filter(Objects::nonNull).map(OnlineUserDTO::new);

        return onlineUserStream
            .filter(o ->
                StrUtil.isEmpty(username) || username.equals(o.getUsername())
            ).filter( o ->
                StrUtil.isEmpty(ipAddress) || ipAddress.equals(o.getIpAddress())
            ).toList()
            .reversed();
    }

    public ServerInfo getServerInfo() {
        return ServerInfo.fillInfo();
    }


}
