package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.domain.factorylink.plc.entity.EnvironmentDataEntity;
import com.agileboot.domain.factorylink.plc.mapper.EnvironmentDataMapper;
import com.agileboot.domain.factorylink.plc.service.EnvironmentDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EnvironmentDataServiceImpl extends ServiceImpl<EnvironmentDataMapper, EnvironmentDataEntity>
        implements EnvironmentDataService {

    private static final String PM25_KEY = "pm2.5";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ingest(String topic, String jsonPayload) {
        if (StrUtil.isBlank(topic) || StrUtil.isBlank(jsonPayload)) {
            return;
        }
        JSONObject root;
        try {
            root = JSONUtil.parseObj(jsonPayload);
        } catch (Exception e) {
            log.warn("environment MQTT JSON parse failed: {}", e.getMessage());
            return;
        }
        String mac = StrUtil.trim(root.getStr("mac"));
        if (StrUtil.isBlank(mac)) {
            log.warn("environment MQTT missing mac, topic={}", topic);
            return;
        }
        BigDecimal pm25 = Convert.toBigDecimal(root.get(PM25_KEY), null);

        EnvironmentDataEntity row = new EnvironmentDataEntity();
        row.setTopic(topic.trim());
        row.setMac(mac);
        row.setPm25(pm25);
        row.setDataTimestamp(LocalDateTime.now());
        save(row);
    }

    @Override
    public EnvironmentDataEntity latestByMac(String mac) {
        if (StrUtil.isBlank(mac)) {
            return null;
        }
        return getOne(
                lambdaQuery()
                        .eq(EnvironmentDataEntity::getMac, mac.trim())
                        .orderByDesc(EnvironmentDataEntity::getDataTimestamp)
                        .last("LIMIT 1")
                        .getWrapper(),
                false);
    }
}
