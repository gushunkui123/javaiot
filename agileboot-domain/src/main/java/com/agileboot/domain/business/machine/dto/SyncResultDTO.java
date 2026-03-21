package com.agileboot.domain.business.machine.dto;

import lombok.Data;

/**
 * 设备下发结果
 */
@Data
public class SyncResultDTO {

    private DeviceResult mainScale;
    private DeviceResult microScale;

    @Data
    public static class DeviceResult {

        private boolean success;
        private String message;
        private boolean skipped;

        public static DeviceResult success() {
            DeviceResult result = new DeviceResult();
            result.setSuccess(true);
            result.setMessage("ok");
            return result;
        }

        public static DeviceResult fail(String message) {
            DeviceResult result = new DeviceResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }

        public static DeviceResult skipped(String reason) {
            DeviceResult result = new DeviceResult();
            result.setSkipped(true);
            result.setMessage(reason);
            return result;
        }
    }

    public boolean isAllSuccess() {
        return (mainScale == null || mainScale.isSuccess() || mainScale.isSkipped())
            && (microScale == null || microScale.isSuccess() || microScale.isSkipped());
    }
}
