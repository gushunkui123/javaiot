package com.factorylink.domain.business.workorder;

import com.factorylink.domain.business.machine.ScaleQueryService;
import com.factorylink.domain.business.workorder.db.BizWorkOrderEntity;
import com.factorylink.domain.business.workorder.db.BizWorkOrderService;
import com.factorylink.infrastructure.machine.dto.ScaleApiResponse;
import com.factorylink.infrastructure.machine.dto.response.WorkOrderData;
import com.factorylink.infrastructure.sse.SseConnectionManager;
import com.factorylink.infrastructure.sse.SseMessage;
import com.factorylink.infrastructure.sse.SseMessageLevel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 工单生产状态轮询服务
 * <p>
 * 定期查询所有"生产中"状态的工单，通过磅秤设备API获取最新的生产进度数据
 * （完工批次数、完工重量等），同步更新到本地数据库。
 * 当设备端报告工单已完工时，自动将本地工单状态更新为"已完成"，并通过SSE推送通知。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkOrderPollingService {

    private static final int PROCESS_STATUS_PRODUCING = 3;
    private static final int PROCESS_STATUS_COMPLETED = 4;
    private static final int ORDER_STATE_PRODUCING = 2;
    private static final int ORDER_STATE_COMPLETED = 3;

    private final BizWorkOrderService workOrderService;
    private final ScaleQueryService scaleQueryService;
    private final SseConnectionManager sseConnectionManager;

    /**
     * 轮询生产中的工单，从设备获取最新生产数据并更新本地数据库。
     * 默认每30秒执行一次，可通过 factorylink.polling.work-order-interval 配置。
     */
    @Scheduled(fixedDelayString = "${factorylink.polling.work-order-interval:30000}")
    public void pollProducingWorkOrders() {
        List<BizWorkOrderEntity> producingOrders = workOrderService.list(
            new LambdaQueryWrapper<BizWorkOrderEntity>()
                .eq(BizWorkOrderEntity::getProcessStatus, PROCESS_STATUS_PRODUCING)
                .eq(BizWorkOrderEntity::getOrderState, ORDER_STATE_PRODUCING)
        );

        if (producingOrders.isEmpty()) {
            return;
        }

        log.debug("轮询生产中工单，共{}条", producingOrders.size());

        for (BizWorkOrderEntity order : producingOrders) {
            try {
                pollSingleWorkOrder(order);
            } catch (Exception e) {
                log.warn("轮询工单[{}]失败: {}", order.getWorkOrderNo(), e.getMessage());
            }
        }
    }

    private void pollSingleWorkOrder(BizWorkOrderEntity localOrder) {
        // 从主磅设备查询工单生产数据
        ScaleApiResponse<WorkOrderData> response = scaleQueryService.queryMainScaleWorkOrder(
            localOrder.getWorkOrderNo(), null, null, null, null);

        if (!response.isSuccess() || response.getRtndata() == null || response.getRtndata().isEmpty()) {
            log.debug("设备未返回工单[{}]数据", localOrder.getWorkOrderNo());
            return;
        }

        // 从设备返回数据中匹配工单
        WorkOrderData deviceData = response.getRtndata().stream()
            .filter(d -> localOrder.getWorkOrderNo().equals(d.getWorkOrderNo()))
            .findFirst()
            .orElse(response.getRtndata().get(0));

        updateWorkOrderFromDevice(localOrder, deviceData);
    }

    private void updateWorkOrderFromDevice(BizWorkOrderEntity localOrder, WorkOrderData deviceData) {
        boolean changed = false;
        BizWorkOrderEntity update = new BizWorkOrderEntity();
        update.setWorkOrderId(localOrder.getWorkOrderId());

        // 更新完工批次数
        if (deviceData.getFinishBatchNum() != null
                && !deviceData.getFinishBatchNum().equals(localOrder.getFinishBatchNum())) {
            update.setFinishBatchNum(deviceData.getFinishBatchNum());
            changed = true;
        }

        // 更新完工重量
        if (deviceData.getFinishWeight() != null
                && (localOrder.getFinishWeight() == null
                    || deviceData.getFinishWeight().compareTo(localOrder.getFinishWeight()) != 0)) {
            update.setFinishWeight(deviceData.getFinishWeight());
            changed = true;
        }

        // 检查设备端工单是否已完工
        boolean completed = deviceData.getOrderState() != null
                && deviceData.getOrderState() == ORDER_STATE_COMPLETED;

        if (completed) {
            update.setOrderState(ORDER_STATE_COMPLETED);
            update.setProcessStatus(PROCESS_STATUS_COMPLETED);
            update.setFinishTime(new Date());
            changed = true;
        }

        if (changed) {
            workOrderService.updateById(update);
            log.info("工单[{}]生产数据已更新: 完工批次={}, 完工重量={}, 已完工={}",
                localOrder.getWorkOrderNo(),
                deviceData.getFinishBatchNum(),
                deviceData.getFinishWeight(),
                completed);

            if (completed) {
                notifyWorkOrderCompleted(localOrder);
            }
        }
    }

    private void notifyWorkOrderCompleted(BizWorkOrderEntity order) {
        try {
            SseMessage message = SseMessage.builder()
                .level(SseMessageLevel.NOTIFICATION)
                .title("工单生产完成")
                .content(String.format("工单 %s 已完成生产", order.getWorkOrderNo()))
                .build();
            sseConnectionManager.broadcast(message);
            log.info("工单完工通知已推送, workOrderNo={}", order.getWorkOrderNo());
        } catch (Exception e) {
            log.warn("推送工单完工通知失败: {}", e.getMessage());
        }
    }
}
