package com.factorylink.admin.config;


import com.factorylink.admin.FactoryLinkAdminApplication;
import com.factorylink.common.config.FactoryLinkConfig;
import com.factorylink.common.constant.Constants.UploadSubDir;
import java.io.File;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = FactoryLinkAdminApplication.class)
@RunWith(SpringRunner.class)
public class FactoryLinkConfigTest {

    @Resource
    private FactoryLinkConfig config;

    @Test
    public void testConfig() {
        String fileBaseDir = "D:\\factorylink\\profile";

        Assertions.assertEquals("FactoryLink", config.getName());
        Assertions.assertEquals("1.8.0", config.getVersion());
        Assertions.assertEquals("2022", config.getCopyrightYear());
        Assertions.assertFalse(config.isDemoEnabled());
        Assertions.assertEquals(fileBaseDir, FactoryLinkConfig.getFileBaseDir());
        Assertions.assertFalse(FactoryLinkConfig.isAddressEnabled());
        Assertions.assertEquals(fileBaseDir + "\\import",
            FactoryLinkConfig.getFileBaseDir() + File.separator + UploadSubDir.IMPORT_PATH);
        Assertions.assertEquals(fileBaseDir + "\\avatar",
            FactoryLinkConfig.getFileBaseDir() + File.separator + UploadSubDir.AVATAR_PATH);
        Assertions.assertEquals(fileBaseDir + "\\download",
            FactoryLinkConfig.getFileBaseDir() + File.separator + UploadSubDir.DOWNLOAD_PATH);
        Assertions.assertEquals(fileBaseDir + "\\upload",
            FactoryLinkConfig.getFileBaseDir() + File.separator + UploadSubDir.UPLOAD_PATH);
    }

}
