package com.factorylink.domain.business.machine.dto;

import java.util.List;
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
        private List<String> warnings;

        public static DeviceResult success() {
            DeviceResult result = new DeviceResult();
            result.setSuccess(true);
            result.setMessage("ok");
            return result;
        }

        public static DeviceResult success(List<String> warnings) {
            DeviceResult result = new DeviceResult();
            result.setSuccess(true);
            result.setMessage("ok");
            result.setWarnings(warnings);
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
