package com.factorylink.common.config;

import com.factorylink.common.constant.Constants;
import java.io.File;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 * TODO 移走  不合适放在这里common包底下
 * @author valarchie
 */
@Component
@ConfigurationProperties(prefix = "factorylink")
@Data
public class FactoryLinkConfig {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 版权年份
     */
    private String copyrightYear;

    /**
     * 实例演示开关
     */
    private static boolean demoEnabled;

    /**
     * 配方导入时，自动创建不存在的原料
     */
    private static boolean formulaImportAutoCreateMaterial;

    /**
     * 上传路径
     */
    private static String fileBaseDir;

    /**
     * 获取地址开关
     */
    private static boolean addressEnabled;

    private static String apiPrefix;

    public static String getFileBaseDir() {
        return fileBaseDir;
    }

    public void setFileBaseDir(String fileBaseDir) {
        FactoryLinkConfig.fileBaseDir = fileBaseDir  + File.separator + Constants.RESOURCE_PREFIX;
    }

    public static String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiDocsPathPrefix) {
        FactoryLinkConfig.apiPrefix = apiDocsPathPrefix;
    }

    public static boolean isAddressEnabled() {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        FactoryLinkConfig.addressEnabled = addressEnabled;
    }

    public static boolean isDemoEnabled() {
        return demoEnabled;
    }

    public void setDemoEnabled(boolean demoEnabled) {
        FactoryLinkConfig.demoEnabled = demoEnabled;
    }

    public static boolean isFormulaImportAutoCreateMaterial() {
        return formulaImportAutoCreateMaterial;
    }

    public void setFormulaImportAutoCreateMaterial(boolean formulaImportAutoCreateMaterial) {
        FactoryLinkConfig.formulaImportAutoCreateMaterial = formulaImportAutoCreateMaterial;
    }

}
