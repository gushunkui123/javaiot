package com.factorylink.integrationTest.db;

import com.factorylink.domain.system.config.db.SysConfigService;
import com.factorylink.integrationTest.IntegrationTestApplication;
import com.factorylink.common.enums.common.ConfigKeyEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = IntegrationTestApplication.class)
@RunWith(SpringRunner.class)
class  SysConfigServiceImplTest {

    @Resource
    SysConfigService configService;

    @Test
    void testGetConfigValueByKey() {
        String configValue = configService.getConfigValueByKey(ConfigKeyEnum.REGISTER.getValue());
        Assertions.assertFalse(Boolean.parseBoolean(configValue));
    }


}
