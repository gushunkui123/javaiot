package com.factorylink.infrastructure.machine.dto.response;

import lombok.Data;

/**
 * 设备端料桶与原料映射数据
 */
@Data
public class MaterialInBucketData {

    private String bucketNo;
    private String materialNo;
}
