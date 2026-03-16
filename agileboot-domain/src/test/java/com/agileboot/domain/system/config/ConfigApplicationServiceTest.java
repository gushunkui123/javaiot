package com.agileboot.domain.system.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.domain.common.cache.CacheCenter;
import com.agileboot.domain.system.config.command.ConfigUpdateCommand;
import com.agileboot.domain.system.config.db.SysConfigService;
import com.agileboot.domain.system.config.model.ConfigModel;
import com.agileboot.domain.system.config.model.ConfigModelFactory;
import com.agileboot.infrastructure.cache.guava.AbstractGuavaCacheTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigApplicationServiceTest {

    private final ConfigModelFactory configModelFactory = mock(ConfigModelFactory.class);
    private final SysConfigService configService = mock(SysConfigService.class);
    private final ConfigApplicationService applicationService =
        new ConfigApplicationService(configModelFactory, configService);

    @SuppressWarnings("unchecked")
    private final AbstractGuavaCacheTemplate<String> configCache = mock(AbstractGuavaCacheTemplate.class);

    private AbstractGuavaCacheTemplate<String> originalConfigCache;

    @BeforeEach
    void setUp() {
        originalConfigCache = CacheCenter.configCache;
        CacheCenter.configCache = configCache;
    }

    @AfterEach
    void tearDown() {
        CacheCenter.configCache = originalConfigCache;
    }

    @Test
    void updateConfigShouldInvalidateConfigCache() {
        ConfigUpdateCommand command = new ConfigUpdateCommand();
        command.setConfigId(1L);
        ConfigModel configModel = mock(ConfigModel.class);
        when(configModelFactory.loadById(1L)).thenReturn(configModel);
        when(configModel.getConfigKey()).thenReturn("sys.account.captchaEnabled");

        applicationService.updateConfig(command);

        verify(configModel).loadUpdateCommand(command);
        verify(configModel).checkCanBeModify();
        verify(configModel).updateById();
        verify(configCache).invalidate("sys.account.captchaEnabled");
    }
}
